package de.robinwersich.todue.ui.components

import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate

data class TaskUiState(
  val id: Int,
  val text: String,
  val dueDate: LocalDate,
  val doneDate: LocalDate? = null,
)

fun Task.toUiState(): TaskUiState = TaskUiState(id, text, dueDate, doneDate)
