package com.robinwersich.todue.ui.screens.main

import java.time.LocalDate

sealed interface MainScreenEvent

data object AddTask : MainScreenEvent

data class ExpandTask(val taskId: Long) : MainScreenEvent

data object CollapseTasks : MainScreenEvent

data class ModifyTask(val event: ModifyTaskEvent, val taskId: Long) : MainScreenEvent

sealed interface ModifyTaskEvent {
  data class SetDone(val done: Boolean) : ModifyTaskEvent
  data class SetText(val text: String) : ModifyTaskEvent
  data class SetDueDate(val date: LocalDate) : ModifyTaskEvent
  data object Delete : ModifyTaskEvent
}
