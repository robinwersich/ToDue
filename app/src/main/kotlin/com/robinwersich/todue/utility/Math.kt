package com.robinwersich.todue.utility

import kotlin.math.max
import kotlin.math.min

fun interpolateDouble(start: Double, end: Double, progress: Float) =
  start * (1 - progress) + end * progress

fun Double.interpolateTo(target: Double, progress: Float) =
  interpolateDouble(this, target, progress)

fun interpolateDoubleRange(
  startRange: ClosedRange<Double>,
  endRange: ClosedRange<Double>,
  progress: Float,
): ClosedRange<Double> {
  val start = interpolateDouble(startRange.start, endRange.start, progress)
  val end = interpolateDouble(startRange.endInclusive, endRange.endInclusive, progress)
  return start..end
}

fun ClosedRange<Double>.interpolateTo(
  target: ClosedRange<Double>,
  progress: Float,
): ClosedRange<Double> = interpolateDoubleRange(this, target, progress)

operator fun <T : Comparable<T>> ClosedRange<T>.contains(other: ClosedRange<T>) =
  start <= other.start && endInclusive >= other.endInclusive

infix fun <T : Comparable<T>> ClosedRange<T>.overlapsWith(other: ClosedRange<T>) =
  start <= other.endInclusive && endInclusive >= other.start

infix fun <T : Comparable<T>> ClosedRange<T>.intersection(other: ClosedRange<T>) =
  maxOf(start, other.start)..minOf(endInclusive, other.endInclusive)

infix fun ClosedRange<Double>.intersection(other: ClosedRange<Double>) =
  max(start, other.start)..min(endInclusive, other.endInclusive)

infix fun <T : Comparable<T>> ClosedRange<T>.union(other: ClosedRange<T>) =
  minOf(start, other.start)..maxOf(endInclusive, other.endInclusive)

fun <T : Comparable<T>, R : Comparable<R>> ClosedRange<T>.map(transform: (T) -> R) =
  transform(start)..transform(endInclusive)
