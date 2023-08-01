package com.robinwersich.todue.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.ui.components.Task
import com.robinwersich.todue.ui.components.TaskEvent
import com.robinwersich.todue.ui.components.TaskFocusLevel
import com.robinwersich.todue.ui.components.TaskState
import com.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(state: MainScreenState, onEvent: (TaskEvent) -> Unit) {
  Scaffold(
    containerColor = MaterialTheme.colorScheme.surface,
    floatingActionButton = {
      FloatingActionButton(onClick = { onEvent(TaskEvent.Add) }) {
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
fun TaskList(
  tasks: List<TaskState>,
  onEvent: (TaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val focusManager = LocalFocusManager.current
  val taskListModifier =
    remember(modifier, onEvent) {
      modifier.clickable(interactionSource = interactionSource, indication = null) {
        focusManager.clearFocus()
        onEvent(TaskEvent.Collapse)
      }
    }
  LazyColumn(modifier = taskListModifier.padding(8.dp)) {
    items(items = tasks, key = { it.id }) {
      // TODO: don't remember modifier once upgraded to compose 1.5
      val taskModifier =
        remember(it.id, it.focusLevel, onEvent, interactionSource) {
          when (it.focusLevel) {
            TaskFocusLevel.FOCUSSED ->
              Modifier.clickable(interactionSource = interactionSource, indication = null) {}
            TaskFocusLevel.NEUTRAL -> Modifier.clickable { onEvent(TaskEvent.Expand(it.id)) }
            TaskFocusLevel.BACKGROUND -> Modifier
          }
        }
      Task(
        state = it,
        onEvent = onEvent,
        modifier = taskModifier,
      )
    }
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  ToDueTheme {
    MainScreen(
      MainScreenState(
        tasks =
          List(4) { TaskState(id = it.toLong(), text = "Task $it", dueDate = LocalDate.now()) }
      ),
      onEvent = {}
    )
  }
}
