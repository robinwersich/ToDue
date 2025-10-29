package com.robinwersich.todue.domain.model

/** A [TimelineBlock] with associated tasks and additional information */
data class TaskBlock(
  val timelineBlock: TimelineBlock,
  val tasks: List<Task> = listOf(),
  // TODO: add totalTaskDuration
)
