package com.robinwersich.todue.domain.model

import java.time.LocalDate

data class Task(
  val id: Long = 0,
  val text: String,
  val timeBlock: TimeBlock,
  val dueDate: LocalDate,
  val doneDate: LocalDate? = null
)
