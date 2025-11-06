package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.runtime.Immutable
import com.robinwersich.todue.domain.model.TimelineBlock
import java.time.LocalDate

enum class FocusLevel {
  /** Collapsed, can be interacted with */
  NEUTRAL,
  /** Expanded, can be interacted with */
  FOCUSSED,
  /** Collapsed, cannot be interacted with (because other task is focussed) */
  BACKGROUND;

  val isFocussed
    get() = this == FOCUSSED
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
