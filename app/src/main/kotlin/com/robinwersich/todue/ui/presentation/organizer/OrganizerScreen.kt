package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskView
import com.robinwersich.todue.ui.theme.ToDueTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerScreen(state: OrganizerState, onEvent: (OrganizerEvent) -> Unit = {}) {
  //  val anchoredDraggableState = remember {
  //    AnchoredDraggableState(
  //      initialValue = LocalDate.now(),
  //      snapAnimationSpec = tween(),
  //      decayAnimationSpec = exponentialDecay(),
  //      positionalThreshold = { it * 0.5f },
  //      velocityThreshold = { 100f },
  //    )
  //  }
  //  val anchors1 = MyDraggableAnchors {
  //    LocalDate.now() at 0f
  //    LocalDate.now().plusDays(2) at 100f
  //  }
  //  val anchors2 = MyDraggableAnchors {
  //    LocalDate.now() at 0f
  //    LocalDate.now().plusWeeks(3) at 300f
  //  }
  //
  //  Column(
  //    modifier =
  //      Modifier.anchoredDraggable(anchoredDraggableState, orientation = Orientation.Vertical)
  //  ) {
  //    Text("Offset: ${anchoredDraggableState.offset}")
  //    Text("Current value: ${anchoredDraggableState.currentValue}")
  //    Button(
  //      onClick = {
  //        val x = 3
  //        anchoredDraggableState.updateAnchors(anchors1)
  //      }
  //    ) {
  //      Text("Anchors 1")
  //    }
  //    Button(onClick = { anchoredDraggableState.updateAnchors(anchors2) }) { Text("Anchors 2") }
  //  }

  val timelines =
    persistentListOf(
      Timeline(0, TimeUnit.DAY),
      Timeline(1, TimeUnit.WEEK),
      Timeline(2, TimeUnit.MONTH),
    )
  OrganizerNavigation(timelines = timelines) { _, timeBlock ->
    Text(
      timeBlock.displayName,
      modifier = Modifier.fillMaxSize().padding(4.dp).background(Color.LightGray),
    )
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

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(OrganizerState()) }
}
