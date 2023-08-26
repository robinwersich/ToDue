package com.robinwersich.todue.ui.components

import androidx.compose.runtime.Immutable
import com.robinwersich.todue.data.entities.TimeBlock
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
  val timeBlock: TimeBlock = TimeBlock.Day(),
  val dueDate: LocalDate = LocalDate.now(),
  val doneDate: LocalDate? = null,
  val focusLevel: TaskFocusLevel = TaskFocusLevel.NEUTRAL,
)
