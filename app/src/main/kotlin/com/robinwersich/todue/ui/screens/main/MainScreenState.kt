package com.robinwersich.todue.ui.screens.main

import com.robinwersich.todue.ui.components.TaskState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface TaskPropertyOverlay {
  val taskId: Long
  data class DueDateOverlay(override val taskId: Long) : TaskPropertyOverlay
}

data class MainScreenState(
  val tasks: ImmutableList<TaskState> = persistentListOf(),
  val taskPropertyOverlay: TaskPropertyOverlay? = null,
)
