package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.domain.model.DateTimeRange
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.duration
import com.robinwersich.todue.domain.model.toDateTimeRange
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.utility.interpolateFloat
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigation(
  timelines: ImmutableList<Timeline>,
  modifier: Modifier = Modifier,
  childTimelineSizeFraction: Float = 0.3f,
  timelineBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
) {
  val positionalThreshold = 0.0f
  val velocityThreshold = with(LocalDensity.current) { 100.dp.toPx() }

  val navigationState =
    remember(timelines, childTimelineSizeFraction) {
      NavigationState(
        timelines = timelines,
        childTimelineSizeRatio = childTimelineSizeFraction,
        positionalThreshold = { it * positionalThreshold },
        velocityThreshold = { velocityThreshold },
        snapAnimationSpec = tween(),
        decayAnimationSpec = exponentialDecay(),
      )
    }
  val timelineDraggableState = navigationState.timelineDraggableState
  val dateDraggableState = navigationState.dateDraggableState
  LaunchedEffect(navigationState) { navigationState.updateDateAnchorsOnSwipe() }

  OrganizerNavigationLayout(
    navigationState = navigationState,
    modifier =
      modifier
        .anchoredDraggable(timelineDraggableState, Orientation.Horizontal)
        .anchoredDraggable(dateDraggableState, Orientation.Vertical)
        .onSizeChanged { navigationState.updateViewportSize(it) },
    timelineBlockContent = timelineBlockContent,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigationLayout(
  navigationState: NavigationState,
  modifier: Modifier = Modifier,
  timelineBlockContent: @Composable (timeline: Timeline, timeBlock: TimeBlock) -> Unit,
) =
  Layout(
    content = {
      for ((timeline, timeBlocks) in navigationState.activeTimelineBlocks) {
        TimelineLayout(
          timeBlocks = timeBlocks,
          visibleDateRange = { navigationState.visibleDateTimeRange },
        ) {
          timelineBlockContent(timeline, it)
        }
      }
    },
    modifier = modifier.clipScrollableContainer(Orientation.Vertical),
    measurePolicy = { measurables, constraints ->
      val placeables =
        measurables.zip(navigationState.activeTimelineBlocks) { measurable, (timeline, _) ->
          val relativeWidth =
            navigationState.timelineDraggableState.interpolateFloat { navPos ->
              when {
                timeline < navPos.timeline -> navigationState.childTimelineSizeRatio
                timeline == navPos.timeline && !navPos.showChild -> 1f
                else -> 1 - navigationState.childTimelineSizeRatio
              }
            }
          val width = (relativeWidth * constraints.maxWidth).toInt()
          measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        }
      layout(width = constraints.maxWidth, height = constraints.maxHeight) {
        placeables.zip(navigationState.activeTimelineBlocks) { placeable, (timeline, _) ->
          val relativeOffset =
            navigationState.timelineDraggableState.interpolateFloat { navPos ->
              when {
                timeline < navPos.visibleTimelines.first() ->
                  -navigationState.childTimelineSizeRatio
                timeline == navPos.visibleTimelines.first() -> 0f
                timeline == navPos.timeline && navPos.showChild ->
                  navigationState.childTimelineSizeRatio
                else -> 1f
              }
            }
          val offset = (relativeOffset * constraints.maxWidth).toInt()
          placeable.place(offset, 0)
        }
      }
    },
  )

@Composable
fun TimelineLayout(
  timeBlocks: ImmutableList<TimeBlock>,
  visibleDateRange: () -> DateTimeRange,
  modifier: Modifier = Modifier,
  timeBlockContent: @Composable (timeBlock: TimeBlock) -> Unit,
) =
  Layout(
    content = { for (timeBlock in timeBlocks) timeBlockContent(timeBlock) },
    modifier = modifier,
    measurePolicy = { measurables, constraints ->
      val timelineRange = visibleDateRange()
      val placeables =
        measurables.zip(timeBlocks) { measurable, timeBlock ->
          val relativeSize = timeBlock.duration / timelineRange.duration
          val itemSize = (relativeSize * constraints.maxHeight).toInt()
          measurable.measure(constraints.copy(minHeight = itemSize, maxHeight = itemSize))
        }

      layout(width = constraints.maxWidth, height = constraints.maxHeight) {
        placeables.zip(timeBlocks) { placeable, timeBlock ->
          val relativeOffset =
            (timeBlock.toDateTimeRange().start - timelineRange.start) / timelineRange.duration
          val itemOffset = (relativeOffset * constraints.maxHeight).toInt()
          placeable.place(0, itemOffset)
        }
      }
    },
  )
