package com.robinwersich.todue.domain.model

import java.time.LocalDate

/**
 * Represents an infinite series of [TimeBlock]s with a specific [TimeUnit].
 *
 * @param id The identifier of this timeline, which also determines its position in the organizer.
 * @param timeUnit The time unit of this timelines [TimeBlock]s.
 */
data class Timeline(val id: Long, val timeUnit: TimeUnit) : Comparable<Timeline> {
  val blockSize
    get() = timeUnit.referenceSize

  /** Creates a new [TimeBlock] with this timeline's [TimeUnit] from a [LocalDate]. */
  fun timeBlockFrom(date: LocalDate) = timeUnit.instanceFrom(date)

  override fun compareTo(other: Timeline) =
    compareBy<Timeline>({ it.id }, { it.timeUnit }).compare(this, other)
}
