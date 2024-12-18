package com.robinwersich.todue.domain.model

import com.google.common.truth.Truth.assertThat
import com.robinwersich.todue.utility.size
import java.time.LocalDate
import org.junit.Test

class DateRangeTest {
  @Test
  fun `duration includes start and end day`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    assertThat(dateRange.size).isEqualTo(10)
  }

  @Test
  fun `duration is 1 for single day range`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 1)
    assertThat(dateRange.size).isEqualTo(1)
    assertThat(dateRange.isEmpty()).isFalse()
  }

  @Test
  fun `DateRange and derived DateTimeRange have same duration`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    val dateTimeRange = dateRange.toDoubleRange()
    assertThat(dateTimeRange.size).isEqualTo(dateRange.size.toDouble())
  }

  @Test
  fun `daysUntil returns correct number of days`() {
    assertThat(LocalDate.of(2020, 1, 1).daysUntil(LocalDate.of(2020, 1, 10))).isEqualTo(9)
  }

  @Test
  fun `empty DateRange contains no elements`() {
    val range = LocalDate.of(2020, 1, 1)..LocalDate.of(2019, 12, 31)
    assertThat(range.toList()).isEmpty()
  }

  @Test
  fun `DateRange with one day contains correct element`() {
    val range = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 1)
    assertThat(range.toList()).containsExactly(LocalDate.of(2020, 1, 1))
  }

  @Test
  fun `DateRange with more than one day contains correct elements`() {
    val range = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 3)
    assertThat(range.toList())
      .containsExactly(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 3))
      .inOrder()
  }
}
