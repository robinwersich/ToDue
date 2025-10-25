package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.runtime.Immutable
import com.robinwersich.todue.domain.model.TimeBlock
import java.time.LocalDate

enum class FocusLevel {
  NEUTRAL,
  FOCUSSED,
  BACKGROUND,
}

@Immutable
data class TaskViewState(
  val id: Long = 0,
  val text: String = "",
  val timeBlock: TimeBlock? = null,
  val dueDate: LocalDate = LocalDate.now(),
  val doneDate: LocalDate? = null,
  val focusLevel: FocusLevel = FocusLevel.NEUTRAL,
)
