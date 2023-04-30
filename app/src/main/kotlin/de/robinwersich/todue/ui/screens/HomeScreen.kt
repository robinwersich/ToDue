package de.robinwersich.todue.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
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

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)) {
  TaskList(todos = viewModel.taskList.collectAsState().value, onDoneChanged = viewModel::setDone)
}

@Composable
fun TaskList(
  todos: List<TaskUiState>,
  onDoneChanged: (id: Int, done: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(modifier = modifier) {
    items(items = todos, key = { it.id }) {
      Column {
        Task(
          text = it.text,
          dueDate = it.dueDate,
          doneDate = it.doneDate,
          onDoneChanged = { done -> onDoneChanged(it.id, done) },
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
      onDoneChanged = { _, _ -> },
    )
  }
}
