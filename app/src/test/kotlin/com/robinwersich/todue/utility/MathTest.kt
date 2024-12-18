package com.robinwersich.todue.utility

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

@RunWith(Enclosed::class)
class MathTest {
  class Numbers {
    @Test
    fun `interpolateDouble interpolates correctly`() {
      assertThat(interpolateDouble(0.0, 1.0, 0.0f)).isEqualTo(0.0)
      assertThat(interpolateDouble(0.0, 1.0, 0.5f)).isEqualTo(0.5)
      assertThat(interpolateDouble(0.0, 1.0, 1.0f)).isEqualTo(1.0)
      assertThat(interpolateDouble(1.0, 5.0, 0.25f)).isEqualTo(2.0)
    }

    @Test
    fun `relativeProgress returns correct value`() {
      assertThat(relativeProgress(0.0f, 1.0f, 0.3f)).isEqualTo(0.3f)
      assertThat(relativeProgress(0.0f, 0.5f, 0.2f)).isEqualTo(0.4f)
      assertThat(relativeProgress(0.3f, 0.7f, 0.4f)).isEqualTo(0.25f)
      assertThat(relativeProgress(0.5f, 0.8f, 0.3f)).isEqualTo(0f)
    }

    @Test
    fun `interpolateDoubleRange interpolates correctly`() {
      val startRange = 0.0..1.0
      val endRange = 1.0..5.0
      assertThat(interpolateDoubleRange(startRange, endRange, 0.0f)).isEqualTo(0.0..1.0)
      assertThat(interpolateDoubleRange(startRange, endRange, 0.5f)).isEqualTo(0.5..3.0)
      assertThat(interpolateDoubleRange(startRange, endRange, 1.0f)).isEqualTo(1.0..5.0)
    }
  }

  class ClosedRanges {
    @Test
    fun `contains returns true for contained range`() {
      assertThat(1..5 in 0..10).isTrue()
    }

    @Test
    fun `contains returns true for same range`() {
      assertThat(0..10 in 0..10).isTrue()
    }

    @Test
    fun `contains returns false for overlapping range`() {
      assertThat(5..15 in 0..10).isFalse()
    }

    @Test
    fun `contains returns false for non-overlapping range`() {
      assertThat(10..15 in 0..5).isFalse()
    }

    @Test
    fun `overlapsWith returns true for contained range`() {
      assertThat(1..5 overlapsWith 0..10).isTrue()
    }

    @Test
    fun `overlapsWith returns true for same range`() {
      assertThat(0..10 overlapsWith 0..10).isTrue()
    }

    @Test
    fun `overlapsWith returns true for overlapping range`() {
      assertThat(5..15 overlapsWith 0..10).isTrue()
    }

    @Test
    fun `overlapsWith returns false for non-overlapping range`() {
      assertThat(10..15 overlapsWith 0..5).isFalse()
    }

    @Test
    fun `overlapsWith returns true for adjacent range`() {
      assertThat(5..10 overlapsWith 10..15).isTrue()
    }

    @Test
    fun `intersection with contained range returns contained range`() {
      assertThat(1..5 intersection 0..10).isEqualTo(1..5)
    }

    @Test
    fun `intersection with overlapping range returns correct intersection`() {
      assertThat(5..15 intersection 0..10).isEqualTo(5..10)
    }

    @Test
    fun `intersection with same range returns this range`() {
      assertThat(0..10 intersection 0..10).isEqualTo(0..10)
    }

    @Test
    fun `intersection with non-overlapping range returns empty range`() {
      assertThat((10..15 intersection 0..5).isEmpty()).isTrue()
    }

    @Test
    fun `union with contained range returns containing range`() {
      assertThat(1..5 union 0..10).isEqualTo(0..10)
    }

    @Test
    fun `union with overlapping range returns correct union`() {
      assertThat(5..15 union 0..10).isEqualTo(0..15)
    }

    @Test
    fun `union with same range returns this range`() {
      assertThat(0..10 union 0..10).isEqualTo(0..10)
    }

    @Test
    fun `union with non-overlapping range returns containing range`() {
      assertThat(10..15 union 0..5).isEqualTo(0..15)
    }

    @Test
    fun `map transforms range correctly`() {
      val range = 0..10
      val mappedRange = range.mapBounds { it * 2 }
      // the mapped range is a [ComparableRange<Int>] while the original range is an [IntRange],
      // so they are not equal, but the bounds should be
      assertThat(mappedRange.start).isEqualTo(0)
      assertThat(mappedRange.endInclusive).isEqualTo(20)
    }
  }
}
