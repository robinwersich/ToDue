package com.robinwersich.todue.domain.model

import java.time.LocalDate

data class Task(
  val id: Long = 0,
  val text: String,
  val scheduledBlock: TimelineBlock,
  val dueDate: LocalDate,
  val doneDate: LocalDate? = null,
)

val Task.isDone
  get() = doneDate != null
