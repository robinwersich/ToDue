package com.robinwersich.todue.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration

/** A [TimelineBlock] with associated task data */
@Immutable
data class TaskBlock(
  val timelineBlock: TimelineBlock,
  val mainData: TaskBlockMainData? = null,
  val previewData: TaskBlockPreviewData? = null,
) {
  val timeBlock: TimeBlock
    get() = timelineBlock.section

  companion object {
    fun main(timelineBlock: TimelineBlock, tasks: List<Task>) =
      TaskBlock(
        timelineBlock = timelineBlock,
        mainData = TaskBlockMainData(tasks),
      )

    fun preview(timelineBlock: TimelineBlock, totalEstimatedDuration: Duration) =
      TaskBlock(
        timelineBlock = timelineBlock,
        previewData = TaskBlockPreviewData(totalEstimatedDuration),
      )
  }
}

@Immutable data class TaskBlockMainData(val tasks: List<Task>)

@Immutable data class TaskBlockPreviewData(val totalEstimatedDuration: Duration)
