package com.robinwersich.todue.domain.model

import java.time.LocalDate

/**
 * Represents an infinite series of [TimeBlock]s with a specific [TimeUnit]. A [Timeline] is
 * compared to other timelines by the size of their [TimeUnit]s.
 *
 * @param id The unique identifier of this timeline (used for DB access).
 * @param timeUnit The time unit of this timelines [TimeBlock]s.
 */
data class Timeline(val id: Int, val timeUnit: TimeUnit) : Comparable<Timeline> {
  override operator fun compareTo(other: Timeline) =
    timeUnit.referenceSize.compareTo(other.timeUnit.referenceSize)

  /** Creates a new [TimeBlock] with this timeline's [TimeUnit] from a [LocalDate]. */
  fun timeBlockFrom(date: LocalDate) = timeUnit.instanceFrom(date)

  override fun toString() = "Timeline($timeUnit)"
}
