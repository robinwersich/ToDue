package de.robinwersich.todue.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.robinwersich.todue.data.entities.Task
import de.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate


@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = viewModel(factory = HomeScreenViewModel.Factory)) {
    TaskList(todos = viewModel.taskList.collectAsState().value)
}

@Composable
fun TaskListItem(text: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Checkbox(checked = false, onCheckedChange = null, modifier = Modifier.padding(8.dp))
        Text(text = text, fontSize = 24.sp)
    }
}

@Composable
fun TaskList(todos: List<Task>) {
    LazyColumn {
        itemsIndexed(todos) { index, item ->
            TaskListItem(item.text)
            if (index <= todos.lastIndex) Divider(thickness = Dp.Hairline)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
    TaskListItem("Create Todo App")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ToDueTheme {
        TaskList(List(50) { Task(text = "Task $it", dueDate = LocalDate.now()) })
    }
}