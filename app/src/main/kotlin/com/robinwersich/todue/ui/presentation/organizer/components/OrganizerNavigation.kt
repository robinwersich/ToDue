package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.daysUntil
import com.robinwersich.todue.domain.model.size
import com.robinwersich.todue.ui.composeextensions.SwipeableTransition
import com.robinwersich.todue.ui.composeextensions.instantStop
import com.robinwersich.todue.ui.composeextensions.modifiers.placeRelative
import com.robinwersich.todue.ui.composeextensions.modifiers.placeRelativeScaling
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationPosition
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TimelineStyle
import com.robinwersich.todue.ui.presentation.organizer.state.timelineStyle
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
 * @param taskBlockLabel The label content to display for a [TimeBlock] in preview mode.
 * @param taskBlockContent The content to display for a [TimeBlock] in expanded mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigation(
  timelines: ImmutableList<Timeline>,
  modifier: Modifier = Modifier,
  childTimelineSizeFraction: Float = 0.3f,
  taskBlockLabel: @Composable (Timeline, TimeBlock) -> Unit,
  taskBlockContent: @Composable (Timeline, TimeBlock) -> Unit,
) {
  val backgroundColor = MaterialTheme.colorScheme.surface
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

  Box(
    modifier =
      remember(navigationState) {
        modifier
          .fillMaxSize()
          .background(backgroundColor)
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
          .onSizeChanged { navigationState.updateViewportSize(it) }
      }
  ) {
    for ((timeline, timeBlocks) in navigationState.activeTimelineBlocks) {
      for (timeBlock in timeBlocks) {
        TaskBlock(
          navigationState = navigationState,
          timeBlock = timeBlock,
          timeline = timeline,
          label = { taskBlockLabel(timeline, timeBlock) },
          content = { taskBlockContent(timeline, timeBlock) },
        )
      }
    }
  }
}

private data class TaskBlockDisplayState(
  val timelineStyle: TimelineStyle,
  val isFocussed: Boolean,
  val size: Size,
  val offset: Offset,
)

private fun taskBlockDisplayState(
  timeBlock: TimeBlock,
  timeline: Timeline,
  navPos: NavigationPosition,
  childTimelineSizeRatio: Float,
): TaskBlockDisplayState {
  val timelineStyle = timelineStyle(timeline, navPos.timelineNavPos)
  return TaskBlockDisplayState(
    timelineStyle = timelineStyle,
    isFocussed = timeBlock == navPos.timeBlock,
    size =
      Size(
        width =
          when (timelineStyle) {
            TimelineStyle.HIDDEN_CHILD,
            TimelineStyle.CHILD -> childTimelineSizeRatio
            TimelineStyle.FULLSCREEN -> 1f
            TimelineStyle.PARENT,
            TimelineStyle.HIDDEN_PARENT -> 1f - childTimelineSizeRatio
          },
        height = timeBlock.size.toFloat() / navPos.dateRange.size.toFloat(),
      ),
    offset =
      Offset(
        x =
          when (timelineStyle) {
            TimelineStyle.HIDDEN_CHILD -> -childTimelineSizeRatio
            TimelineStyle.CHILD,
            TimelineStyle.FULLSCREEN -> 0f
            TimelineStyle.PARENT -> childTimelineSizeRatio
            TimelineStyle.HIDDEN_PARENT -> 1f
          },
        y = (navPos.dateRange.start.daysUntil(timeBlock.start) / navPos.dateRange.size.toFloat()),
      ),
  )
}

private val taskBlockPadding = 4.dp
private val taskBlockCornerRadius = 24.dp

