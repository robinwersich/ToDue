package com.robinwersich.todue.domain.model

import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class Task(
  val id: Long = 0,
  val text: String,
  val scheduledBlock: TimelineBlock,
  val dueDate: LocalDate,
  val estimatedDuration: Duration = 30.minutes,
  val doneDate: LocalDate? = null,
)

val Task.isDone
  get() = doneDate != null
