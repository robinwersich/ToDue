package com.robinwersich.todue.domain.model

import kotlinx.collections.immutable.ImmutableList

/** A [TimelineBlock] with associated tasks and additional information */
data class TaskBlock(
  val timelineBlock: TimelineBlock,
  val tasks: ImmutableList<Task>,
  // TODO: add totalTaskDuration
)
