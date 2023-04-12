package com.example.todue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todue.ui.theme.ToDueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDueTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    TodoList(todos)
                }
            }
        }
    }
}

data class TodoData(val text: String)

private val todos = List(50) { TodoData("Task $it") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(data: TodoData, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .size(48.dp)
            .padding(8.dp)
    ) {
        Checkbox(checked = false, onCheckedChange = null, modifier = Modifier.padding(8.dp))
        Text(text = data.text, fontSize = 24.sp)
    }
}

@Composable
fun TodoList(todos: List<TodoData>) {
    LazyColumn {
        itemsIndexed(todos) { index, item ->
            TodoItem(item)
            if (index <= todos.lastIndex) Divider(thickness = Dp.Hairline)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
    TodoItem(todos.first())
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ToDueTheme {
        TodoList(todos)
    }
}