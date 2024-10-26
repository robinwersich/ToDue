package com.robinwersich.todue.ui.presentation.organizer

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class OrganizerState(
  val tasks: ImmutableList<TaskViewState> = persistentListOf(),
)
