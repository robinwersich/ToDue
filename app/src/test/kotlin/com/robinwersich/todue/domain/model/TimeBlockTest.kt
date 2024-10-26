package com.robinwersich.todue.domain.model

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlin.test.assertFailsWith
import org.junit.Test

class TimeBlockTest {
  @Test
  fun `TimeBlock created from TimeUnit and LocalDate has correct start and end`() {
    val date = LocalDate.of(2020, 1, 1)

    val dayBlock = TimeUnit.DAY.instanceFrom(date)
    assertThat(dayBlock.start).isEqualTo(date)
    assertThat(dayBlock.endInclusive).isEqualTo(date)

    val weekBlock = TimeUnit.WEEK.instanceFrom(date)
    assertThat(weekBlock.start).isEqualTo(LocalDate.of(2019, 12, 30))
    assertThat(weekBlock.endInclusive).isEqualTo(LocalDate.of(2020, 1, 5))

    val monthBlock = TimeUnit.MONTH.instanceFrom(date)
    assertThat(monthBlock.start).isEqualTo(LocalDate.of(2020, 1, 1))
    assertThat(monthBlock.endInclusive).isEqualTo(LocalDate.of(2020, 1, 31))
  }

  @Test
  fun `TimeBlock arithmetic works correctly`() {
    assertThat(Day(2020, 1, 1) + 1).isEqualTo(Day(2020, 1, 2))
    assertThat(Week(2020, 1) + 2).isEqualTo(Week(2020, 3))
    assertThat(Month(2020, 1) + 3).isEqualTo(Month(2020, 4))
  }

  @Test
  fun `TimeBlock comparisons with same unit work correctly`() {
    assertThat(Day(2020, 1, 1) < Day(2020, 1, 2)).isTrue()
    assertThat(Week(2020, 1) > Week(2019, 52)).isTrue()
    assertThat(Month(2020, 1) >= Month(2020, 1)).isTrue()
  }

  @Test
  fun `TimeBlock comparisons with different units work as expected`() {
    assertThat(Day(2020, 1, 1) < Week(2020, 2)).isTrue()
    assertThat(Week(2020, 2) < Month(2020, 1)).isFalse()
    assertThat(Week(2020, 2) > Month(2020, 1)).isFalse()
    assertThat(Week(2020, 6) > Month(2020, 1)).isTrue()
  }

  @Test
  fun `empty TimeBlock range contains no elements`() {
    val range = Day(2020, 1, 1)..Day(2019, 12, 31)
    assertThat(range.toList()).isEmpty()
  }

  @Test
  fun `TimeBlock range with one block contains correct element`() {
    val range = Week(2020, 1)..Week(2020, 1)
    assertThat(range.toList()).containsExactly(Week(2020, 1))
  }

  @Test
  fun `TimeBlock range with more than one block contains correct elements`() {
    val range = Week(2020, 1)..Week(2020, 3)
    assertThat(range.toList())
      .containsExactly(Week(2020, 1), Week(2020, 2), Week(2020, 3))
      .inOrder()
  }

  @Test
  fun `creating TimeUnitInstanceSequence with different units throws exception`() {
    val day = Day(2020, 1, 1)
    val week = Week(2020, 2)

    assertFailsWith<IllegalArgumentException> { day..week }
  }
}
