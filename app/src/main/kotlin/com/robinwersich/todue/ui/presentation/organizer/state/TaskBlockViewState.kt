package com.robinwersich.todue.ui.presentation.organizer.state

import com.robinwersich.todue.domain.model.TimelineBlock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TaskBlockViewState(
  val timelineBlock: TimelineBlock,
  val tasks: ImmutableList<TaskViewState> = persistentListOf(),
) {
  val timeBlock
    get() = timelineBlock.section
}
