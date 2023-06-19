package de.robinwersich.todue.ui.components

import androidx.compose.runtime.Immutable
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate

@Immutable
data class TaskState(
  val id: Int = 0,
  val text: String = "",
  val dueDate: LocalDate = LocalDate.now(),
  val doneDate: LocalDate? = null,
  val expanded: Boolean = false,
)

fun Task.toUiState(expanded: Boolean = false): TaskState =
  TaskState(id, text, dueDate, doneDate, expanded)
