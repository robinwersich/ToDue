package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.lerp
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.daysUntil
import com.robinwersich.todue.domain.model.size
import com.robinwersich.todue.ui.composeextensions.DeriveScope
import com.robinwersich.todue.ui.composeextensions.PaddedRoundedCornerShape
import com.robinwersich.todue.ui.composeextensions.SwipeableTransition
import com.robinwersich.todue.ui.composeextensions.anchoredDraggableWithNestedScroll
import com.robinwersich.todue.ui.composeextensions.drawShape
import com.robinwersich.todue.ui.composeextensions.modifiers.placeRelative
import com.robinwersich.todue.ui.composeextensions.modifiers.scaleFromSize
import com.robinwersich.todue.ui.composeextensions.reversed
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationPosition
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TimelineStyle
import com.robinwersich.todue.ui.presentation.organizer.state.timelineStyle
import androidx.compose.ui.graphics.lerp as colorLerp

enum class TaskBlockContentMode {
  /** Block occupies the whole viewport. */
  FULLSCREEN,
  /** Block is parent in split view. */
  PARENT;

  val isFullscreen
    get() = this == FULLSCREEN

  val isParent
    get() = this == PARENT
}

private val taskBlockCornerRadius = 12.dp
private val taskBlockGapSize = 4.dp
private val taskBlockPaddingValues = PaddingValues(taskBlockGapSize / 2)

/**
 * A 2-dimensional navigation component that allows the user to navigate through [TimelineBlock]s on
 * a time axis (vertical) and a granularity axis (horizontal). To be able to drag tasks between
 * [TimelineBlock]s, there is a navigation state which shows a parent [TimelineBlock] together with
 * all its children from the next smaller granularity level.
 *
 * @param navigationState The [NavigationState] containing info about layout and current position.
 * @param modifier The modifier to apply to this layout.
 * @param contentPadding Padding that should be applied to the focussed area, while still drawing
 *   content within the full bounds.
 * @param taskBlockLabel The label content to display for a [TimelineBlock] in preview mode.
 * @param taskBlockContent The content to display for a [TimelineBlock] in expanded mode.
 */
