package com.robinwersich.todue.ui.presentation.organizer.formatting

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.common.truth.Truth.assertThat
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Week
import java.time.LocalDate
import java.util.Locale
import org.junit.Rule
import org.junit.Test

class TimeBlockFormatterTest {
  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val today = LocalDate.of(2020, 6, 1)

  private fun getFormatter(locale: Locale): TimeBlockFormatter {
    // update locale for date formatters
    Locale.setDefault(locale)
    // update locale for app resources
    val config = rule.activity.resources.configuration
    config.setLocale(locale)
    val localizedContext = rule.activity.baseContext.createConfigurationContext(config)
    return TimeBlockFormatter(localizedContext.resources)
  }

  private fun assertFormats(
    timeBlock: TimeBlock,
    expected: String,
    locale: Locale = Locale.US,
    today: LocalDate? = null,
  ) {
    val formatter = getFormatter(locale)
    assertThat(formatter.format(timeBlock, today)).isEqualTo(expected)
  }

  @Test fun format_Day() = assertFormats(Day(2020, 1, 1), "Wed, Jan 1, 2020")

  @Test
  fun format_Day_DE() = assertFormats(Day(2020, 1, 1), "Mi, 1. Jan 2020", locale = Locale.GERMANY)

  @Test fun format_Day_Today() = assertFormats(Day(today), "Today", today = today)

  @Test fun format_Day_Tomorrow() = assertFormats(Day(today) + 1, "Tomorrow", today = today)

  @Test fun format_Week_ThisWeek() = assertFormats(Week(today), "This Week", today = today)

  @Test
  fun format_Week_DifferentYears() =
    assertFormats(Week(2020, 1), "Dec 30, 2019 – Jan 5, 2020", today = today)

  @Test fun format_Week_DifferentMonths() = assertFormats(Week(2020, 5), "Jan 27 – Feb 2, 2020")

  @Test
  fun format_Week_DifferentMonths_ThisYear() =
    assertFormats(Week(2020, 5), "Jan 27 – Feb 2", today = today)

  @Test fun format_Week_SameMonth() = assertFormats(Week(2020, 2), "Jan 6 – 12, 2020")

  @Test
  fun format_Week_SameMonth_DE() =
    assertFormats(Week(2020, 2), "6. – 12. Jan 2020", locale = Locale.GERMANY)

  @Test
  fun format_Week_SameMonth_ThisYear() = assertFormats(Week(2020, 2), "Jan 6 – 12", today = today)

  @Test fun format_Month() = assertFormats(Month(2020, 1), "January 2020")

  @Test fun format_Month_ThisYear() = assertFormats(Month(2020, 12), "December", today = today)
}
