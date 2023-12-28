package com.robinwersich.todue.domain.model

import java.time.LocalDate

/**
 * Represents a thing that needs to be done.
 *
 * @property scheduledTimeBlock Describes when the task is currently scheduled to be done.
 */
data class Task(
  val id: Long = 0,
  val text: String,
  val scheduledTimeBlock: TimeBlock,
  val dueDate: LocalDate,
  val doneDate: LocalDate? = null
)
