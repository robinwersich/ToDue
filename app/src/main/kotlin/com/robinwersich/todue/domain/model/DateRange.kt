package com.robinwersich.todue.domain.model

import java.time.LocalDate

/** A range of dates with inclusive start and end. */
typealias DateRange = ClosedRange<LocalDate>

/**
 * Returns how many days this range spans. Note that a [DateRange] is assumed to last from the start
 * of the first day to the end of the last day, so a range from 2021-01-01 to 2021-01-01 has a size
 * of 1.
 */
val DateRange.duration: Long
  get() = endInclusive.toEpochDay() + 1 - start.toEpochDay()

/**
 * Converts a [DateRange] to a [Double] range, representing epoch days. This is useful for
 * interpolating. Note that a [DateRange] of a single day, thus having a size of 0, is equivalent to
 * a [Double] range from the start of the day to the end of the day, thus having a size of 1.
 */
fun DateRange.toDoubleRange() =
  start.toEpochDay().toDouble()..(endInclusive.toEpochDay() + 1).toDouble()

/** Returns the (signed) number of days from this date until the other date. */
fun LocalDate.daysUntil(other: LocalDate) = other.toEpochDay() - this.toEpochDay()

/** A sequence of dates from [start] to [endInclusive]. */
class DateSequence(override val start: LocalDate, override val endInclusive: LocalDate) :
  DateRange, Sequence<LocalDate> {

  override fun iterator(): Iterator<LocalDate> =
    object : Iterator<LocalDate> {
      private var next: LocalDate? = if (start <= endInclusive) start else null

      override fun hasNext() = next != null

      override fun next(): LocalDate {
        next?.let {
          next = if (it == endInclusive) null else it.plusDays(1)
          return it
        }
        throw NoSuchElementException()
      }
    }

  override fun equals(other: Any?) =
    other is DateSequence &&
      (isEmpty() && other.isEmpty() || start == other.start && endInclusive == other.endInclusive)

  override fun hashCode() = if (isEmpty()) -1 else 31 * start.hashCode() + endInclusive.hashCode()

  override fun toString() = "$start..$endInclusive"
}

/** Returns a [DateRange] from [this] to [other]. */
operator fun LocalDate.rangeTo(other: LocalDate) = DateSequence(this, other)
