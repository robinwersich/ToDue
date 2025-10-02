package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.lerp
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.daysUntil
import com.robinwersich.todue.domain.model.size
import com.robinwersich.todue.ui.composeextensions.PaddedRoundedCornerShape
import com.robinwersich.todue.ui.composeextensions.SwipeableTransition
import com.robinwersich.todue.ui.composeextensions.instantStop
import com.robinwersich.todue.ui.composeextensions.modifiers.placeRelative
import com.robinwersich.todue.ui.composeextensions.modifiers.scaleFromSize
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationPosition
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TimelineStyle
import com.robinwersich.todue.ui.presentation.organizer.state.timelineStyle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

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
 * @param contentPadding Padding that should be applied to the focussed area, while still drawing
 *   content within the full bounds.
 * @param taskBlockLabel The label content to display for a [TimeBlock] in preview mode.
 * @param taskBlockContent The content to display for a [TimeBlock] in expanded mode.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerNavigation(
  timelines: ImmutableList<Timeline>,
  modifier: Modifier = Modifier,
  childTimelineSizeFraction: Float = 0.3f,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  taskBlockLabel: @Composable (Timeline, TimeBlock, PaddingValues) -> Unit,
  taskBlockContent: @Composable (Timeline, TimeBlock, PaddingValues) -> Unit,
) {
  val backgroundColor = MaterialTheme.colorScheme.surface
  val positionalThreshold = 0.3f
  val velocityThreshold = with(LocalDensity.current) { 500.dp.toPx() }
  val density = LocalDensity.current

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
  LaunchedEffect(navigationState) {
    launch { navigationState.updateDateAnchorsOnSwipe() }
    launch { navigationState.updateTimelineAnchorsOnSwipe() }
  }

  Box(
    modifier =
      remember(navigationState) {
        modifier
          .fillMaxSize()
          .background(backgroundColor)
          .clipToBounds()
          .padding(contentPadding)
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
          .onSizeChanged {
            with(density) {
              val topPaddingFraction = contentPadding.calculateTopPadding().toPx() / it.height
              val bottomPaddingFraction = contentPadding.calculateBottomPadding().toPx() / it.height
              navigationState.updateViewportSize(it, topPaddingFraction, bottomPaddingFraction)
            }
          }
      }
  ) {
    TaskBlocks(navigationState, taskBlockLabel, taskBlockContent)
  }
}

@Composable
private fun TaskBlocks(
  navigationState: NavigationState,
  taskBlockLabel: @Composable (Timeline, TimeBlock, PaddingValues) -> Unit,
  taskBlockContent: @Composable (Timeline, TimeBlock, PaddingValues) -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()

  for ((timeline, timeBlocks) in navigationState.activeTimelineBlocks) {
    for (timeBlock in timeBlocks) {
      key(timeline, timeBlock) {
        val displayStateTransition =
          navigationState.navPosTransition.derived(cacheStates = true) {
            blockDisplayState(timeBlock, timeline, it, navigationState.childTimelineSizeRatio)
          }
        TaskBlock(
          displayStateTransition = displayStateTransition,
          contentMeasureSizeState =
            blockContentMeasureSize(displayStateTransition) { navigationState.viewportSize },
          onClick = { coroutineScope.launch { navigationState.tryAnimateToChild(timeBlock) } },
          label = { padding -> taskBlockLabel(timeline, timeBlock, padding) },
          content = { padding -> taskBlockContent(timeline, timeBlock, padding) },
        )
      }
    }
  }
}

private val taskBlockPadding = PaddingValues(4.dp)
private val taskBlockCornerRadius = 24.dp

/**
 * Places a task block in the parent composable.
 *
 * @param displayStateTransition A transition containing layout and style information in the form of
 *   a [TaskBlockDisplayState]
 * @param contentMeasureSizeState The size at which the block content should be measured. This is
 *   needed, because this size shouldn't change when the block is entering/exiting the screen to
 *   avoid unnecessary recompositions and visual artifacts.
 * @param onClick Action to invoke when the block is clicked on in child state.
 * @param label The composable content to show in child state.
 * @param content The composable content to show in expanded state.
 */
