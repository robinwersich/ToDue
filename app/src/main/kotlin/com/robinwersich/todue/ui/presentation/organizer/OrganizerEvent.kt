package com.robinwersich.todue.ui.presentation.organizer

import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock

sealed interface OrganizerEvent {
  data class AddTask(val timelineBlock: TimelineBlock) : OrganizerEvent

  data class DeleteTask(val taskId: Long) : OrganizerEvent

  data class UpdateTask(val task: Task) : OrganizerEvent

  data class SetTaskDone(val taskId: Long, val done: Boolean) : OrganizerEvent
}