@Composable
private fun TaskBlock(
  navigationState: NavigationState,
  timeBlock: TimeBlock,
  timeline: Timeline,
  label: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
  val contentColor = MaterialTheme.colorScheme.onSurface

  val displayStateTransition =
    navigationState.navPosTransition.derived(cacheStates = true) {
      taskBlockDisplayState(timeBlock, timeline, it, navigationState.childTimelineSizeRatio)
    }

  val offset by displayStateTransition.interpolatedValue(::lerp) { it.offset }
  val size by displayStateTransition.interpolatedValue(::lerp) { it.size }
  // When entering/exiting the screen, the size at which the blocks are measured shouldn't change
  // to avoid unnecessary recompositions and visual artifacts. They will be scaled instead.
  val measureSize by
    displayStateTransition.interpolatedValue(
      ::lerp,
      padding = { state, otherState ->
        when {
          state.timelineStyle == TimelineStyle.FULLSCREEN &&
            otherState.timelineStyle == TimelineStyle.CHILD -> 1f
          state.timelineStyle == TimelineStyle.PARENT &&
            otherState.timelineStyle != TimelineStyle.FULLSCREEN -> 1f
          else -> 0f
        }
      },
      transform = { it.size },
    )

  val expandedAlphaState =
    displayStateTransition.interpolatedValue(
      ::lerp,
      padding = { state, otherState ->
        when {
          state.timelineStyle == TimelineStyle.CHILD &&
            otherState.timelineStyle == TimelineStyle.FULLSCREEN -> 0.8f
          state.timelineStyle == TimelineStyle.HIDDEN_PARENT &&
            otherState.timelineStyle == TimelineStyle.PARENT -> 0.8f
          state.timelineStyle == TimelineStyle.PARENT &&
            state.isFocussed &&
            otherState.timelineStyle == TimelineStyle.PARENT &&
            !otherState.isFocussed -> 0.8f
          else -> 0f
        }
      },
      transform = {
        when {
          it.timelineStyle == TimelineStyle.FULLSCREEN -> 1f
          it.timelineStyle == TimelineStyle.PARENT && it.isFocussed -> 1f
          else -> 0f
        }
      },
    )
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    TaskBlockContainer(
      displayStateTransition = displayStateTransition,
      expandedAlphaState = expandedAlphaState,
      color = backgroundColor,
      modifier = Modifier.placeRelative({ offset }, { size }),
      label = label,
    )

    Box(
      Modifier.placeRelativeScaling(
          { offset },
          { size },
          { measureSize },
          PaddingValues(taskBlockPadding),
        )
        .graphicsLayer {
          shape = RoundedCornerShape(taskBlockCornerRadius)
          clip = true
          alpha = expandedAlphaState.value
        },
      propagateMinConstraints = true,
    ) {
      val showDetails by
        remember(expandedAlphaState) { derivedStateOf { expandedAlphaState.value > 0f } }
      if (showDetails) {
        content()
      }
    }
  }
}

@Composable
private fun TaskBlockContainer(
  displayStateTransition: SwipeableTransition<TaskBlockDisplayState>,
  expandedAlphaState: State<Float>,
  color: Color,
  modifier: Modifier = Modifier,
  label: @Composable () -> Unit,
) {
  val showBlockBounds =
    displayStateTransition.derived(
      padding = { _, otherState ->
        when {
          otherState.timelineStyle == TimelineStyle.FULLSCREEN && otherState.isFocussed -> 0.9f
          else -> 0f
        }
      },
      manyToOne = true,
    ) {
      it.timelineStyle != TimelineStyle.FULLSCREEN || !it.isFocussed
    }
  val padding by showBlockBounds.interpolatedValue(::lerp) { if (it) taskBlockPadding else 0.dp }
  val cornerRadius by
    showBlockBounds.interpolatedValue(::lerp) { if (it) taskBlockCornerRadius else 0.dp }
  val showLabel by remember(expandedAlphaState) { derivedStateOf { expandedAlphaState.value < 1f } }

  Box(
    modifier
      .drawBehind {
        val paddingPx = padding.toPx()
        drawRoundRect(
          color,
          topLeft = Offset(x = paddingPx, y = paddingPx),
          size = Size(width = size.width - 2 * paddingPx, height = size.height - 2 * paddingPx),
          cornerRadius = CornerRadius(cornerRadius.toPx()),
        )
      }
      .padding(taskBlockPadding)
      .graphicsLayer { alpha = 1f - expandedAlphaState.value },
    contentAlignment = Alignment.Center,
  ) {
    if (showLabel) {
      label()
    }
  }
}
