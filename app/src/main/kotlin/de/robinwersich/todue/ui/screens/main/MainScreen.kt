package de.robinwersich.todue.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import de.robinwersich.todue.ui.components.Task
import de.robinwersich.todue.ui.components.TaskEvent
import de.robinwersich.todue.ui.components.TaskState
import de.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(state: MainScreenState, onEvent: (TaskEvent) -> Unit) {
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { onEvent(TaskEvent.Add) }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    TaskList(
      tasks = state.tasks,
      onEvent = { onEvent(it) }, // TODO: use method reference once this doesn't cause recomposition
      modifier = Modifier.padding(paddingValues),
    )
  }
}

@Composable
fun TaskList(
  tasks: List<TaskState>,
  onEvent: (TaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    items(items = tasks, key = { it.id }) {
      // TODO: don't remember modifier once upgraded to compose 1.5
      val clickableModifier =
        remember(onEvent, it.id) { Modifier.clickable { onEvent(TaskEvent.Expand(it.id)) } }
      Column {
        Task(
          state = it,
          onEvent = onEvent,
          modifier = clickableModifier,
        )
        Divider(thickness = Dp.Hairline)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
  ToDueTheme {
    TaskList(
      tasks = List(50) { TaskState(id = it, text = "Task $it", dueDate = LocalDate.now()) },
      onEvent = {}
    )
  }
}
