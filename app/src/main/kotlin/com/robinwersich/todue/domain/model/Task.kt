package com.robinwersich.todue.domain.model

import java.time.Duration
import java.time.LocalDate

data class Task(
  val id: Long = 0,
  val text: String,
  val scheduledTimelineRange: TimelineRange,
  val dueDate: LocalDate,
  val estimatedDuration: Duration,
  val doneDate: LocalDate? = null,
)
