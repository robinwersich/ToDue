package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.duration
import com.robinwersich.todue.domain.model.toDoubleRange
import com.robinwersich.todue.ui.composeextensions.instantStop
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TimelinePresentation
import com.robinwersich.todue.utility.size
import kotlinx.collections.immutable.ImmutableList

/**
 * A 2-dimensional navigation component that allows the user to navigate through [TimeBlock]s on a
 * time axis (vertical) and a granularity axis (horizontal). To be able to drag tasks between
 * [TimeBlock]s, there is a navigation state which shows a parent [TimeBlock] together with all its
 * children from the next smaller granularity level.
 *
 * @param timelines The timelines (i.e. granularity levels) to display.
 * @param modifier The modifier to apply to this layout.
 * @param childTimelineSizeFraction The fraction of the screen width that the child timeline should
 *   take up in a split view with two timelines.
 * @param timelineBlockContent The content to display for each [TimeBlock] in each [Timeline].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigation(
  timelines: ImmutableList<Timeline>,
  modifier: Modifier = Modifier,
  childTimelineSizeFraction: Float = 0.3f,
  timelineBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
) {
  val positionalThreshold = 0.3f
  val velocityThreshold = with(LocalDensity.current) { 500.dp.toPx() }

  val navigationState =
    remember(timelines, childTimelineSizeFraction) {
      NavigationState(
        timelines = timelines,
        childTimelineSizeRatio = childTimelineSizeFraction,
        positionalThreshold = { it * positionalThreshold },
        velocityThreshold = { velocityThreshold },
        snapAnimationSpec =
          spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Int.VisibilityThreshold.toFloat(),
          ),
        decayAnimationSpec = instantStop(),
      )
    }
  val timelineDraggableState = navigationState.timelineDraggableState
  val dateDraggableState = navigationState.dateDraggableState
  LaunchedEffect(navigationState) { navigationState.updateDateAnchorsOnSwipe() }

  OrganizerNavigationLayout(
    navigationState = navigationState,
    modifier =
      modifier
        .clipToBounds()
        .anchoredDraggable(
          timelineDraggableState,
          orientation = Orientation.Horizontal,
          reverseDirection = true,
        )
        .anchoredDraggable(
          dateDraggableState,
          orientation = Orientation.Vertical,
          reverseDirection = true,
        )
        .onSizeChanged { navigationState.updateViewportSize(it) },
    timelineBlockContent = timelineBlockContent,
  )
}

/** Lays out visible [Timeline]s horizontally based on a [NavigationState]. */
@Composable
private fun OrganizerNavigationLayout(
  navigationState: NavigationState,
  modifier: Modifier = Modifier,
  timelineBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
) {
  Layout(
    content = {
      // reading the activeNavigationPositions here directly somehow fixes the issue of
      // activeTimelineBlocks not updating
      navigationState.activeNavigationPositions
      for ((timeline, timeBlocks) in navigationState.activeTimelineBlocks) {
        TimelineLayout(
          timeBlocks = timeBlocks,
          visibleDateRange = { navigationState.visibleDateTimeRange },
        ) {
          timelineBlockContent(timeline, it)
        }
      }
    },
    modifier = modifier,
    measurePolicy = { measurables, constraints ->
      val placeables =
        measurables.zip(navigationState.activeTimelineBlocks) { measurable, (timeline, _) ->
          val relativeWidth =
            navigationState.timelinePresentationTransitions[timeline]!!.interpolateFloat {
              when (it) {
                TimelinePresentation.CHILD,
                TimelinePresentation.HIDDEN_CHILD -> navigationState.childTimelineSizeRatio
                TimelinePresentation.FULLSCREEN -> 1f
                TimelinePresentation.PARENT,
                TimelinePresentation.HIDDEN_PARENT -> 1f - navigationState.childTimelineSizeRatio
              }
            }
          val width = (relativeWidth * constraints.maxWidth).toInt()
          measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        }
      layout(width = constraints.maxWidth, height = constraints.maxHeight) {
        placeables.zip(navigationState.activeTimelineBlocks) { placeable, (timeline, _) ->
          val relativeOffset =
            navigationState.timelinePresentationTransitions[timeline]!!.interpolateFloat {
              when (it) {
                TimelinePresentation.HIDDEN_CHILD -> -navigationState.childTimelineSizeRatio
                TimelinePresentation.CHILD,
                TimelinePresentation.FULLSCREEN -> 0f
                TimelinePresentation.PARENT -> navigationState.childTimelineSizeRatio
                TimelinePresentation.HIDDEN_PARENT -> 1f
              }
            }
          val offset = (relativeOffset * constraints.maxWidth).toInt()
          placeable.place(offset, 0)
        }
      }
    },
  )
}

/** Lays out [TimeBlock]s vertically based on the currently visible date range. */
@Composable
private fun TimelineLayout(
  timeBlocks: ImmutableList<TimeBlock>,
  visibleDateRange: () -> ClosedRange<Double>,
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
          val relativeSize = timeBlock.duration / timelineRange.size
          val itemSize = (relativeSize * constraints.maxHeight).toInt()
          measurable.measure(constraints.copy(minHeight = itemSize, maxHeight = itemSize))
        }

      layout(width = constraints.maxWidth, height = constraints.maxHeight) {
        placeables.zip(timeBlocks) { placeable, timeBlock ->
          val relativeOffset =
            (timeBlock.toDoubleRange().start - timelineRange.start) / timelineRange.size
          val itemOffset = (relativeOffset * constraints.maxHeight).toInt()
          placeable.place(0, itemOffset)
        }
      }
    },
  )
