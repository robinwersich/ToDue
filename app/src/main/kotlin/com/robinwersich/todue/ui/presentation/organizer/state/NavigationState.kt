package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import java.time.LocalDate
import kotlin.math.ceil
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.center
import com.robinwersich.todue.domain.model.rangeTo
import com.robinwersich.todue.domain.model.size
import com.robinwersich.todue.domain.model.toDoubleRange
import com.robinwersich.todue.ui.composeextensions.SwipeableTransition
import com.robinwersich.todue.ui.composeextensions.getAdjacentToCurrentAnchors
import com.robinwersich.todue.ui.composeextensions.instantStop
import com.robinwersich.todue.ui.composeextensions.isSettled
import com.robinwersich.todue.ui.composeextensions.offsetToCurrent
import com.robinwersich.todue.ui.composeextensions.pairReferentialEqualityPolicy
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState.Companion.defaultTimeline
import com.robinwersich.todue.utility.buildImmutableList
import com.robinwersich.todue.utility.center
import com.robinwersich.todue.utility.forEachDistinct
import com.robinwersich.todue.utility.intersection
import com.robinwersich.todue.utility.size
import com.robinwersich.todue.utility.union

/**
 * Holds information about the current navigation position of the organizer. This is mainly defined
 * by a [timelineDraggableState] and a [dateDraggableState]. Other derived properties are exposed as
 * observable state.
 *
 * @param timelines The timelines to navigate through. These do not need to be sorted initially, but
 *   they will be internally.
 */
