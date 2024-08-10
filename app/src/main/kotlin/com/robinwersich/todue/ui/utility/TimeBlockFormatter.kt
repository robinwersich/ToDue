package com.robinwersich.todue.ui.utility

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.TimeUnitInstanceSequence
import com.robinwersich.todue.domain.model.Week
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.threeten.extra.YearWeek

/**
 * Provides an up-to-date [TimeBlockFormatter] based on the current locale and the given options.
 */
@Composable
fun rememberTimeBlockFormatter(): TimeBlockFormatter {
  val resources = LocalContext.current.resources
  val languageTag = resources.configuration.locales[0].toLanguageTag()
  // TODO: with strong skipping, it might be possible to use resources as key directly
  return remember(languageTag) { TimeBlockFormatter(resources) }
}

/**
 * A formatter turning [TimeBlocks][TimeBlock] into human-readable strings.
 *
 * @param resources The resources of the current language to use for string formatting.
 */
class TimeBlockFormatter(private val resources: Resources) {
  private val wideFormatters = Formatters.fromResources(resources)
  private val narrowFormatters = Formatters.fromResourcesNarrow(resources)

  /**
   * Formats a [TimeBlock] into a human-readable string.
   *
   * @param timeBlock The time block to format.
   * @param today The current date to use as a reference for today, tomorrow, this week, etc. and to
   *   determine if the year should be omitted. If null, no context is used and the year is always
   *   included.
   * @param useNarrowFormatting Split the text over multiple lines to make it more narrow.
   */
  fun format(
    timeBlock: TimeBlock,
    today: LocalDate? = LocalDate.now(),
    useNarrowFormatting: Boolean = false,
  ) = format(timeBlock, today, if (useNarrowFormatting) narrowFormatters else wideFormatters)

  private fun format(
    timeBlock: TimeBlock,
    today: LocalDate? = LocalDate.now(),
    formatters: Formatters,
  ): String =
    when (timeBlock) {
      is Day -> formatDay(timeBlock, today, formatters)
      is Week -> formatWeek(timeBlock, today, formatters)
      is Month -> formatMonth(timeBlock, today, formatters)
      is TimeUnitInstanceSequence -> {
        when {
          timeBlock.startBlock == timeBlock.endBlock ->
            format(timeBlock.startBlock, today, formatters)
          // if single weeks are represented as date range, we only need to format the outer range
          timeBlock.unit == TimeUnit.WEEK -> formatDateRange(timeBlock, today, formatters)
          else ->
            format(timeBlock.startBlock, today, formatters) +
              format(timeBlock.endBlock, today, formatters)
        }
      }

      else -> formatDateRange(timeBlock, today, formatters)
    }

  private fun formatDay(day: Day, today: LocalDate?, formatters: Formatters) =
    when (day.date) {
      today -> formatters.today
      today?.plusDays(1) -> formatters.tomorrow
      today?.minusDays(1) -> formatters.yesterday
      else -> {
        val isThisYear = day.date.year == today?.year
        val formatter = if (isThisYear) formatters.day else formatters.dayWithYear
        formatter.format(day.date)
      }
    }

  private fun formatWeek(week: Week, today: LocalDate?, formatters: Formatters): String {
    val thisWeek = today?.let { YearWeek.from(it) }
    return when (week.yearWeek) {
      thisWeek -> formatters.thisWeek
      thisWeek?.plusWeeks(1) -> formatters.nextWeek
      thisWeek?.minusWeeks(1) -> formatters.lastWeek
      else -> formatDateRange(week, today, formatters)
    }
  }

  private fun formatDateRange(range: DateRange, today: LocalDate?, formatters: Formatters): String {
    val isSameYear = range.start.year == range.endInclusive.year
    val isSameMonth = range.start.month == range.endInclusive.month && isSameYear
    val useStartYear = !isSameYear
    val useEndYear = range.endInclusive.year != today?.year || !isSameYear

    val startFormatter =
      when {
        isSameMonth -> formatters.dateRangeStartSameMonth
        useStartYear -> formatters.dateRangeStartWithYear
        else -> formatters.dateRangeStart
      }
    val endFormatter =
      when {
        isSameMonth && useEndYear -> formatters.dateRangeEndSameMonthWithYear
        isSameMonth -> formatters.dateRangeEndSameMonth
        useEndYear -> formatters.dateRangeEndWithYear
        else -> formatters.dateRangeEnd
      }

    return startFormatter.format(range.start) + endFormatter.format(range.endInclusive)
  }

