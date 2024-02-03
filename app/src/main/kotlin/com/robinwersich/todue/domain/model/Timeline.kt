package com.robinwersich.todue.domain.model

import java.time.LocalDate

data class Timeline(val id: Int, val timeBlockUnit: TimeUnit) {
  val now
    get() = timeBlockUnit.instanceFrom(LocalDate.now())

  operator fun compareTo(other: Timeline) =
    timeBlockUnit.referenceSize.compareTo(other.timeBlockUnit.referenceSize)
}