@Composable
private fun TaskBlock(
  displayStateTransition: SwipeableTransition<TaskBlockDisplayState>,
  contentMeasureSizeState: State<Size?>,
  onClick: () -> Unit,
  label: @Composable (PaddingValues) -> Unit,
  content: @Composable (PaddingValues) -> Unit,
) {
  val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
  val contentColor = MaterialTheme.colorScheme.onSurface

  val relativeOffset by displayStateTransition.interpolatedValue(::lerp) { it.relativeOffset }
  val relativeSize by displayStateTransition.interpolatedValue(::lerp) { it.relativeSize }
  val contentAlphaState = blockContentAlpha(displayStateTransition)
  val contentAlpha by contentAlphaState
  val labelAlphaState = blockLabelAlpha(displayStateTransition)
  val labelAlpha by labelAlphaState
  val showLabel by remember(labelAlphaState) { derivedStateOf { labelAlpha > 0f } }
  val showContent by remember(contentAlphaState) { derivedStateOf { contentAlpha > 0f } }
  val enableChildNavigationClick =
    displayStateTransition.deriveValue { prevState, nextState ->
      prevState.timelineStyle == TimelineStyle.CHILD &&
        nextState.timelineStyle == TimelineStyle.CHILD
    }
  val shape = PaddedRoundedCornerShape(taskBlockCornerRadius, taskBlockPadding)

  Box(
    Modifier.placeRelative({ relativeOffset }, { relativeSize })
      .clip(shape)
      .background(backgroundColor),
    propagateMinConstraints = true,
  ) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
      if (showLabel) {
        Box(
          Modifier.clickable(
              interactionSource = null,
              indication = null,
              enabled = enableChildNavigationClick,
              role = Role.Button,
              onClick = onClick,
            )
            .graphicsLayer { alpha = labelAlpha },
          propagateMinConstraints = true,
        ) {
          label(taskBlockPadding)
        }
      }

      if (showContent) {
        Box(
          Modifier.scaleFromSize { contentMeasureSizeState.value?.roundToIntSize() }
            .graphicsLayer { alpha = contentAlpha },
          propagateMinConstraints = true,
        ) {
          content(taskBlockPadding)
        }
      }
    }
  }
}

private data class TaskBlockDisplayState(
  val timelineStyle: TimelineStyle,
  val isFocussed: Boolean,
  val relativeSize: Size,
  val relativeOffset: Offset,
)

private fun blockDisplayState(
  timeBlock: TimeBlock,
  timeline: Timeline,
  navPos: NavigationPosition,
  childTimelineSizeRatio: Float,
): TaskBlockDisplayState {
  val timelineStyle = timelineStyle(timeline, navPos.timelineNavPos)
  return TaskBlockDisplayState(
    timelineStyle = timelineStyle,
    isFocussed = timeBlock == navPos.timeBlock,
    relativeSize =
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
    relativeOffset =
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

@Composable
private fun blockContentMeasureSize(
  displayStateTransition: SwipeableTransition<TaskBlockDisplayState>,
  organizerSizeState: () -> IntSize?,
) =
  displayStateTransition.interpolatedValue(
    { start, end, progress ->
      if (start == null || end == null) null else lerp(start, end, progress)
    },
    padding = { state, otherState ->
      when {
        state.timelineStyle == TimelineStyle.FULLSCREEN &&
          otherState.timelineStyle == TimelineStyle.CHILD -> 1f
        state.timelineStyle == TimelineStyle.PARENT &&
          state.isFocussed &&
          otherState.timelineStyle != TimelineStyle.FULLSCREEN -> 1f
        else -> 0f
      }
    },
    transform = {
      organizerSizeState()?.let { (organizerWidth, organizerHeight) ->
        Size((it.relativeSize.width * organizerWidth), (it.relativeSize.height * organizerHeight))
      }
    },
  )

@Composable
private fun blockContentAlpha(displayStateTransition: SwipeableTransition<TaskBlockDisplayState>) =
  displayStateTransition.interpolatedValue(
    ::lerp,
    padding = { state, otherState ->
      when {
        state.timelineStyle == TimelineStyle.CHILD &&
          otherState.timelineStyle == TimelineStyle.FULLSCREEN -> 0.8f
        state.timelineStyle == TimelineStyle.FULLSCREEN &&
          state.isFocussed &&
          otherState.timelineStyle == TimelineStyle.FULLSCREEN -> 1f
        state.timelineStyle == TimelineStyle.PARENT &&
          state.isFocussed &&
          otherState.timelineStyle == TimelineStyle.PARENT -> 0.8f
        state.timelineStyle == TimelineStyle.HIDDEN_PARENT &&
          otherState.timelineStyle == TimelineStyle.PARENT -> 0.8f
        else -> 0f
      }
    },
    transform = { if (it.isFocussed) 1f else 0f },
  )

@Composable
private fun blockLabelAlpha(displayStateTransition: SwipeableTransition<TaskBlockDisplayState>) =
  displayStateTransition.interpolatedValue(
    ::lerp,
    padding = { state, otherState ->
      when {
        state.timelineStyle == TimelineStyle.CHILD &&
          otherState.timelineStyle == TimelineStyle.FULLSCREEN -> 0.6f
        else -> 0f
      }
    },
    transform = {
      when (it.timelineStyle) {
        TimelineStyle.CHILD,
        TimelineStyle.HIDDEN_CHILD -> 1f
        else -> 0f
      }
    },
  )