  private fun formatMonth(month: Month, today: LocalDate?, formatters: Formatters): String {
    val useYear = month.yearMonth.year == today?.year
    val formatter = if (useYear) formatters.month else formatters.monthWithYear
    return formatter.format(month.yearMonth)
  }
}

private data class Formatters(
  val yesterday: String,
  val today: String,
  val tomorrow: String,
  val lastWeek: String,
  val thisWeek: String,
  val nextWeek: String,
  val day: DateTimeFormatter,
  val dayWithYear: DateTimeFormatter,
  val month: DateTimeFormatter,
  val monthWithYear: DateTimeFormatter,
  val year: DateTimeFormatter,
  val dateRangeStart: DateTimeFormatter,
  val dateRangeStartSameMonth: DateTimeFormatter,
  val dateRangeStartWithYear: DateTimeFormatter,
  val dateRangeEnd: DateTimeFormatter,
  val dateRangeEndSameMonth: DateTimeFormatter,
  val dateRangeEndSameMonthWithYear: DateTimeFormatter,
  val dateRangeEndWithYear: DateTimeFormatter,
) {
  companion object {
    fun fromResources(resources: Resources) =
      with(resources) {
        Formatters(
          yesterday = getString(R.string.yesterday),
          today = getString(R.string.today),
          tomorrow = getString(R.string.tomorrow),
          lastWeek = getString(R.string.last_week),
          thisWeek = getString(R.string.this_week),
          nextWeek = getString(R.string.next_week),
          day = formatter(R.string.format_day),
          dayWithYear = formatter(R.string.format_day_with_year),
          month = formatter(R.string.format_month),
          monthWithYear = formatter(R.string.format_month_with_year),
          year = formatter(R.string.format_year),
          dateRangeStart = formatter(R.string.format_date_range_start),
          dateRangeStartSameMonth = formatter(R.string.format_date_range_start_same_month),
          dateRangeStartWithYear = formatter(R.string.format_date_range_start_with_year),
          dateRangeEnd = formatter(R.string.format_date_range_end),
          dateRangeEndSameMonth = formatter(R.string.format_date_range_end_same_month),
          dateRangeEndSameMonthWithYear =
            formatter(R.string.format_date_range_end_same_month_with_year),
          dateRangeEndWithYear = formatter(R.string.format_date_range_end_with_year),
        )
      }

    fun fromResourcesNarrow(resources: Resources) =
      with(resources) {
        Formatters(
          yesterday = getString(R.string.yesterday_narrow),
          today = getString(R.string.today_narrow),
          tomorrow = getString(R.string.tomorrow_narrow),
          lastWeek = getString(R.string.last_week_narrow),
          thisWeek = getString(R.string.this_week_narrow),
          nextWeek = getString(R.string.next_week_narrow),
          day = formatter(R.string.format_day_narrow),
          dayWithYear = formatter(R.string.format_day_with_year_narrow),
          month = formatter(R.string.format_month_narrow),
          monthWithYear = formatter(R.string.format_month_with_year_narrow),
          year = formatter(R.string.format_year_narrow),
          dateRangeStart = formatter(R.string.format_date_range_start_narrow),
          dateRangeStartSameMonth = formatter(R.string.format_date_range_start_same_month_narrow),
          dateRangeStartWithYear = formatter(R.string.format_date_range_start_with_year_narrow),
          dateRangeEnd = formatter(R.string.format_date_range_end_narrow),
          dateRangeEndSameMonth = formatter(R.string.format_date_range_end_same_month_narrow),
          dateRangeEndSameMonthWithYear =
            formatter(R.string.format_date_range_end_same_month_with_year_narrow),
          dateRangeEndWithYear = formatter(R.string.format_date_range_end_with_year_narrow),
        )
      }
  }
}

private fun Resources.formatter(@StringRes id: Int) = DateTimeFormatter.ofPattern(getString(id))
