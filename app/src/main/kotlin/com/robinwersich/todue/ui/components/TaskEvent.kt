package com.robinwersich.todue.ui.components

import com.robinwersich.todue.ui.screens.main.MainScreenEvent

sealed interface TaskListEvent : MainScreenEvent {
  data object AddTask : TaskListEvent
  data class ExpandTask(val taskId: Long) : TaskListEvent
  data object CollapseTasks : TaskListEvent
  data class ModifyTask(val event: TaskModifyEvent, val taskId: Long) : TaskListEvent
}

sealed interface TaskModifyEvent {
  data class SetDone(val done: Boolean) : TaskModifyEvent
  data class SetText(val text: String) : TaskModifyEvent
  data object Delete : TaskModifyEvent
}
