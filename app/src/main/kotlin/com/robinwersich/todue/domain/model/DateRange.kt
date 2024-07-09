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
