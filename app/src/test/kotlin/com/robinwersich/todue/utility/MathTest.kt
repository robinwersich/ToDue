package com.robinwersich.todue.utility

import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class MathTest {
  class Numbers {
    @Test
    fun `interpolateDouble interpolates correctly`() {
      assertEquals(0.0, interpolateDouble(0.0, 1.0, 0.0f), 0.0)
      assertEquals(0.5, interpolateDouble(0.0, 1.0, 0.5f), 0.0)
      assertEquals(1.0, interpolateDouble(0.0, 1.0, 1.0f), 0.0)
      assertEquals(2.0, interpolateDouble(1.0, 5.0, 0.25f), 0.0)
    }

    @Test
    fun `interpolateDoubleRange interpolates correctly`() {
      val startRange = 0.0..1.0
      val endRange = 1.0..5.0
      assertEquals(0.0..1.0, interpolateDoubleRange(startRange, endRange, 0.0f))
      assertEquals(0.5..3.0, interpolateDoubleRange(startRange, endRange, 0.5f))
      assertEquals(1.0..5.0, interpolateDoubleRange(startRange, endRange, 1.0f))
    }
  }

  class ClosedRanges {
    @Test
    fun `contains returns true for contained range`() {
      assertTrue(1..5 in 0..10)
    }

    @Test
    fun `contains returns true for same range`() {
      assertTrue(0..10 in 0..10)
    }

    @Test
    fun `contains returns false for overlapping range`() {
      assertFalse(5..15 in 0..10)
    }

    @Test
    fun `contains returns false for non-overlapping range`() {
      assertFalse(10..15 in 0..5)
    }

    @Test
    fun `overlapsWith returns true for contained range`() {
      assertTrue(1..5 overlapsWith 0..10)
    }

    @Test
    fun `overlapsWith returns true for same range`() {
      assertTrue(0..10 overlapsWith 0..10)
    }

    @Test
    fun `overlapsWith returns true for overlapping range`() {
      assertTrue(5..15 overlapsWith 0..10)
    }

    @Test
    fun `overlapsWith returns false for non-overlapping range`() {
      assertFalse(10..15 overlapsWith 0..5)
    }

    @Test
    fun `overlapsWith returns true for adjacent range`() {
      assertTrue(0.0..5.0 overlapsWith 5.0..10.0)
    }

    @Test
    fun `intersection with contained range returns contained range`() {
      assertEquals(1..5, 1..5 intersection 0..10)
    }

    @Test
    fun `intersection with overlapping range returns correct intersection`() {
      assertEquals(5..10, 5..15 intersection 0..10)
    }

    @Test
    fun `intersection with same range returns this range`() {
      assertEquals(0..10, 0..10 intersection 0..10)
    }

    @Test
    fun `intersection with non-overlapping range returns empty range`() {
      assertTrue((10..15 intersection 0..5).isEmpty())
    }

    @Test
    fun `union with contained range returns containing range`() {
      assertEquals(0..10, 1..5 union 0..10)
    }

    @Test
    fun `union with overlapping range returns correct union`() {
      assertEquals(0..15, 5..15 union 0..10)
    }

    @Test
    fun `union with same range returns this range`() {
      assertEquals(0..10, 0..10 union 0..10)
    }

    @Test
    fun `union with non-overlapping range returns containing range`() {
      assertEquals(0..15, 10..15 union 0..5)
    }

    @Test
    fun `map transforms range correctly`() {
      val range = 1..10
      val mappedRange = range.map { it * 2.0 }
      assertEquals(2.0..20.0, mappedRange)
    }
  }
}

/**
 * Asserts that the given ranges are equal. This is necessary because the [equals] implementation of
 * different [ClosedRanges][ClosedRange] is type sensitive, meaning that an [IntRange] and a
 * [ClosedRange<Int>] with the same bounds are not considered equal.
 */
private fun <T : Comparable<T>> assertEquals(expected: ClosedRange<T>, actual: ClosedRange<T>) {
  assertEquals("Range starts differ.", expected.start, actual.start)
  assertEquals("Range ends differ.", expected.endInclusive, actual.endInclusive)
}
