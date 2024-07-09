package com.robinwersich.todue.domain.model

import java.time.LocalDate

data class Timeline(val id: Int, val timeUnit: TimeUnit) : Comparable<Timeline> {
  override operator fun compareTo(other: Timeline) = timeUnit.compareTo(other.timeUnit)

  fun timeBlockFrom(date: LocalDate) = timeUnit.instanceFrom(date)
}
