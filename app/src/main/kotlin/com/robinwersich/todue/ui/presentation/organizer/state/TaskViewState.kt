package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import com.robinwersich.todue.domain.model.TimelineBlock

enum class FocusLevel {
  NEUTRAL,
  FOCUSSED,
  BACKGROUND,
}

@Immutable
data class TaskViewState(
  val id: Long = 0,
  val text: String = "",
  val timelineBlock: TimelineBlock? = null,
  val dueDate: LocalDate = LocalDate.now(),
  val doneDate: LocalDate? = null,
  val focusLevel: FocusLevel = FocusLevel.NEUTRAL,
)
