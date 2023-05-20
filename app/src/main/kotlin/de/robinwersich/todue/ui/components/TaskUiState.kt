package de.robinwersich.todue.ui.components

import androidx.compose.runtime.Immutable
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate

@Immutable
data class TaskUiState(
  val id: Int,
  val text: String,
  val dueDate: LocalDate,
  val doneDate: LocalDate? = null,
  val expanded: Boolean = false,
)

fun Task.toUiState(expanded: Boolean = false): TaskUiState =
  TaskUiState(id, text, dueDate, doneDate, expanded)
