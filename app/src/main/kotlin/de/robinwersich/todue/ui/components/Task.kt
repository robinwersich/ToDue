package de.robinwersich.todue.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.robinwersich.todue.R
import java.time.LocalDate

@Composable
fun Task(
  state: TaskUiState,
  onTextChanged: (String) -> Unit,
  onDoneChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) = Task(state.text, state.dueDate, state.doneDate, onTextChanged, onDoneChanged, modifier)

@Composable
fun Task(
  text: String,
  dueDate: LocalDate,
  doneDate: LocalDate?,
  onTextChanged: (String) -> Unit,
  onDoneChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val done = doneDate != null
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)
  ) {
    TaskCheckbox(checked = done, onCheckedChange = onDoneChanged)
    Column {
      CachedUpdate(text, onTextChanged) {
        val (cachedText, setCachedText) = it
        BasicTextField(
          value = cachedText,
          onValueChange = setCachedText,
          textStyle =
            MaterialTheme.typography.titleLarge.copy(
              textDecoration = if (done) TextDecoration.LineThrough else null
            ),
        )
      }
      TaskInfo(dueDate = dueDate, modifier = Modifier.padding(top = 8.dp))
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
      contentDescription = null,
    )
  }
}

@Composable
fun TaskInfo(
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

@Preview(showBackground = true)
@Composable
fun TodoItemPreview() {
  Task(
    text = "Create Todo App",
    dueDate = LocalDate.now(),
    doneDate = null,
    onTextChanged = {},
    onDoneChanged = {}
  )
}

@Preview(showBackground = true)
@Composable
fun TodoItemDonePreview() {
  Task(
    text = "Create Todo App",
    dueDate = LocalDate.now(),
    doneDate = LocalDate.now(),
    onTextChanged = {},
    onDoneChanged = {},
  )
}

@Preview(showBackground = true)
@Composable
fun TodoItemMultiLinePreview() {
  Task(
    text = "This is a very long task that spans two lines",
    dueDate = LocalDate.now(),
    doneDate = null,
    onTextChanged = {},
    onDoneChanged = {},
  )
}
