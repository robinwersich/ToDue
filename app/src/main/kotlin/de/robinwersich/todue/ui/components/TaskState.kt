package de.robinwersich.todue.ui.components

import androidx.compose.runtime.Immutable
import java.time.LocalDate

enum class TaskFocusLevel {
  NEUTRAL,
  FOCUSSED,
  BACKGROUND
}

@Immutable
data class TaskState(
  val id: Long = 0,
  val text: String = "",
  val dueDate: LocalDate = LocalDate.now(),
  val doneDate: LocalDate? = null,
  val focusLevel: TaskFocusLevel = TaskFocusLevel.NEUTRAL,
)
