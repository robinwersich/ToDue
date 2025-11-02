package com.robinwersich.todue.ui.presentation.organizer

import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import java.time.LocalDate

sealed interface OrganizerEvent {
  data class AddTask(val timelineBlock: TimelineBlock) : OrganizerEvent

  data class ExpandTask(val taskId: Long) : OrganizerEvent

  data object CollapseTasks : OrganizerEvent

  data class ForTask(val taskId: Long, val event: TaskEvent) : OrganizerEvent
}

sealed interface TaskEvent {
  data class SetDone(val done: Boolean) : TaskEvent

  data class SetText(val text: String) : TaskEvent

  data class SetTimeBlock(val timeBlock: TimeBlock) : TaskEvent

  data class SetDueDate(val date: LocalDate) : TaskEvent

  data object Delete : TaskEvent
}
