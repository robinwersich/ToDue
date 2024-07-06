package com.robinwersich.todue.domain.model

data class Timeline(val id: Int, val timeUnit: TimeUnit) : Comparable<Timeline> {
  override operator fun compareTo(other: Timeline) = timeUnit.compareTo(other.timeUnit)
}
