package de.robinwersich.todue.ui.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.robinwersich.todue.ui.Task
import de.robinwersich.todue.ui.TaskUiData
import de.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)) {
  TaskList(todos = viewModel.taskList.collectAsState().value, onDoneChanged = viewModel::setDone)
}

@Composable
fun TaskList(todos: List<TaskUiData>, onDoneChanged: (id: Int, done: Boolean) -> Unit) {
  LazyColumn {
    items(items = todos, key = { it.id }) {
      Task(
        text = it.text,
        dueDate = it.dueDate,
        done = it.done,
        onDoneChanged = { done -> onDoneChanged(it.id, done) }
      )
      Divider(thickness = Dp.Hairline)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {
  ToDueTheme {
    TaskList(
      todos = List(50) { TaskUiData(id = it, text = "Task $it", dueDate = LocalDate.now()) },
      onDoneChanged = {_, _ ->}
    )
  }
}
