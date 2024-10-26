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

val ClosedRange<Double>.size: Double
  get() = endInclusive - start

val ClosedRange<Double>.center: Double
  get() = (start + endInclusive) / 2

fun <T : Comparable<T>, R : Comparable<R>> ClosedRange<T>.mapBounds(transform: (T) -> R) =
  transform(start)..transform(endInclusive)

/** Returns true if start and end of [other] are both contained in this range. */
operator fun <T : Comparable<T>> ClosedRange<T>.contains(other: ClosedRange<T>) =
  start <= other.start && endInclusive >= other.endInclusive

/** Returns true if there is at least one value contained in both ranges. */
infix fun <T : Comparable<T>> ClosedRange<T>.overlapsWith(other: ClosedRange<T>) =
  start <= other.endInclusive && endInclusive >= other.start

/** Returns the range of values contained in both ranges. */
infix fun <T : Comparable<T>> ClosedRange<T>.intersection(other: ClosedRange<T>) =
  maxOf(start, other.start)..minOf(endInclusive, other.endInclusive)

/** Returns the smallest range that contains all values of both ranges. */
infix fun <T : Comparable<T>> ClosedRange<T>.union(other: ClosedRange<T>) =
  minOf(start, other.start)..maxOf(endInclusive, other.endInclusive)

// --- overloads for primitive ranges ---

/** @see intersection(other: ClosedRange<T>) */
@JvmName("intersectionDouble")
infix fun ClosedRange<Double>.intersection(other: ClosedRange<Double>) =
  max(start, other.start)..min(endInclusive, other.endInclusive)

/** @see intersection(other: ClosedRange<T>) */
@JvmName("intersectionInt")
infix fun ClosedRange<Int>.intersection(other: ClosedRange<Int>) =
  max(start, other.start)..min(endInclusive, other.endInclusive)

/** @see intersection(other: ClosedRange<T>) */
@JvmName("intersectionLong")
infix fun ClosedRange<Long>.intersection(other: ClosedRange<Long>) =
  max(start, other.start)..min(endInclusive, other.endInclusive)

/** @see intersection(other: ClosedRange<T>) */
@JvmName("intersectionChar")
infix fun ClosedRange<Char>.intersection(other: ClosedRange<Char>) =
  maxOf(start, other.start)..minOf(endInclusive, other.endInclusive)

/** @see union(other: ClosedRange<T>) */
@JvmName("unionFloat")
infix fun ClosedRange<Float>.union(other: ClosedRange<Float>) =
  min(start, other.start)..max(endInclusive, other.endInclusive)

/** @see union(other: ClosedRange<T>) */
@JvmName("unionDouble")
infix fun ClosedRange<Double>.union(other: ClosedRange<Double>) =
  min(start, other.start)..max(endInclusive, other.endInclusive)

/** @see union(other: ClosedRange<T>) */
@JvmName("unionInt")
infix fun ClosedRange<Int>.union(other: ClosedRange<Int>) =
  min(start, other.start)..max(endInclusive, other.endInclusive)

/** @see union(other: ClosedRange<T>) */
@JvmName("unionLong")
infix fun ClosedRange<Long>.union(other: ClosedRange<Long>) =
  min(start, other.start)..max(endInclusive, other.endInclusive)

/** @see union(other: ClosedRange<T>) */
@JvmName("unionChar")
infix fun ClosedRange<Char>.union(other: ClosedRange<Char>) =
  minOf(start, other.start)..maxOf(endInclusive, other.endInclusive)
