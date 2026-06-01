package com.robinwersich.todue.ui.presentation.organizer.formatting

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import org.threeten.extra.YearWeek
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.TimeUnitInstanceSequence
import com.robinwersich.todue.domain.model.Week

/** Components for collapsed time block labels, rendered in a column. */
sealed interface TimeBlockLabelComponent {
  data class Title(val text: String) : TimeBlockLabelComponent

  data class Subtitle(val text: String) : TimeBlockLabelComponent

  data object Divider : TimeBlockLabelComponent
}

/** For expanded time blocks */
data class TimeBlockHeading(val title: String, val subtitle: String? = null)

@Composable
fun rememberTimeBlockFormatter(): TimeBlockFormatter {
  val resources = LocalContext.current.resources
  return remember(resources) { TimeBlockFormatter(resources) }
}

class TimeBlockFormatter(resources: Resources) {
  private val headingFormatters = HeadingFormatters.fromResources(resources)
  private val labelFormatters = LabelFormatters.fromResources(resources)

  fun format(
    timeBlock: TimeBlock,
    today: LocalDate? = LocalDate.now(),
  ) = format(timeBlock, today, headingFormatters)

  fun formatLabel(timeBlock: TimeBlock): List<TimeBlockLabelComponent> =
    when (timeBlock) {
      is Day ->
        listOf(
          TimeBlockLabelComponent.Subtitle(timeBlock.date.dayOfMonth.toString()),
          TimeBlockLabelComponent.Title(labelFormatters.weekday.format(timeBlock.date)),
          TimeBlockLabelComponent.Subtitle(labelFormatters.month.format(timeBlock.date)),
        )
      is Week ->
        listOf(
          TimeBlockLabelComponent.Subtitle(labelFormatters.week.format(timeBlock.yearWeek.week)),
          TimeBlockLabelComponent.Title(timeBlock.start.dayOfMonth.toString()),
          TimeBlockLabelComponent.Divider,
          TimeBlockLabelComponent.Title(timeBlock.endInclusive.dayOfMonth.toString()),
          TimeBlockLabelComponent.Subtitle(labelFormatters.month.format(timeBlock.start)),
        )
      is Month ->
        listOf(TimeBlockLabelComponent.Title(labelFormatters.month.format(timeBlock.start)))
      else ->
        listOf(
          TimeBlockLabelComponent.Title(timeBlock.start.dayOfMonth.toString()),
          TimeBlockLabelComponent.Subtitle(labelFormatters.month.format(timeBlock.start)),
          TimeBlockLabelComponent.Divider,
          TimeBlockLabelComponent.Title(timeBlock.endInclusive.dayOfMonth.toString()),
          TimeBlockLabelComponent.Subtitle(labelFormatters.month.format(timeBlock.endInclusive)),
        )
    }

  fun formatHeading(
    timeBlock: TimeBlock,
    today: LocalDate? = LocalDate.now(),
  ): TimeBlockHeading {
    val main =
      when (timeBlock) {
        is Day -> {
          val isThisYear = timeBlock.date.year == today?.year
          val formatter = if (isThisYear) headingFormatters.day else headingFormatters.dayWithYear
          formatter.format(timeBlock.date)
        }

        is Week -> formatDateRange(timeBlock, today, headingFormatters)
        is Month -> formatMonth(timeBlock, today, headingFormatters)
        else -> format(timeBlock, today, headingFormatters)
      }
    val subtitle =
      when (timeBlock) {
        is Day ->
          when (timeBlock.date) {
            today -> headingFormatters.today
            today?.plusDays(1) -> headingFormatters.tomorrow
            today?.minusDays(1) -> headingFormatters.yesterday
            else -> null
          }

        is Week -> {
          val thisWeek = today?.let { YearWeek.from(it) }
          when (timeBlock.yearWeek) {
            thisWeek -> headingFormatters.thisWeek
            thisWeek?.plusWeeks(1) -> headingFormatters.nextWeek
            thisWeek?.minusWeeks(1) -> headingFormatters.lastWeek
            else -> null
          }
        }

        is Month -> {
          val thisMonth = today?.let { YearMonth.from(it) }
          when (timeBlock.yearMonth) {
            thisMonth -> headingFormatters.thisMonth
            else -> null
          }
        }

        else -> null
      }
    return TimeBlockHeading(main, subtitle)
  }

  private fun format(
    timeBlock: TimeBlock,
    today: LocalDate? = LocalDate.now(),
    formatters: HeadingFormatters,
  ): String =
    when (timeBlock) {
      is Day -> formatDay(timeBlock, today, formatters)
      is Week -> formatWeek(timeBlock, today, formatters)
      is Month -> formatMonth(timeBlock, today, formatters)
      is TimeUnitInstanceSequence -> {
        when {
          timeBlock.startBlock == timeBlock.endBlock ->
            format(timeBlock.startBlock, today, formatters)
          timeBlock.unit == TimeUnit.WEEK -> formatDateRange(timeBlock, today, formatters)
          else ->
            format(timeBlock.startBlock, today, formatters) +
              format(timeBlock.endBlock, today, formatters)
        }
      }
      else -> formatDateRange(timeBlock, today, formatters)
    }

  private fun formatDay(day: Day, today: LocalDate?, formatters: HeadingFormatters) =
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

  private fun formatWeek(week: Week, today: LocalDate?, formatters: HeadingFormatters): String {
    val thisWeek = today?.let { YearWeek.from(it) }
    return when (week.yearWeek) {
      thisWeek -> formatters.thisWeek
      thisWeek?.plusWeeks(1) -> formatters.nextWeek
      thisWeek?.minusWeeks(1) -> formatters.lastWeek
      else -> formatDateRange(week, today, formatters)
    }
  }

  private fun formatDateRange(
    range: DateRange,
    today: LocalDate?,
    formatters: HeadingFormatters,
  ): String {
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

  private fun formatMonth(month: Month, today: LocalDate?, formatters: HeadingFormatters): String {
    val useYear = month.yearMonth.year == today?.year
    val formatter = if (useYear) formatters.month else formatters.monthWithYear
    return formatter.format(month.yearMonth)
  }
}

private data class LabelFormatters(
  val weekday: DateTimeFormatter,
  val month: DateTimeFormatter,
  val week: String,
) {
  companion object {
    fun fromResources(resources: Resources): LabelFormatters {
      val locale = resources.configuration.locales[0]
      return LabelFormatters(
        weekday = DateTimeFormatter.ofPattern("E", locale),
        month = DateTimeFormatter.ofPattern("MMM", locale),
        week = resources.getString(R.string.format_week_label),
      )
    }
  }
}

private data class HeadingFormatters(
  val yesterday: String,
  val today: String,
  val tomorrow: String,
  val lastWeek: String,
  val thisWeek: String,
  val nextWeek: String,
  val thisMonth: String,
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
        HeadingFormatters(
          yesterday = getString(R.string.yesterday),
          today = getString(R.string.today),
          tomorrow = getString(R.string.tomorrow),
          lastWeek = getString(R.string.last_week),
          thisWeek = getString(R.string.this_week),
          nextWeek = getString(R.string.next_week),
          thisMonth = getString(R.string.this_month),
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
  }
}

private fun Resources.formatter(@StringRes id: Int) = DateTimeFormatter.ofPattern(getString(id))
