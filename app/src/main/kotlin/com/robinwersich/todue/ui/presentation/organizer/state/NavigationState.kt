package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.DateTimeRange
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.daysUntil
import com.robinwersich.todue.domain.model.duration
import com.robinwersich.todue.domain.model.toDateTimeRange
import com.robinwersich.todue.ui.utility.MyDraggableAnchors
import com.robinwersich.todue.ui.utility.getAdjacentToCurrentAnchors
import com.robinwersich.todue.ui.utility.isSettled
import com.robinwersich.todue.ui.utility.offsetToCurrent
import com.robinwersich.todue.ui.utility.pairReferentialEqualityPolicy
import com.robinwersich.todue.utility.interpolateTo
import com.robinwersich.todue.utility.mapToImmutableList
import com.robinwersich.todue.utility.toImmutableList
import com.robinwersich.todue.utility.union
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
@OptIn(ExperimentalFoundationApi::class)
class NavigationState(
  timelines: List<Timeline>,
  val childTimelineSizeRatio: Float,
  positionalThreshold: (totalDistance: Float) -> Float,
  velocityThreshold: () -> Float,
  snapAnimationSpec: AnimationSpec<Float>,
  decayAnimationSpec: DecayAnimationSpec<Float>,
  initialTimeline: Timeline = timelines.first(),
  initialDate: LocalDate = LocalDate.now(),
) {
  private val timelines = timelines.sorted().toImmutableList()

  val dateDraggableState =
    AnchoredDraggableState(
      initialValue = initialDate,
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )
  val timelineDraggableState =
    AnchoredDraggableState(
      initialValue = TimelineNavigationPosition(timelines, initialTimeline),
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )

  private var viewportSize: IntSize? = null

  private val currentTimelinePosition: TimelineNavigationPosition
    get() = timelineDraggableState.currentValue

  private val currentTimeline: Timeline
    get() = currentTimelinePosition.timeline

  private val currentDate: LocalDate
    get() = dateDraggableState.currentValue

  /**
   * This function needs to be launched in the [CoroutineScope][kotlinx.coroutines.CoroutineScope]
   * of the composable using this state to correctly update the internal [DraggableAnchors].
   */
  suspend fun updateDateAnchorsOnSwipe() {
    snapshotFlow { currentTimelinePosition to currentDate }
      .collect { viewportSize?.let { updateDateAnchors(it.height) } }
  }

  fun updateViewportSize(size: IntSize) {
    if (size.width != viewportSize?.width) updateTimelineAnchors(size.width)
    if (size.height != viewportSize?.height) updateDateAnchors(size.height)
    viewportSize = size
  }

  private fun updateTimelineAnchors(viewportLength: Int) {
    val newAnchors = MyDraggableAnchors {
      for (i in timelines.indices) {
        val position = TimelineNavigationPosition(timelines, i, showChild = false)
        val offset = (viewportLength * i).toFloat()
        position at offset
        if (i != 0) {
          position.copy(showChild = true) at offset - viewportLength * childTimelineSizeRatio
        }
      }
    }
    timelineDraggableState.updateAnchors(newAnchors, newTarget = currentTimelinePosition)
  }

  private fun updateDateAnchors(viewportLength: Int) {
    val newAnchors = MyDraggableAnchors {
      val currentTimeBlock = currentTimeline.timeBlockFrom(currentDate)
      val prevBlock = currentTimeBlock - 1
      val nextBlock = currentTimeBlock + 1

      val currentDateRange = getVisibleDateRange(currentTimelinePosition, currentTimeBlock)
      val prevDateDistance =
        getVisibleEnd(currentTimelinePosition, prevBlock).daysUntil(currentDateRange.endInclusive)
      val nextDateDistance =
        currentDateRange.start.daysUntil(getVisibleStart(currentTimelinePosition, nextBlock))

      val pxPerDay = viewportLength / currentDateRange.duration
      prevBlock.start at -(prevDateDistance * pxPerDay).toFloat()
      currentDate at 0f
      nextBlock.start at (nextDateDistance * pxPerDay).toFloat()
    }
    // By updating the anchors, the offset for the current anchor may jump.
    // To keep the relation between offset and current anchor, we also need to update the offset.
    val prevCurrentDatePos = dateDraggableState.anchors.positionOf(currentDate)
    dateDraggableState.updateAnchors(newAnchors, newTarget = currentDate)
    if (!prevCurrentDatePos.isNaN()) dateDraggableState.dispatchRawDelta(-prevCurrentDatePos)
  }

  /**
   * Describes the current [NavigationPosition] and the ones reachable from there. If the current
   * position the first or last, the previous or next position will be the same as the current one.
   */
  private data class AdjacentNavigationPositions(
    val current: NavigationPosition,
    val prevDate: NavigationPosition,
    val nextDate: NavigationPosition,
    val prevTimeline: NavigationPosition,
    val nextTimeline: NavigationPosition,
  ) : Iterable<NavigationPosition> {
    override fun iterator() =
      listOf(current, prevDate, nextDate, prevTimeline, nextTimeline).iterator()
  }

  private val adjacentNavigationPositions: AdjacentNavigationPositions by derivedStateOf {
    val (prevDate, nextDate) = dateDraggableState.getAdjacentToCurrentAnchors()
    val (prevTimelinePos, nextTimelinePos) = timelineDraggableState.getAdjacentToCurrentAnchors()

    fun navPos(timeline: TimelineNavigationPosition, date: LocalDate) =
      NavigationPosition(timeline, date, getVisibleDateRange(timeline, date))

    AdjacentNavigationPositions(
      current = navPos(currentTimelinePosition, currentDate),
      prevDate = navPos(currentTimelinePosition, prevDate),
      nextDate = navPos(currentTimelinePosition, nextDate),
      prevTimeline = navPos(prevTimelinePos, currentDate),
      nextTimeline = navPos(nextTimelinePos, currentDate),
    )
  }

  val prefetchTimelineDateRanges: ImmutableList<Pair<Timeline, DateRange>> by derivedStateOf {
    val dateRangesByTimeline = mutableMapOf<Timeline, DateRange>()
    for ((timelinePosition, _, dateRange) in adjacentNavigationPositions) {
      timelinePosition.visibleTimelines.forEach { timeline ->
        val currentRange = dateRangesByTimeline.getOrDefault(timeline, dateRange)
        dateRangesByTimeline[timeline] = currentRange union dateRange
      }
    }
    dateRangesByTimeline.toImmutableList()
  }

  val activeNavigationPositions: Pair<NavigationPosition, NavigationPosition> by
    derivedStateOf(pairReferentialEqualityPolicy()) {
      with(adjacentNavigationPositions) {
        if (!timelineDraggableState.isSettled) {
          val offsetToCurrent = timelineDraggableState.offsetToCurrent
          when {
            offsetToCurrent < 0 -> prevTimeline to current
            offsetToCurrent > 0 -> current to nextTimeline
            else -> current to current
          }
        } else {
          val offsetToCurrent = dateDraggableState.offsetToCurrent
          when {
            offsetToCurrent < 0 -> prevDate to current
            offsetToCurrent > 0 -> current to nextDate
            else -> current to current
          }
        }
      }
    }

  val activeTimelineBlocks:
    ImmutableList<Pair<Timeline, ImmutableList<TimeBlock>>> by derivedStateOf {
    val (prevPos, nextPos) = activeNavigationPositions
    val activeTimelines = buildList {
      prevPos.timelinePosition.visibleTimelines.forEach { add(it) }
      nextPos.timelinePosition.visibleTimelines.forEach { if (it !in this) add(it) }
    }
    val activeDateRange = prevPos.dateRange union nextPos.dateRange
    activeTimelines.mapToImmutableList { timeline ->
      val firstBlock = timeline.timeBlockFrom(activeDateRange.start)
      val lastBlock = timeline.timeBlockFrom(activeDateRange.endInclusive)
      timeline to (firstBlock..lastBlock).toImmutableList()
    }
  }

  val visibleDateTimeRange: DateTimeRange by derivedStateOf {
    val (prevPos, nextPos) = activeNavigationPositions
    val progress =
      if (!timelineDraggableState.isSettled) {
        timelineDraggableState.progress(prevPos.timelinePosition, nextPos.timelinePosition)
      } else {
        dateDraggableState.progress(prevPos.datePosition, nextPos.datePosition)
      }
    prevPos.dateRange.toDateTimeRange().interpolateTo(nextPos.dateRange.toDateTimeRange(), progress)
  }
}

private fun getVisibleDateRange(timelinePosition: TimelineNavigationPosition, date: LocalDate) =
  getVisibleDateRange(timelinePosition, timelinePosition.timeline.timeBlockFrom(date))

private fun getVisibleDateRange(
  timelinePosition: TimelineNavigationPosition,
  timeBlock: TimeBlock,
): DateRange {
  return getVisibleStart(timelinePosition, timeBlock)..getVisibleEnd(timelinePosition, timeBlock)
}

private fun getVisibleStart(
  timelinePosition: TimelineNavigationPosition,
  timeBlock: TimeBlock,
): LocalDate {
  return timelinePosition.visibleChild?.let {
    val childBlock = it.timeBlockFrom(timeBlock.start)
    childBlock.start
  } ?: timeBlock.start
}

private fun getVisibleEnd(
  timelinePosition: TimelineNavigationPosition,
  timeBlock: TimeBlock,
): LocalDate {
  return timelinePosition.visibleChild?.let {
    val childBlock = it.timeBlockFrom(timeBlock.endInclusive)
    childBlock.endInclusive
  } ?: timeBlock.endInclusive
}
