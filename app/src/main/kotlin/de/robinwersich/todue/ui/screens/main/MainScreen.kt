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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.robinwersich.todue.ui.components.Task
import de.robinwersich.todue.ui.components.TaskEvent
import de.robinwersich.todue.ui.components.TaskState
import de.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainScreenViewModel = viewModel(factory = MainScreenViewModel.Factory)) {
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { viewModel.handleEvent(TaskEvent.Add) }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    val viewState by viewModel.viewState.collectAsState()
    TaskList(
      tasks = viewState.tasks,
      onEvent = viewModel::handleEvent,
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
      val taskId = it.id
      Column {
        Task(
          state = it,
          onEvent = onEvent,
          modifier = Modifier.clickable(onClick = { onEvent(TaskEvent.Expand(taskId)) })
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
