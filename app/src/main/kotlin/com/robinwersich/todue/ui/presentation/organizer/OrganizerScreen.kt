package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.ui.presentation.organizer.components.TaskView
import com.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun OrganizerScreen(state: OrganizerState, onEvent: (OrganizerEvent) -> Unit = {}) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    floatingActionButton = {
      FloatingActionButton(onClick = { onEvent(AddTask) }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    TaskList(
      tasks = state.tasks,
      onEvent = { onEvent(it) }, // TODO: use method reference once this doesn't cause recomposition
      modifier = Modifier.padding(paddingValues).fillMaxHeight(),
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
              FokusLevel.FOCUSSED ->
                Modifier.clickable(interactionSource = interactionSource, indication = null) {}
              FokusLevel.NEUTRAL ->
                Modifier.clickable(interactionSource = interactionSource, indication = null) {
                  onEvent(ExpandTask(taskState.id))
                }
              FokusLevel.BACKGROUND -> Modifier
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
        null -> FokusLevel.NEUTRAL
        it -> FokusLevel.FOCUSSED
        else -> FokusLevel.BACKGROUND
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
    OrganizerScreen(OrganizerState(tasks = taskStateList(size = 5, focussedTask = 3).toImmutableList()))
  }
}
