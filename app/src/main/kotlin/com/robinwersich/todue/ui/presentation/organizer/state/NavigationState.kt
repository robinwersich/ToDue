package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.rangeTo
import com.robinwersich.todue.domain.model.size
import com.robinwersich.todue.domain.model.toDoubleRange
import com.robinwersich.todue.ui.composeextensions.SwipeableTransition
import com.robinwersich.todue.ui.composeextensions.getAdjacentToCurrentAnchors
import com.robinwersich.todue.ui.composeextensions.isSettled
import com.robinwersich.todue.ui.composeextensions.offsetToCurrent
import com.robinwersich.todue.ui.composeextensions.pairReferentialEqualityPolicy
import com.robinwersich.todue.utility.center
import com.robinwersich.todue.utility.mapToImmutableList
import com.robinwersich.todue.utility.size
import com.robinwersich.todue.utility.toImmutableList
import com.robinwersich.todue.utility.union
import java.time.LocalDate
import kotlin.math.ceil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Holds information about the current navigation position of the organizer. This is mainly defined
 * by a [timelineDraggableState] and a [dateDraggableState]. Other derived properties are exposed as
 * observable state.
 *
 * @param timelines The timelines to navigate through. These do not need to be sorted initially, but
 *   they will be internally.
 */
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
  /** The ordered list of possible [Timeline]s to navigate through. */
  private val timelines = timelines.sorted()

  /** The [AnchoredDraggableState] controlling the time navigation. */
  val dateDraggableState =
    AnchoredDraggableState(
      initialValue = initialDate,
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )
  /** The [AnchoredDraggableState] controlling the granularity navigation. */
  val timelineDraggableState =
    AnchoredDraggableState(
      initialValue = TimelineNavPosition(initialTimeline),
      snapAnimationSpec = snapAnimationSpec,
      decayAnimationSpec = decayAnimationSpec,
      positionalThreshold = positionalThreshold,
      velocityThreshold = velocityThreshold,
    )

  /** The current size of the viewport, used to calculate the anchor positions. */
  var viewportSize: IntSize? by mutableStateOf(null)

  /**
   * The relative additionally visible space before the current
   * [dateRange][NavigationPosition.dateRange].
   */
  private var relativeTopMargin: Float by mutableFloatStateOf(0f)

  /**
   * The relative additionally visible space after the current
   * [dateRange][NavigationPosition.dateRange].
   */
  private var relativeBottomMargin: Float by mutableFloatStateOf(0f)

  /** The current [TimelineNavPosition] of the organizer. */
  private val currentTimelineNavPos: TimelineNavPosition
    get() = timelineDraggableState.currentValue

  /** The focussed date of the organizer from which the current [TimeBlock] is derived. */
  private val currentDate: LocalDate
    get() = dateDraggableState.currentValue

  /** The current [NavigationPosition] of the organizer. */
  private val currentNavPos: NavigationPosition
    get() = adjacentNavigationPositions.current

  /** The currently focussed [Timeline]. */
  private val currentTimeline: Timeline
    get() = currentTimelineNavPos.timeline

  /** The currently focussed [TimeBlock]. */
  internal val currentTimeBlock: TimeBlock
    get() = currentNavPos.timeBlock

  /**
   * This function needs to be launched in the [CoroutineScope][kotlinx.coroutines.CoroutineScope]
   * of the composable using this state to correctly update the internal [DraggableAnchors].
   */
  suspend fun updateDateAnchorsOnSwipe() {
    snapshotFlow { timelineDraggableState.currentValue to dateDraggableState.currentValue }
      .collect { (_, centerAnchor) ->
        viewportSize?.run { updateDateAnchors(centerAnchor, height) }
      }
  }

  /**
   * This function needs to be launched in the [CoroutineScope][kotlinx.coroutines.CoroutineScope]
   * of the composable using this state to correctly update the internal [DraggableAnchors].
   */
  suspend fun updateTimelineAnchorsOnSwipe() {
    snapshotFlow { with(timelineDraggableState) { settledValue } }
      .collect { centerAnchor -> viewportSize?.run { updateTimelineAnchors(centerAnchor, width) } }
  }

  /**
   * Should be called whenever the viewport size changes to ensure the [DraggableAnchors] are of
   * this state are spaced correctly.
   */
  fun updateViewportSize(
    size: IntSize,
    relativeTopMargin: Float = this.relativeTopMargin,
    relativeBottomMargin: Float = this.relativeBottomMargin,
  ) {
    if (size.width != viewportSize?.width) updateTimelineAnchors(currentTimelineNavPos, size.width)
    if (size.height != viewportSize?.height) updateDateAnchors(currentDate, size.height)
    this.viewportSize = size
    this.relativeTopMargin = relativeTopMargin
    this.relativeBottomMargin = relativeBottomMargin
  }

  private fun getChild(timeline: Timeline): Timeline? {
    val timelineIndex = timelines.indexOf(timeline)
    if (timelineIndex == -1) return null
    return timelines.getOrNull(timelineIndex - 1)
  }

  private fun getParent(timeline: Timeline): Timeline? {
    val timelineIndex = timelines.indexOf(timeline)
    if (timelineIndex == -1) return null
    return timelines.getOrNull(timelineIndex + 1)
  }

  private fun updateTimelineAnchors(newCenter: TimelineNavPosition, viewportLength: Int) {
    val newAnchors: DraggableAnchors<TimelineNavPosition> =
      if (newCenter.child != null) {
        DraggableAnchors {
          TimelineNavPosition(newCenter.child) at (childTimelineSizeRatio - 1) * viewportLength
          newCenter at 0f
          TimelineNavPosition(newCenter.timeline) at childTimelineSizeRatio * viewportLength
        }
      } else {
        DraggableAnchors {
          getChild(newCenter.timeline)?.let {
            TimelineNavPosition(child = it, timeline = newCenter.timeline) at
              -childTimelineSizeRatio * viewportLength
          }
          newCenter at 0f
          getParent(newCenter.timeline)?.let {
            TimelineNavPosition(child = newCenter.timeline, timeline = it) at
              (1 - childTimelineSizeRatio) * viewportLength
          }
        }
      }
    updateAnchors(timelineDraggableState, newAnchors, newTarget = newCenter)
  }

  private fun updateDateAnchors(newCenter: LocalDate, viewportLength: Int) {
    val newAnchors = DraggableAnchors {
      val currentBlock = currentTimeline.timeBlockFrom(newCenter)
      val prevBlock = currentBlock - 1
      val nextBlock = currentBlock + 1

      val currentDateRange =
        getVisibleDateRange(currentTimelineNavPos, currentBlock).toDoubleRange()
      val prevDateRange = getVisibleDateRange(currentTimelineNavPos, prevBlock).toDoubleRange()
      val nextDateRange = getVisibleDateRange(currentTimelineNavPos, nextBlock).toDoubleRange()

      val prevDateDistance = prevDateRange.center - currentDateRange.center
      val nextDateDistance = nextDateRange.center - currentDateRange.center
      val prevPxPerDay = (viewportLength * 2) / (prevDateRange.size + currentDateRange.size)
      val nextPxPerDay = (viewportLength * 2) / (nextDateRange.size + currentDateRange.size)

      prevBlock.start at (prevDateDistance * prevPxPerDay).toFloat()
      newCenter at 0f
      nextBlock.start at (nextDateDistance * nextPxPerDay).toFloat()
    }
    updateAnchors(dateDraggableState, newAnchors, newTarget = currentDate)
  }

  /**
   * Updates the anchors of [state] while avoiding jumps in the offset, i.e. the offset to
   * [newTarget] will stay the same if both the old and the new anchors contain it.
   */
  private fun <T> updateAnchors(
    state: AnchoredDraggableState<T>,
    newAnchors: DraggableAnchors<T>,
    newTarget: T,
  ) {
    val oldNewTargetPos = state.anchors.positionOf(newTarget)
    state.updateAnchors(newAnchors, newTarget)
    if (state.settledValue != newTarget) {
      // snap during updateAnchors was not successful (drag / animation in progress)
      // need to adjust offset manually
      val newNewTargetPos = state.anchors.positionOf(newTarget)
      val offsetShift = newNewTargetPos - oldNewTargetPos
      if (!offsetShift.isNaN()) state.dispatchRawDelta(offsetShift)
    }
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

    fun navPos(timelineNavPos: TimelineNavPosition, date: LocalDate): NavigationPosition {
      val timeBlock = timelineNavPos.timeline.timeBlockFrom(date)
      return NavigationPosition(
        timelineNavPos = timelineNavPos,
        date = date,
        dateRange = getVisibleDateRange(timelineNavPos, timeBlock),
        timeBlock = timeBlock,
      )
    }

    AdjacentNavigationPositions(
      current = navPos(currentTimelineNavPos, currentDate),
      prevDate = navPos(currentTimelineNavPos, prevDate),
      nextDate = navPos(currentTimelineNavPos, nextDate),
      prevTimeline = navPos(prevTimelinePos, currentDate),
      nextTimeline = navPos(nextTimelinePos, currentDate),
    )
  }

  /**
   * All [Timeline]s and corresponding [DateRange]s of the current [AdjacentNavigationPositions].
   */
  val prefetchTimelineDateRanges: ImmutableList<Pair<Timeline, DateRange>> by derivedStateOf {
    val dateRangesByTimeline = mutableMapOf<Timeline, DateRange>()
    for ((timelineNavPos, _, dateRange) in adjacentNavigationPositions) {
      timelineNavPos.visibleTimelines.forEach { timeline ->
        val currentRange = dateRangesByTimeline.getOrDefault(timeline, dateRange)
        dateRangesByTimeline[timeline] = currentRange union dateRange
      }
    }
    dateRangesByTimeline.toImmutableList()
  }

  /**
   * The transition of the current [NavigationPosition]. This combines two [AnchoredDraggableState]s
   * into a single [SwipeableTransition].
   */
  val navPosTransition: SwipeableTransition<NavigationPosition> =
    derivedStateOf(pairReferentialEqualityPolicy()) {
        with(adjacentNavigationPositions) {
          val timelineOffsetToCurrent = timelineDraggableState.offsetToCurrent
          val dateOffsetToCurrent = dateDraggableState.offsetToCurrent
          when {
            timelineOffsetToCurrent < 0 -> prevTimeline to current
            timelineOffsetToCurrent > 0 -> current to nextTimeline
            dateOffsetToCurrent < 0 -> prevDate to current
            dateOffsetToCurrent > 0 -> current to nextDate
            else -> current to current
          }
        }
      }
      .let { transitionStates ->
        SwipeableTransition(
          transitionStates = { transitionStates.value },
          progress = {
            val (prevPos, nextPos) = transitionStates.value
            if (!timelineDraggableState.isSettled) {
              timelineDraggableState.progress(prevPos.timelineNavPos, nextPos.timelineNavPos)
            } else {
              dateDraggableState.progress(prevPos.date, nextPos.date)
            }
          },
        )
      }

  /**
   * The [Timeline]s and corresponding [TimeBlock]s that are visible currently or at some point in
   * the current [NavigationPosition] transition.
   */
  val activeTimelineBlocks:
    ImmutableList<Pair<Timeline, ImmutableList<TimeBlock>>> by derivedStateOf {
    val (prevPos, nextPos) = navPosTransition.transitionStates()
    val activeTimelines = buildList {
      prevPos.timelineNavPos.visibleTimelines.forEach { add(it) }
      nextPos.timelineNavPos.visibleTimelines.forEach { if (it !in this) add(it) }
    }
    val activeDateRange =
      prevPos.dateRange.applyMargin(relativeTopMargin, relativeBottomMargin) union
        nextPos.dateRange.applyMargin(relativeTopMargin, relativeBottomMargin)
    activeTimelines.mapToImmutableList { timeline ->
      val firstBlock = timeline.timeBlockFrom(activeDateRange.start)
      val lastBlock = timeline.timeBlockFrom(activeDateRange.endInclusive)
      timeline to (firstBlock..lastBlock).toImmutableList()
    }
  }
}

private fun DateRange.applyMargin(startMargin: Float, endMargin: Float): DateRange {
  val rangeSize = size
  val newStart = start.minusDays(ceil(rangeSize * startMargin).toLong())
  val newEnd = endInclusive.plusDays(ceil(rangeSize * endMargin).toLong())
  return newStart..newEnd
}

private fun getVisibleDateRange(
  timelineNavPos: TimelineNavPosition,
  timeBlock: TimeBlock,
): DateRange {
  return getVisibleStart(timelineNavPos, timeBlock)..getVisibleEnd(timelineNavPos, timeBlock)
}

private fun getVisibleStart(timelineNavPos: TimelineNavPosition, timeBlock: TimeBlock): LocalDate {
  return timelineNavPos.child?.let {
    val childBlock = it.timeBlockFrom(timeBlock.start)
    childBlock.start
  } ?: timeBlock.start
}

private fun getVisibleEnd(timelineNavPos: TimelineNavPosition, timeBlock: TimeBlock): LocalDate {
  return timelineNavPos.child?.let {
    val childBlock = it.timeBlockFrom(timeBlock.endInclusive)
    childBlock.endInclusive
  } ?: timeBlock.endInclusive
}
