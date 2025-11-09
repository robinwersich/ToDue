package com.robinwersich.todue.domain.model

import androidx.compose.runtime.Immutable

/** A [TimelineBlock] with associated tasks and additional information */
@Immutable
data class TaskBlock(
  val timelineBlock: TimelineBlock,
  val tasks: List<Task> = listOf(),
  // TODO: add totalTaskDuration
) {
  val timeBlock: TimeBlock
    get() = timelineBlock.section
}
