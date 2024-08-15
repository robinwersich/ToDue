package com.robinwersich.todue.ui.presentation.organizer.components

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.duration
import com.robinwersich.todue.domain.model.toDoubleRange
import com.robinwersich.todue.ui.composeextensions.Eq
import com.robinwersich.todue.ui.composeextensions.Leq
import com.robinwersich.todue.ui.composeextensions.Lt
import com.robinwersich.todue.ui.composeextensions.Near
import com.robinwersich.todue.ui.composeextensions.instantStop
import com.robinwersich.todue.ui.composeextensions.interpolatedFloat
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TimelineStyle
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
 * @param previewTimeBlockContent The content to display for a [TimeBlock] in preview mode.
 * @param expandedTimeBlockContent The content to display for a [TimeBlock] in expanded mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigation(
  timelines: ImmutableList<Timeline>,
  modifier: Modifier = Modifier,
  childTimelineSizeFraction: Float = 0.3f,
  previewTimeBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
  expandedTimeBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
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

  NewOrganizerNavigationLayout(
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
    previewTimeBlockContent = previewTimeBlockContent,
    expandedTimeBlockContent = expandedTimeBlockContent,
  )
}

@Composable
fun NewOrganizerNavigationLayout(
  navigationState: NavigationState,
  modifier: Modifier = Modifier,
  previewTimeBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
  expandedTimeBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
) {
  Box(modifier.fillMaxSize()) {
    for ((timeline, timeBlocks) in navigationState.activeTimelineBlocks) {
      val timelineStyleTransition = navigationState.timelineStyleTransitions.getValue(timeline)
      val timelineWidth by
        timelineStyleTransition.interpolatedFloat(aggregate = true) {
          when (it) {
            TimelineStyle.HIDDEN_CHILD,
            TimelineStyle.CHILD -> navigationState.childTimelineSizeRatio
            TimelineStyle.FULLSCREEN -> 1f
            TimelineStyle.PARENT,
            TimelineStyle.HIDDEN_PARENT -> 1f - navigationState.childTimelineSizeRatio
          }
        }
      val timelineOffset by
        timelineStyleTransition.interpolatedFloat(aggregate = true) {
          when (it) {
            TimelineStyle.HIDDEN_CHILD -> -navigationState.childTimelineSizeRatio
            TimelineStyle.CHILD,
            TimelineStyle.FULLSCREEN -> 0f
            TimelineStyle.PARENT -> navigationState.childTimelineSizeRatio
            TimelineStyle.HIDDEN_PARENT -> 1f
          }
        }

      for (timeBlock in timeBlocks) {
        val blockHeightValue =
          navigationState.visibleDateRange.derived { (timeBlock.duration / it.size).toFloat() }
        val blockHeight by blockHeightValue
        val blockOffset by
          navigationState.visibleDateRange.derived {
            ((timeBlock.toDoubleRange().start - it.start) / it.size).toFloat()
          }
        if (
          timelineStyleTransition.isState(Eq(TimelineStyle.FULLSCREEN)) ||
            navigationState.currentDate in timeBlock &&
              timelineStyleTransition.isState(
                Near(TimelineStyle.FULLSCREEN),
                Near(TimelineStyle.PARENT),
              )
        ) {
          Box(
            Modifier.layout { measurable, constraints ->
              val width =
                if (timelineStyleTransition.isState(Near(TimelineStyle.PARENT))) timelineWidth
                else 1f
              val height =
                when {
                  timelineStyleTransition.isState(Lt(TimelineStyle.FULLSCREEN)) -> blockHeight
                  timelineStyleTransition.isState(Leq(TimelineStyle.PARENT)) ->
                    blockHeightValue.at(1f)
                  else -> 1f
                }
              val placeable =
                measurable.measure(
                  Constraints.fixed(
                    (width * constraints.maxWidth).toInt(),
                    (height * constraints.maxHeight).toInt(),
                  )
                )
              layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.placeRelativeWithLayer(
                  (timelineOffset * constraints.maxWidth).toInt(),
                  (blockOffset * constraints.maxHeight).toInt(),
                ) {
                  transformOrigin = TransformOrigin(0f, 0f)
                  scaleX = timelineWidth / width
                  scaleY = blockHeight / height
                }
              }
            }
          ) {
            expandedTimeBlockContent(timeline, timeBlock)
          }
        }
        Box(
          Modifier.layout { measurable, constraints ->
            val width = (timelineWidth * constraints.maxWidth).toInt()
            val height = (blockHeight * constraints.maxHeight).toInt()
            val placeable = measurable.measure(Constraints.fixed(width, height))
            layout(constraints.maxWidth, constraints.maxHeight) {
              placeable.placeRelative(
                (timelineOffset * constraints.maxWidth).toInt(),
                (blockOffset * constraints.maxHeight).toInt(),
              )
            }
          }
        ) {
          previewTimeBlockContent(timeline, timeBlock)
        }
      }
    }
  }
}
