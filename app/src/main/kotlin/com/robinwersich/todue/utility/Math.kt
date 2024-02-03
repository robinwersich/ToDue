package com.robinwersich.todue.utility

fun Double.interpolateTo(target: Double, progress: Float) =
  this * (1 - progress) + target * progress

fun ClosedFloatingPointRange<Double>.interpolateTo(
  target: ClosedFloatingPointRange<Double>,
  progress: Float
): ClosedFloatingPointRange<Double> {
  val start = this.start.interpolateTo(target.start, progress)
  val end = this.endInclusive.interpolateTo(target.endInclusive, progress)
  return start..end
}