@Composable
fun OrganizerNavigation(
  navigationState: NavigationState,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
  taskBlockLabel: @Composable (TimelineBlock, PaddingValues) -> Unit,
  taskBlockContent: @Composable (TimelineBlock, TaskBlockContentMode, PaddingValues) -> Unit,
) {
  val backgroundColor = MaterialTheme.colorScheme.surface
  val density = LocalDensity.current

  val timelineDraggableState = navigationState.timelineDraggableState
  val dateDraggableState = navigationState.dateDraggableState

  val overscrollEffect = rememberOverscrollEffect()?.reversed()
  Box(
    modifier =
      remember(navigationState) {
        modifier
          .fillMaxSize()
          .background(backgroundColor)
          .overscroll(overscrollEffect)
          .clipToBounds()
          .padding(contentPadding + PaddingValues(horizontal = taskBlockGapSize / 2))
          .anchoredDraggable(
            timelineDraggableState,
            orientation = Orientation.Horizontal,
            reverseDirection = true,
            overscrollEffect = overscrollEffect,
          )
          .anchoredDraggableWithNestedScroll(
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
  taskBlockLabel: @Composable (TimelineBlock, PaddingValues) -> Unit,
  taskBlockContent: @Composable (TimelineBlock, TaskBlockContentMode, PaddingValues) -> Unit,
) {
  val navigationAnimationScope = rememberCoroutineScope()

  for (timelineBlock in navigationState.visibleTimelineBlocks) {
    key(timelineBlock) {
      TaskBlock(
        navigationState = navigationState,
        timelineBlock = timelineBlock,
        navigationAnimationScope = navigationAnimationScope,
        label = { padding -> taskBlockLabel(timelineBlock, padding) },
        content = { mode, padding -> taskBlockContent(timelineBlock, mode, padding) },
      )
    }
  }
}

@Composable
private fun TaskBlock(
  navigationState: NavigationState,
  timelineBlock: TimelineBlock,
  navigationAnimationScope: CoroutineScope,
  label: @Composable (PaddingValues) -> Unit,
  content: @Composable (TaskBlockContentMode, PaddingValues) -> Unit,
) {
  val displayStateTransition =
    navigationState.navPosTransition.derived(cacheStates = true) {
      blockDisplayState(timelineBlock, navigationState.childTimelineSizeRatio)
    }

  // size for measuring shouldn't change when the block is entering/exiting the screen to
  // avoid unnecessary recompositions and visual artifacts.
  val contentMeasureSize by
    blockContentMeasureSize(displayStateTransition) { navigationState.viewportSize }
  val relativeOffset by displayStateTransition.interpolatedValue(::lerp) { it.relativeOffset }
  val relativeSize by displayStateTransition.interpolatedValue(::lerp) { it.relativeSize }
  val contentAlphaState = blockContentAlpha(displayStateTransition)
  val contentAlpha by contentAlphaState
  val labelAlphaState = blockLabelAlpha(displayStateTransition)
  val labelAlpha by labelAlphaState
  val showLabel by remember(labelAlphaState) { derivedStateOf { labelAlpha > 0f } }
  val showContent by remember(contentAlphaState) { derivedStateOf { contentAlpha > 0f } }
  val clickTarget by displayStateTransition.derivedValue { prevState, nextState ->
    when {
      prevState.timelineStyle == TimelineStyle.CHILD &&
        nextState.timelineStyle == TimelineStyle.CHILD -> TaskBlockClickTarget.CHILD
      prevState.timelineStyle == TimelineStyle.PARENT &&
        nextState.timelineStyle == TimelineStyle.PARENT -> TaskBlockClickTarget.PARENT
      else -> TaskBlockClickTarget.NONE
    }
  }
  val contentMode by
    displayStateTransition
      .derived {
        when (current.timelineStyle) {
          TimelineStyle.HIDDEN_CHILD,
          TimelineStyle.CHILD,
          TimelineStyle.FULLSCREEN -> TaskBlockContentMode.FULLSCREEN
          TimelineStyle.PARENT,
          TimelineStyle.HIDDEN_PARENT -> TaskBlockContentMode.PARENT
        }
      }
      .derivedValue(useState = true) { prevState, nextState ->
        if (prevState.isFullscreen && nextState.isFullscreen) TaskBlockContentMode.FULLSCREEN
        else TaskBlockContentMode.PARENT
      }
  val shape = PaddedRoundedCornerShape(taskBlockCornerRadius, taskBlockPaddingValues)
  val style by blockStyle(displayStateTransition, timelineBlock)

  Box(
    Modifier.placeRelative({ relativeOffset }, { relativeSize })
      .drawShape(
        shape,
        backgroundColor = { style.backgroundColor },
        borderColor = { style.borderColor },
        borderWidth = { 0.5.dp },
      )
      .clip(shape)
      .clickable(
        interactionSource = null,
        indication = null,
        enabled = clickTarget != TaskBlockClickTarget.NONE,
        role = Role.Button,
      ) {
        navigationAnimationScope.launch {
          when (clickTarget) {
            TaskBlockClickTarget.CHILD -> navigationState.tryAnimateToChild(timelineBlock.section)
            TaskBlockClickTarget.PARENT -> navigationState.animateToParent()
            TaskBlockClickTarget.NONE -> {}
          }
        }
      },
    propagateMinConstraints = true,
  ) {
    if (showLabel) {
      Box(
        Modifier.graphicsLayer { alpha = labelAlpha },
        propagateMinConstraints = true,
      ) {
        CompositionLocalProvider(LocalContentColor provides style.labelColor) {
          label(taskBlockPaddingValues)
        }
      }
    }

    if (showContent) {
      Box(
        Modifier.scaleFromSize { contentMeasureSize?.roundToIntSize() }
          .graphicsLayer { alpha = contentAlpha },
        propagateMinConstraints = true,
      ) {
        content(contentMode, taskBlockPaddingValues)
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

private fun DeriveScope<NavigationPosition>.blockDisplayState(
  timelineBlock: TimelineBlock,
  childTimelineSizeRatio: Float,
): TaskBlockDisplayState {
  val timelineId = timelineBlock.timelineId
  val timeBlock = timelineBlock.section
  val timelineStyle = timelineStyle(timelineId, current.timelineNavPos)

  fun NavigationPosition.relativeHeight() = timeBlock.size.toFloat() / dateRange.size.toFloat()

  val relativeHeight = current.relativeHeight()
  val relativeWidth =
    when (timelineStyle) {
      TimelineStyle.HIDDEN_CHILD,
      TimelineStyle.CHILD -> childTimelineSizeRatio
      TimelineStyle.FULLSCREEN -> 1f
      TimelineStyle.PARENT -> 1f - childTimelineSizeRatio
      TimelineStyle.HIDDEN_PARENT ->
        (1f - childTimelineSizeRatio) / other.relativeHeight() * relativeHeight
    }

  val relativeOffsetLeft =
    when (timelineStyle) {
      TimelineStyle.HIDDEN_CHILD -> -childTimelineSizeRatio
      TimelineStyle.CHILD,
      TimelineStyle.FULLSCREEN -> 0f
      TimelineStyle.PARENT -> childTimelineSizeRatio
      TimelineStyle.HIDDEN_PARENT -> 1f
    }
  val relativeOffsetTop =
    current.dateRange.start.daysUntil(timeBlock.start) / current.dateRange.size.toFloat()

  return TaskBlockDisplayState(
    timelineStyle = timelineStyle,
    isFocussed = timelineBlock == current.timelineBlock,
    relativeSize = Size(relativeWidth, relativeHeight),
    relativeOffset = Offset(relativeOffsetLeft, relativeOffsetTop),
  )
}

private data class TaskBlockStyle(
  val backgroundColor: Color,
  val borderColor: Color,
  val labelColor: Color,
)

@Composable
private fun blockStyle(
  displayStateTransition: SwipeableTransition<TaskBlockDisplayState>,
  timelineBlock: TimelineBlock,
  today: LocalDate = LocalDate.now(),
): State<TaskBlockStyle> {
  val colorScheme = MaterialTheme.colorScheme
  val timeBlock = timelineBlock.section

  return displayStateTransition.interpolatedValue(
    lerp = { start, end, fraction ->
      TaskBlockStyle(
        backgroundColor = colorLerp(start.backgroundColor, end.backgroundColor, fraction),
        borderColor = colorLerp(start.borderColor, end.borderColor, fraction),
        labelColor = colorLerp(start.labelColor, end.labelColor, fraction),
      )
    },
    padding = ::blockLabelTransitionPadding,
  ) { state ->
    val labelColor =
      when {
        timeBlock.endInclusive < today -> colorScheme.onSurfaceVariant
        today < timeBlock.start -> colorScheme.onSurface
        else -> colorScheme.onPrimary
      }
    when (state.timelineStyle) {
      TimelineStyle.CHILD,
      TimelineStyle.HIDDEN_CHILD -> {
        TaskBlockStyle(
          backgroundColor =
            when {
              timeBlock.endInclusive < today -> colorScheme.surfaceContainer
              today < timeBlock.start -> colorScheme.surfaceContainerHigh
              else -> colorScheme.primary
            },
          borderColor = Color.Transparent,
          labelColor = labelColor,
        )
      }
      else ->
        TaskBlockStyle(
          backgroundColor = colorScheme.surfaceContainerLowest,
          borderColor = if (state.isFocussed) colorScheme.outline else colorScheme.outlineVariant,
          labelColor = labelColor,
        )
    }
  }
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
    padding = ::blockLabelTransitionPadding,
    transform = {
      when (it.timelineStyle) {
        TimelineStyle.CHILD,
        TimelineStyle.HIDDEN_CHILD -> 1f
        else -> 0f
      }
    },
  )

private fun blockLabelTransitionPadding(
  state: TaskBlockDisplayState,
  otherState: TaskBlockDisplayState,
): Float =
  when {
    state.timelineStyle == TimelineStyle.CHILD &&
      otherState.timelineStyle == TimelineStyle.FULLSCREEN -> 0.4f
    state.timelineStyle == TimelineStyle.FULLSCREEN &&
      otherState.timelineStyle == TimelineStyle.CHILD -> 0.1f
    else -> 0f
  }

private enum class TaskBlockClickTarget {
  CHILD,
  PARENT,
  NONE,
}
