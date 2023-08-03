package com.robinwersich.todue.ui.screens.main

import com.robinwersich.todue.ui.components.TaskState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

sealed interface TaskPropertyOverlay {
  data class DueDateOverlay(val taskId: Long, val initialDate: LocalDate) : TaskPropertyOverlay
}

data class MainScreenState(
  val tasks: ImmutableList<TaskState> = persistentListOf(),
  val taskPropertyOverlay: TaskPropertyOverlay? = null,
)
