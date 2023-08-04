package com.robinwersich.todue.ui.screens.main

import com.robinwersich.todue.ui.components.TaskState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MainScreenState(
  val tasks: ImmutableList<TaskState> = persistentListOf(),
)