// TODO switch to new AnchoredDraggableState constructor once
//  https://issuetracker.google.com/issues/448407163 is fixed
@Stable
class NavigationState(
  timelines: Collection<Timeline> = listOf(defaultTimeline),
  val childTimelineSizeRatio: Float = 0.3f,
  positionalThreshold: (totalDistance: Float) -> Float = { it * 0.3f },
  velocityThreshold: () -> Float = { 500f },
  snapAnimationSpec: AnimationSpec<Float> =
    spring(
      stiffness = Spring.StiffnessMediumLow,
      visibilityThreshold = Int.VisibilityThreshold.toFloat(),
    ),
  decayAnimationSpec: DecayAnimationSpec<Float> = instantStop(),
  initialTimeline: Timeline = timelines.first(),
  initialDate: LocalDate = LocalDate.now(),
) {
  companion object {
    val defaultTimeline = Timeline(0, TimeUnit.DAY)
  }

  /** The ordered list of possible [Timeline]s to navigate through. */
  private var timelines = if (timelines.isEmpty()) listOf(defaultTimeline) else timelines.sorted()

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

  val currentTimelineNavPos: TimelineNavPosition
    get() = timelineDraggableState.currentValue

  /** The focussed date of the organizer from which the current [TimeBlock] is derived. */
  val currentDate: LocalDate
    get() = dateDraggableState.currentValue

  val currentNavPos: NavigationPosition
    get() = adjacentNavigationPositions.current

  val currentTimeline: Timeline
    get() = currentTimelineNavPos.timeline

  val currentTimeBlock: TimeBlock
    get() = currentNavPos.timeBlock

  val currentTimelineBlock: TimelineBlock
    get() = currentNavPos.timelineBlock

  val isSplitView: Boolean
    get() = currentTimelineNavPos.showChild

  /**
   * @param timelines Non-empty list of timelines to navigate through. Will be sorted by their ID.
   *   If an empty collection is given, the [defaultTimeline] will be set to avoid exceptions.
   */
  fun setTimelines(timelines: Collection<Timeline>) {
    this.timelines = if (timelines.isEmpty()) listOf(defaultTimeline) else timelines.sorted()
    updateTimelineAnchors(
      newCenter =
        if (this.timelines.containsAll(currentTimelineNavPos.visibleTimelines.toList())) {
          currentTimelineNavPos
        } else {
          TimelineNavPosition(this.timelines.first())
        }
    )
    updateDateAnchors()
  }

  /**
   * This function needs to be launched in the [CoroutineScope][kotlinx.coroutines.CoroutineScope]
   * of the composable using this state to correctly update the internal [DraggableAnchors].
   */
  suspend fun updateDateAnchorsOnSwipe() {
    snapshotFlow { timelineDraggableState.currentValue to dateDraggableState.currentValue }
      .collect { (_, centerAnchor) -> updateDateAnchors(centerAnchor) }
  }

  /**
   * This function needs to be launched in the [CoroutineScope][kotlinx.coroutines.CoroutineScope]
   * of the composable using this state to correctly update the internal [DraggableAnchors].
   */
  suspend fun updateTimelineAnchorsOnSwipe() {
    snapshotFlow { timelineDraggableState.settledValue }
      .collect { centerAnchor -> updateTimelineAnchors(centerAnchor) }
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
    val widthChanged = size.width != viewportSize?.width
    val heightChanged = size.height != viewportSize?.height
    this.viewportSize = size
    this.relativeTopMargin = relativeTopMargin
    this.relativeBottomMargin = relativeBottomMargin
    if (widthChanged) updateTimelineAnchors()
    if (heightChanged) updateDateAnchors()
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

  private fun updateTimelineAnchors(newCenter: TimelineNavPosition = currentTimelineNavPos) {
    val viewportLength = viewportSize?.width
    val newAnchors: DraggableAnchors<TimelineNavPosition> =
      when {
        viewportLength == null -> DraggableAnchors { newCenter at 0f }
        newCenter.child != null -> {
          DraggableAnchors {
            TimelineNavPosition(newCenter.child) at (childTimelineSizeRatio - 1) * viewportLength
            newCenter at 0f
            TimelineNavPosition(newCenter.timeline) at childTimelineSizeRatio * viewportLength
          }
        }
        else -> {
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
      }
    updateAnchors(timelineDraggableState, newAnchors, alignAnchor = newCenter)
  }

  private fun updateDateAnchors(newCenter: LocalDate = currentDate) {
    val viewportLength = viewportSize?.height
    val newAnchors =
      when {
        viewportLength == null -> DraggableAnchors { newCenter at 0f }
        else ->
          DraggableAnchors {
            val currentBlock = currentTimeline.timeBlockFrom(newCenter)
            val prevBlock = currentBlock - 1
            val nextBlock = currentBlock + 1

            val currentDateRange =
              getVisibleDateRange(currentTimelineNavPos, currentBlock).toDoubleRange()
            val prevDateRange =
              getVisibleDateRange(currentTimelineNavPos, prevBlock).toDoubleRange()
            val nextDateRange =
              getVisibleDateRange(currentTimelineNavPos, nextBlock).toDoubleRange()

            val prevDateDistance = prevDateRange.center - currentDateRange.center
            val nextDateDistance = nextDateRange.center - currentDateRange.center
            val prevPxPerDay = (viewportLength * 2) / (prevDateRange.size + currentDateRange.size)
            val nextPxPerDay = (viewportLength * 2) / (nextDateRange.size + currentDateRange.size)

            prevBlock.center at (prevDateDistance * prevPxPerDay).toFloat()
            newCenter at 0f
            nextBlock.center at (nextDateDistance * nextPxPerDay).toFloat()
          }
      }
    updateAnchors(dateDraggableState, newAnchors, alignAnchor = newCenter)
  }

  /**
   * Updates the anchors of [state] while keeping the current offset relative to [alignAnchor] the
   * same. This anchor needs to be contained in both the current and the new anchors.
   */
  private fun <T> updateAnchors(
    state: AnchoredDraggableState<T>,
    anchors: DraggableAnchors<T>,
    alignAnchor: T,
  ) {
    assert(anchors.hasPositionFor(alignAnchor)) {
      "The alignAnchor '$alignAnchor' is not contained in the new anchors '$anchors'"
    }
    val oldOffset = state.offset
    val oldAlignAnchorPos = state.anchors.positionOf(alignAnchor)
    state.updateAnchors(anchors, alignAnchor)
    val newOffset = state.offset
    val newAlignAnchorPos = state.anchors.positionOf(alignAnchor)

    val offsetShift = newOffset - oldOffset
    val anchorShift = newAlignAnchorPos - oldAlignAnchorPos
    // effective shift of the offset relative to the alignAnchor
    val relativeOffsetShift = anchorShift - offsetShift
    if (!relativeOffsetShift.isNaN()) state.dispatchRawDelta(relativeOffsetShift)
  }

  /**
   * Sets the representative date of the child [TimeBlock] to which should be navigated when
   * navigating to the child timeline. This method may only be called when the current
   * [NavigationPosition] shows children and the [childBlock] must overlap with the current
   * [NavigationPosition.dateRange].
   *
   * @return If setting the navigation target was successful.
   */
  fun trySetChildNavTarget(childBlock: TimeBlock): Boolean {
    if (!currentNavPos.timelineNavPos.showChild) return false
    val parentChildIntersection = currentTimeBlock intersection childBlock
    if (parentChildIntersection.isEmpty()) return false
    updateDateAnchors(parentChildIntersection.center)
    return true
  }

  /**
   * Animate navigation to the given [childBlock].
   *
   * @return If the navigation was successful.
   * @see trySetChildNavTarget
   */
  suspend fun tryAnimateToChild(childBlock: TimeBlock): Boolean {
    if (trySetChildNavTarget(childBlock)) {
      timelineDraggableState.animateTo(adjacentNavigationPositions.prevTimeline.timelineNavPos)
      return true
    }
    return false
  }

  suspend fun animateToParent() {
    timelineDraggableState.animateTo(TimelineNavPosition(currentTimeline))
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
   * the current [NavigationPosition] transition (not necessarily showing data.
   */
  val visibleTimelineBlocks: ImmutableList<TimelineBlock> by derivedStateOf {
    val (prevPos, nextPos) = navPosTransition.transitionStates()
    val visibleTimelines = buildSet {
      addAll(prevPos.timelineNavPos.visibleTimelines)
      addAll(nextPos.timelineNavPos.visibleTimelines)
    }
    val visibleDateRange =
      prevPos.dateRange.applyMargin(relativeTopMargin, relativeBottomMargin) union
        nextPos.dateRange.applyMargin(relativeTopMargin, relativeBottomMargin)
    buildImmutableList {
      visibleTimelines.forEach { timeline ->
        val firstBlock = timeline.timeBlockFrom(visibleDateRange.start)
        val lastBlock = timeline.timeBlockFrom(visibleDateRange.endInclusive)
        (firstBlock..lastBlock).forEach { add(TimelineBlock(timeline.id, it)) }
      }
    }
  }

  val currentNavPosFlow = snapshotFlow { currentNavPos }
  val currentTimelineBlockFlow = snapshotFlow { currentTimelineBlock }

  /** The visible [TimelineBlock]s that currently are expanded. */
  val focussedTimelineBlocksFlow = snapshotFlow {
    val (prevPos, nextPos) = navPosTransition.transitionStates()
    val prevTimelineBlock = prevPos.timelineBlock
    val nextTimelineBlock = nextPos.timelineBlock
    if (prevTimelineBlock == nextTimelineBlock) listOf(prevTimelineBlock)
    else listOf(prevTimelineBlock, nextTimelineBlock)
  }

  /** The [TimelineBlock]s that currently show some data. */
  val activeTimelineBlocksFlow: Flow<ImmutableList<TimelineBlock>> = snapshotFlow {
    val activeDateRangesByTimeline = mutableMapOf<Timeline, DateRange>()
    navPosTransition.transitionStates().forEachDistinct { navPos ->
      val dateRange = navPos.timeBlock
      navPos.timelineNavPos.visibleTimelines.forEach { timeline ->
        activeDateRangesByTimeline[timeline] =
          activeDateRangesByTimeline.getOrDefault(timeline, dateRange) union dateRange
      }
    }
    buildImmutableList {
      activeDateRangesByTimeline.forEach { timeline, dateRange ->
        addAll(getTimelineBlocks(timeline, dateRange))
      }
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

private fun getTimelineBlocks(timeline: Timeline, dateRange: DateRange): Sequence<TimelineBlock> {
  val firstBlock = timeline.timeBlockFrom(dateRange.start)
  val lastBlock = timeline.timeBlockFrom(dateRange.endInclusive)
  return (firstBlock..lastBlock).map { TimelineBlock(timeline.id, it) }
}
