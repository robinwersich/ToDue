package com.robinwersich.todue.domain.model

import java.time.LocalDate

/**
 * Represents an infinite series of [TimeBlock]s with a specific [TimeUnit].
 *
 * @param timeUnit The time unit of this timelines [TimeBlock]s.
 */
data class Timeline(val timeUnit: TimeUnit) {
  val blockSize
    get() = timeUnit.referenceSize

  /** Creates a new [TimeBlock] with this timeline's [TimeUnit] from a [LocalDate]. */
  fun timeBlockFrom(date: LocalDate) = timeUnit.instanceFrom(date)
}
