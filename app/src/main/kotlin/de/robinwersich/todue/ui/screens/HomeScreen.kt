package de.robinwersich.todue.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.robinwersich.todue.ui.components.Task
import de.robinwersich.todue.ui.components.TaskUiState
import de.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)) {
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = viewModel::addTask) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    TaskList(
      todos = viewModel.taskList.collectAsState().value,
      onTextChanged = viewModel::setText,
      onDoneChanged = viewModel::setDone,
      onDelete = viewModel::deleteTask,
      onExpandToggled = viewModel::toggleExpansion,
      modifier = Modifier.padding(paddingValues),
    )
  }
}

@Composable
fun TaskList(
  todos: List<TaskUiState>,
  onTextChanged: (id: Int, text: String) -> Unit,
  onDoneChanged: (id: Int, done: Boolean) -> Unit,
  onDelete: (id: Int) -> Unit,
  onExpandToggled: (id: Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    items(items = todos, key = { it.id }) {
      val taskId = it.id
      Column {
        Task(
          state = it,
          onTextChanged = { text -> onTextChanged(taskId, text) },
          onDoneChanged = { done -> onDoneChanged(taskId, done) },
          onDelete = { onDelete(taskId) },
          modifier = Modifier.clickable(onClick = { onExpandToggled(taskId) })
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
      todos = List(50) { TaskUiState(id = it, text = "Task $it", dueDate = LocalDate.now()) },
      onTextChanged = { _, _ -> },
      onDoneChanged = { _, _ -> },
      onDelete = {},
      onExpandToggled = {},
    )
  }
}
