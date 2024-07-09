package com.robinwersich.todue.ui.presentation.organizer

import com.robinwersich.todue.domain.model.TimeBlock
import java.time.LocalDate

sealed interface OrganizerEvent

data object AddTask : OrganizerEvent

data class ExpandTask(val taskId: Long) : OrganizerEvent

data object CollapseTasks : OrganizerEvent

data class ModifyTask(val event: ModifyTaskEvent, val taskId: Long) : OrganizerEvent

sealed interface ModifyTaskEvent {
  data class SetDone(val done: Boolean) : ModifyTaskEvent

  data class SetText(val text: String) : ModifyTaskEvent

  data class SetTimeBlock(val timeBlock: TimeBlock) : ModifyTaskEvent

  data class SetDueDate(val date: LocalDate) : ModifyTaskEvent

  data object Delete : ModifyTaskEvent
}
