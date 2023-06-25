package de.robinwersich.todue.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.robinwersich.todue.R
import java.time.LocalDate

@Composable
fun Task(state: TaskState, onEvent: (TaskEvent) -> Unit, modifier: Modifier = Modifier) {
  val taskId = state.id
  Task(
    text = state.text,
    dueDate = state.dueDate,
    doneDate = state.doneDate,
    expanded = state.expanded,
    onDoneChanged = { onEvent(TaskEvent.SetDone(taskId, it)) },
    onTextChanged = { onEvent(TaskEvent.SetText(taskId, it)) },
    onRemove = { onEvent(TaskEvent.Remove(taskId)) },
    modifier = modifier,
  )
}

@Composable
fun Task(
  text: String,
  dueDate: LocalDate,
  doneDate: LocalDate?,
  expanded: Boolean,
  onDoneChanged: (Boolean) -> Unit,
  onTextChanged: (String) -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
  ) {
    TaskCheckbox(checked = doneDate != null, onCheckedChange = onDoneChanged)
    Column {
      CachedUpdate(value = text, onValueChanged = onTextChanged) {
        val (cachedText, setCachedText) = it
        BasicTextField(
          value = cachedText,
          onValueChange = setCachedText,
          textStyle =
            MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
          cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant)
        )
      }
      if (expanded) {
        ExpandedTaskInfo(
          dueDate = dueDate,
          onRemove = onRemove,
          modifier = Modifier.padding(top = 8.dp)
        )
      } else {
        CollapsedTaskInfo(dueDate = dueDate, modifier = Modifier.padding(top = 8.dp))
      }
    }
  }
}

@Composable
fun TaskCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  IconToggleButton(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier) {
    Icon(
      painter =
        painterResource(if (checked) R.drawable.circle_checked else R.drawable.circle_unchecked),
      contentDescription = null
    )
  }
}

@Composable
fun CollapsedTaskInfo(
  modifier: Modifier = Modifier,
  dueDate: LocalDate? = null,
) {
  Row(modifier = modifier) {
    dueDate?.let {
      Icon(
        painter = painterResource(R.drawable.calendar),
        contentDescription = null,
        modifier = Modifier.padding(end = 4.dp).size(16.dp)
      )
      Text(text = it.toString(), style = MaterialTheme.typography.labelSmall)
    }
  }
}

@Composable
fun ExpandedTaskInfo(
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
  dueDate: LocalDate? = null,
) {
  Column(modifier = modifier) {
    Spacer(modifier = Modifier.fillMaxWidth().height(40.dp))
    Row(
      verticalAlignment = Alignment.Bottom,
      horizontalArrangement = Arrangement.End,
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(onClick = onRemove) {
        Icon(painter = painterResource(R.drawable.delete), contentDescription = null)
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
  Task(TaskState(text = "Create Todo App"), onEvent = {})
}

@Preview(showBackground = true)
@Composable
fun TodoItemDonePreview() {
  Task(TaskState(text = "Create Todo App", doneDate = LocalDate.now()), onEvent = {})
}

@Preview(showBackground = true)
@Composable
fun TodoItemMultiLinePreview() {
  Task(TaskState(text = "This is a very long task that spans two lines"), onEvent = {})
}

@Preview(showBackground = true)
@Composable
fun TodoItemExpandedPreview() {
  Task(
    TaskState(text = "This is a very long task that spans two lines", expanded = true),
    onEvent = {}
  )
}
