package com.robinwersich.todue.ui.presentation.organizer

import androidx.annotation.FloatRange
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.IntSize
import com.robinwersich.todue.domain.model.TimeUnitInstance
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.ui.utility.getInterpolatedValue
import com.robinwersich.todue.ui.utility.isSettled
import com.robinwersich.todue.utility.interpolateTo
import java.time.LocalDate

sealed interface VisibleTimelines {
  data class Single(val timeline: Timeline) : VisibleTimelines

  data class Double(val child: Timeline, val parent: Timeline) : VisibleTimelines
}

@ExperimentalFoundationApi
class NavigationState(
  private var timelines: List<Timeline>,
  private var organizerSize: IntSize,
  positionalThreshold: (Float) -> Float,
  velocityThreshold: () -> Float,
  @FloatRange(0.0, 1.0) private val childTimelineSizeFraction: Float = 0.3f,
  initialTimelineIndex: Int = 0,
  initialDate: LocalDate = LocalDate.now(),
) {
  val timelineDraggableState: AnchoredDraggableState<VisibleTimelines> =
    AnchoredDraggableState(
      initialValue = VisibleTimelines.Single(timelines[initialTimelineIndex]),
      snapAnimationSpec = tween(),
      decayAnimationSpec = exponentialDecay(),
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )
  val taskBlockDraggableState: AnchoredDraggableState<LocalDate> =
    AnchoredDraggableState(
      initialValue = initialDate,
      snapAnimationSpec = tween(),
      decayAnimationSpec = exponentialDecay(),
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )

  private val currentVisibleTimelines: VisibleTimelines
    get() = timelineDraggableState.currentValue

  private val currentTimeline: Timeline
    get() =
      when (val visibleTimelines = timelineDraggableState.currentValue) {
        is VisibleTimelines.Single -> visibleTimelines.timeline
        is VisibleTimelines.Double -> visibleTimelines.parent
      }

  private val currentDate: LocalDate
    get() = taskBlockDraggableState.currentValue

  private val currentTimeBlock
    get() = currentTimeline.timeUnit.instanceFrom(currentDate)

  val visibleDateRange by derivedStateOf {
    if (!timelineDraggableState.isSettled) {
      timelineDraggableState.getInterpolatedValue(
        interpolateValue = { start, end, progress -> start.interpolateTo(end, progress) },
        targetValueByAnchor = { getVisibleDateRange(currentTimeBlock, it) },
      )
    } else {
      taskBlockDraggableState.getInterpolatedValue(
        interpolateValue = { start, end, progress -> start.interpolateTo(end, progress) },
        targetValueByAnchor = {
          val timeBlock = currentTimeline.timeUnit.instanceFrom(it)
          getVisibleDateRange(timeBlock, currentVisibleTimelines)
        },
      )
    }
  }

  init {
    update()
  }

  fun update(
    newTimelines: List<Timeline> = this.timelines,
    newOrganizerSize: IntSize = this.organizerSize,
  ) {
    timelines = newTimelines
    organizerSize = newOrganizerSize
    timelineDraggableState.updateAnchors(getTimelineAnchors(newTimelines))
    val currentTimeBlock = currentTimeline.timeUnit.instanceFrom(currentDate)
    taskBlockDraggableState.updateAnchors(getTaskBlockAnchors(currentTimeBlock))
  }

  private fun getTimelineAnchors(timelines: List<Timeline>): DraggableAnchors<VisibleTimelines> =
    DraggableAnchors {
      for (i in timelines.indices) {
        VisibleTimelines.Single(timelines[i]) at -(organizerSize.width * i).toFloat()
        if (i < timelines.size - 1) {
          VisibleTimelines.Double(child = timelines[i], parent = timelines[i + 1]) at
            -organizerSize.width * (i + 1 - childTimelineSizeFraction)
        }
      }
    }

  private fun getTaskBlockAnchors(currentTimeBlock: TimeUnitInstance): DraggableAnchors<LocalDate> {
    val currentDateRange = getVisibleDateRange(currentTimeBlock, currentVisibleTimelines)

    fun getSwipeDistance(timeBlock: TimeUnitInstance): Float {
      val dateRange = getVisibleDateRange(timeBlock, currentVisibleTimelines)
      val dateDistance =
        if (timeBlock > currentTimeBlock) (currentDateRange.start - dateRange.start)
        else (currentDateRange.endInclusive - dateRange.endInclusive)
      val pxPerDay = organizerSize.height / (currentDateRange.endInclusive - currentDateRange.start)
      return (dateDistance * pxPerDay).toFloat()
    }

    return DraggableAnchors {
      val prevTimeBlock = currentTimeBlock - 1
      val nextTimeBlock = currentTimeBlock + 1

      prevTimeBlock.start at getSwipeDistance(prevTimeBlock)
      currentDate at 0f
      nextTimeBlock.start at getSwipeDistance(nextTimeBlock)
    }
  }

  private fun getVisibleDateRange(
    timeBlock: TimeUnitInstance,
    visibleTimelines: VisibleTimelines,
  ): ClosedFloatingPointRange<Double> {
    return when (visibleTimelines) {
      is VisibleTimelines.Single -> {
        val start = timeBlock.start.toEpochDay().toDouble()
        val end = timeBlock.endInclusive.toEpochDay().toDouble()
        start..end
      }
      is VisibleTimelines.Double -> {
        // TODO: better formatting
        val start =
          visibleTimelines.child.timeUnit
            .instanceFrom(timeBlock.start)
            .start
            .toEpochDay()
            .toDouble()
        val end =
          visibleTimelines.parent.timeUnit
            .instanceFrom(timeBlock.endInclusive)
            .endInclusive
            .toEpochDay()
            .toDouble()
        start..end
      }
    }
  }
}
