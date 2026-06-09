package com.robinwersich.todue.domain.model

import java.time.Duration
import java.time.LocalDate

data class Task(
  val id: Long = 0,
  val text: String,
  val scheduledBlock: TimelineBlock,
  val dueDate: LocalDate,
  val estimatedDuration: Duration = Duration.ofMinutes(30),
  val doneDate: LocalDate? = null,
)

val Task.isDone
  get() = doneDate != null
