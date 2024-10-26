package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.ui.presentation.organizer.CollapseTasks
import com.robinwersich.todue.ui.presentation.organizer.ExpandTask
import com.robinwersich.todue.ui.presentation.organizer.FocusLevel
import com.robinwersich.todue.ui.presentation.organizer.ModifyTask
import com.robinwersich.todue.ui.presentation.organizer.OrganizerEvent
import com.robinwersich.todue.ui.presentation.organizer.TaskViewState
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TaskList(
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
  LazyColumn(modifier = taskListModifier.padding(horizontal = 8.dp)) {
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

@Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
  val tasks =
    listOf("Task 1", "Task 2", "Task 3").mapIndexedToImmutableList() { id, text ->
      TaskViewState(
        id = id.toLong(),
        text = text,
        focusLevel = if (id == 1) FocusLevel.FOCUSSED else FocusLevel.BACKGROUND,
      )
    }
  ToDueTheme { TaskList(tasks = tasks, modifier = Modifier.fillMaxSize()) }
}
