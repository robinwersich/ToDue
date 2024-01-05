package com.robinwersich.todue.ui.presentation.organizer

import androidx.annotation.FloatRange
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.ui.unit.IntSize
import com.robinwersich.todue.domain.model.Timeline
import java.time.LocalDate

@ExperimentalFoundationApi
class NavigationState(
  private var timelines: List<Timeline>,
  private var organizerSize: IntSize,
  timelinePositionalThreshold: (Float) -> Float,
  timelineVelocityThreshold: () -> Float,
  @FloatRange(0.0, 1.0) private val childTimelineSizeFraction: Float = 0.3f,
  initialTimelineIndex: Int = 0,
  initialDate: LocalDate = LocalDate.now(),
) {
  val timelineDraggableState: AnchoredDraggableState<VisibleTimelines> =
    AnchoredDraggableState(
      initialValue = VisibleTimelines.Single(timelines[initialTimelineIndex]),
      positionalThreshold = timelinePositionalThreshold,
      velocityThreshold = timelineVelocityThreshold,
      animationSpec = tween(),
    )

  val currentDate: LocalDate = initialDate

  init {
    updateTimelineAnchors()
  }

  fun update(
    newTimelines: List<Timeline> = this.timelines,
    newOrganizerSize: IntSize = this.organizerSize
  ) {
    timelines = newTimelines
    organizerSize = newOrganizerSize
    updateTimelineAnchors()
  }

  private fun updateTimelineAnchors() {
    val newAnchors = DraggableAnchors {
      for (i in timelines.indices) {
        VisibleTimelines.Single(timelines[i]) at -(organizerSize.width * i).toFloat()
        if (i < timelines.size - 1) {
          VisibleTimelines.Double(child = timelines[i], parent = timelines[i + 1]) at
            -organizerSize.width * (i + 1 - childTimelineSizeFraction)
        }
      }
    }
    timelineDraggableState.updateAnchors(newAnchors)
  }
}

sealed interface VisibleTimelines {
  data class Single(val timeline: Timeline) : VisibleTimelines

  data class Double(val child: Timeline, val parent: Timeline) : VisibleTimelines
}
