package com.robinwersich.todue.ui.presentation.organizer.snapping

import kotlin.math.abs
import kotlin.math.sign

/** Range of offsets (relative to current position) that put an item into a settled position */
class SnapRange(override val start: Float, override val endInclusive: Float) :
  ClosedFloatingPointRange<Float> {

  /** Minimum (by absolute value) scroll offset to snap an item into this range. */
  val snapOffset =
    if (start.sign != endInclusive.sign) 0f
    else if (abs(start) < abs(endInclusive)) start else endInclusive

  override fun lessThanOrEquals(a: Float, b: Float) = a <= b
}
