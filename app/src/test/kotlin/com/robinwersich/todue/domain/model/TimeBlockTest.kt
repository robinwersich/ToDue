package com.robinwersich.todue.domain.model

import java.time.LocalDate
import java.time.YearMonth
import kotlin.test.assertFailsWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.extra.YearWeek

class TimeBlockTest {
  @Test
  fun `TimeBlock created from TimeUnit and LocalDate has correct start and end`() {
    val date = LocalDate.of(2020, 1, 1)

    val dayBlock = TimeUnit.DAY.instanceFrom(date)
    assertEquals(date, dayBlock.start)
    assertEquals(date, dayBlock.endInclusive)

    val weekBlock = TimeUnit.WEEK.instanceFrom(date)
    assertEquals(LocalDate.of(2019, 12, 30), weekBlock.start)
    assertEquals(LocalDate.of(2020, 1, 5), weekBlock.endInclusive)

    val monthBlock = TimeUnit.MONTH.instanceFrom(date)
    assertEquals(LocalDate.of(2020, 1, 1), monthBlock.start)
    assertEquals(LocalDate.of(2020, 1, 31), monthBlock.endInclusive)
  }

  @Test
  fun `TimeBlock arithmetic works correctly`() {
    assertEquals(Day(2020, 1, 2), Day(LocalDate.of(2020, 1, 1)) + 1)
    assertEquals(Week(2020, 3), Week(YearWeek.of(2020, 1)) + 2)
    assertEquals(Month(2020, 4), Month(YearMonth.of(2020, 1)) + 3)
  }

  @Test
  fun `valid TimeBlock comparisons work correctly`() {
    assertTrue(Day(2020, 1, 1) < Day(2020, 1, 2))
    assertTrue(Week(2020, 1) == Week(2020, 1))
    assertTrue(Month(2020, 1) > Month(2019, 1))
  }

  @Test
  fun `incompatible TimeBlock comparisons throw exception`() {
    assertFailsWith<IllegalArgumentException> { Day(2020, 1, 1) < Week(2020, 1) }
  }

  @Test
  fun `TimeBlock range contains correct elements`() {
    val range = Week(2020, 1)..Week(2020, 3)
    val expectedBlocks = listOf(Week(2020, 1), Week(2020, 2), Week(2020, 3))
    assertEquals(expectedBlocks, range.toList())
  }
}
