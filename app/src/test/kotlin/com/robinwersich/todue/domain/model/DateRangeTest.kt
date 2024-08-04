package com.robinwersich.todue.domain.model

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class DateRangeTest {
  @Test
  fun `duration includes start and end day`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    assertThat(dateRange.duration).isEqualTo(10)
  }

  @Test
  fun `duration is 1 for single day range`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 1)
    assertThat(dateRange.duration).isEqualTo(1)
    assertThat(dateRange.isEmpty()).isFalse()
  }

  @Test
  fun `DateRange and derived DateTimeRange have same duration`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    val dateTimeRange = dateRange.toDateTimeRange()
    assertThat(dateTimeRange.duration).isEqualTo(dateRange.duration.toDouble())
  }

  @Test
  fun `daysUntil returns correct number of days`() {
    assertThat(LocalDate.of(2020, 1, 1).daysUntil(LocalDate.of(2020, 1, 10))).isEqualTo(9)
  }
}
