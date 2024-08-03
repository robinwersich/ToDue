package com.robinwersich.todue.domain.model

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class DateRangeTest {
  @Test
  fun `duration includes start and end day`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    assertEquals(10, dateRange.duration)
  }

  @Test
  fun `duration is 1 for single day range`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 1)
    assertEquals(1, dateRange.duration)
    assertFalse(dateRange.isEmpty())
  }

  @Test
  fun `DateRange and derived DateTimeRange have same duration`() {
    val dateRange = LocalDate.of(2020, 1, 1)..LocalDate.of(2020, 1, 10)
    val dateTimeRange = dateRange.toDateTimeRange()
    assertEquals(dateRange.duration.toDouble(), dateTimeRange.duration, 0.0)
  }

  @Test
  fun `daysUntil returns correct number of days`() {
    assertEquals(9, LocalDate.of(2020, 1, 1).daysUntil(LocalDate.of(2020, 1, 10)))
  }
}
