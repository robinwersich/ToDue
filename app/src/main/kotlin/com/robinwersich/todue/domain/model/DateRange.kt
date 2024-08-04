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
 * A smoothly interpolatable range of date times in the form of a [Double] range, representing the
 * inclusive end points as fractional epoch days.
 */
typealias DateTimeRange = ClosedRange<Double>

/** Returns how many days this range spans. */
val DateTimeRange.duration: Double
  get() = endInclusive - start

/**
 * Converts a [DateRange] to a [DateTimeRange]. Note that a [DateRange] of a single day, thus having
 * a size of 0, is equivalent to a [DateTimeRange] from the start of the day to the end of the day,
 * thus having a size of 1.
 */
fun DateRange.toDateTimeRange() =
  start.toEpochDay().toDouble()..(endInclusive.toEpochDay() + 1).toDouble()

/** Returns the (signed) number of days from this date until the other date. */
fun LocalDate.daysUntil(other: LocalDate) = other.toEpochDay() - this.toEpochDay()

/** A sequence of dates from [start] to [endInclusive]. */
data class DateSequence(override val start: LocalDate, override val endInclusive: LocalDate) :
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
}

/** Returns a [DateRange] from [this] to [other]. */
operator fun LocalDate.rangeTo(other: LocalDate) = DateSequence(this, other)
