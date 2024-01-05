package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.ui.presentation.organizer.components.TaskView
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.ui.utility.animateFloat
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerScreen(state: OrganizerState, onEvent: (OrganizerEvent) -> Unit = {}) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.surface,
    floatingActionButton = {
      FloatingActionButton(onClick = { onEvent(AddTask) }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
      val positionalThreshold = 0.0f
      val velocityThreshold = with(LocalDensity.current) { 100.dp.toPx() }
      val childTimelineSizeFraction = 0.3f
      val maxWidth = constraints.maxWidth
      val maxHeight = constraints.maxHeight
      val timelines = listOf(Timeline(0, TimeUnit.DAY), Timeline(1, TimeUnit.WEEK))

      val navigationState = remember {
        NavigationState(
          timelines = timelines,
          organizerSize = IntSize(maxWidth, maxHeight),
          timelinePositionalThreshold = { it * positionalThreshold },
          timelineVelocityThreshold = { velocityThreshold },
          childTimelineSizeFraction = childTimelineSizeFraction,
        )
      }
      // FIXME: this might cause issues because initial state has no timelines and size
      // TODO: use onSizeChanged
      SideEffect {
        navigationState.update(
          newTimelines = timelines,
          newOrganizerSize = IntSize(maxWidth, maxHeight)
        )
      }

      Box(
        modifier =
          Modifier.fillMaxSize()
            .anchoredDraggable(
              navigationState.timelineDraggableState,
              orientation = Orientation.Horizontal
            )
      ) {
        for (timeline in timelines) {
          val sizeFraction by
            navigationState.timelineDraggableState.animateFloat { visibleTimelines ->
              when (visibleTimelines) {
                is VisibleTimelines.Single ->
                  if (timeline < visibleTimelines.timeline) childTimelineSizeFraction else 1f
                is VisibleTimelines.Double ->
                  if (timeline <= visibleTimelines.child) childTimelineSizeFraction else 1f
              }
            }
          val offsetFraction by
            navigationState.timelineDraggableState.animateFloat { visibleTimelines ->
              when (visibleTimelines) {
                is VisibleTimelines.Single ->
                  if (timeline < visibleTimelines.timeline) -childTimelineSizeFraction
                  else if (timeline > visibleTimelines.timeline) 1f else 0f
                is VisibleTimelines.Double ->
                  if (timeline < visibleTimelines.child) -childTimelineSizeFraction
                  else if (timeline > visibleTimelines.parent) 1f
                  else if (timeline == visibleTimelines.parent) childTimelineSizeFraction else 0f
              }
            }
          Box(
            modifier =
              Modifier.fillMaxHeight()
                .fillMaxWidth(sizeFraction)
                .offset { IntOffset((offsetFraction * maxWidth).toInt(), 0) }
                .padding(10.dp)
                .background(Color.DarkGray)
          )
        }
      }
    }

    /*TaskList(
      tasks = state.tasks,
      onEvent = { onEvent(it) }, // TODO: use method reference once this doesn't cause recomposition
      modifier = Modifier.padding(paddingValues).fillMaxHeight(),
    )*/
  }
}

@Composable
private fun TaskList(
  tasks: ImmutableList<TaskViewState>,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  val interactionSource = remember { MutableInteractionSource() }
  val taskListModifier =
    remember(modifier, onEvent) {
      modifier.clickable(interactionSource = interactionSource, indication = null) {
        onEvent(CollapseTasks)
      }
    }
  LazyColumn(modifier = taskListModifier.padding(8.dp)) {
    items(items = tasks, key = { it.id }) { taskState ->
      // extract task ID so that onEvent stays the same, avoiding recomposition
      val taskId = taskState.id
      TaskView(
        state = taskState,
        onEvent = { onEvent(ModifyTask(it, taskId)) },
        modifier =
          remember(taskState.id, taskState.focusLevel, onEvent, interactionSource) {
            when (taskState.focusLevel) {
              FocusLevel.FOCUSSED ->
                Modifier.clickable(interactionSource = interactionSource, indication = null) {}
              FocusLevel.NEUTRAL ->
                Modifier.clickable(interactionSource = interactionSource, indication = null) {
                  onEvent(ExpandTask(taskState.id))
                }
              FocusLevel.BACKGROUND -> Modifier
            }
          },
      )
    }
  }
}

private fun taskStateList(size: Int, focussedTask: Int? = null): List<TaskViewState> {
  return List(size) {
    val focusLevel =
      when (focussedTask) {
        null -> FocusLevel.NEUTRAL
        it -> FocusLevel.FOCUSSED
        else -> FocusLevel.BACKGROUND
      }
    TaskViewState(it.toLong(), "Task $it", dueDate = LocalDate.now(), focusLevel = focusLevel)
  }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(OrganizerState(tasks = taskStateList(size = 5).toImmutableList())) }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenWithFocussedPreview() {
  ToDueTheme {
    OrganizerScreen(
      OrganizerState(tasks = taskStateList(size = 5, focussedTask = 3).toImmutableList())
    )
  }
}
