package com.robinwersich.todue.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.R
import com.robinwersich.todue.ui.theme.ToDueTheme
import java.time.LocalDate

@Composable
fun Task(state: TaskState, onEvent: (TaskEvent) -> Unit, modifier: Modifier = Modifier) {
  val taskId = state.id

  TaskContent(
    text = state.text,
    dueDate = state.dueDate,
    doneDate = state.doneDate,
    focusLevel = state.focusLevel,
    onDoneChanged = { onEvent(TaskEvent.SetDone(taskId, it)) },
    onTextChanged = { onEvent(TaskEvent.SetText(taskId, it)) },
    onRemove = { onEvent(TaskEvent.Remove(taskId)) },
    modifier = modifier,
  )
}

@Composable
fun TaskContent(
  text: String,
  dueDate: LocalDate,
  doneDate: LocalDate?,
  focusLevel: TaskFocusLevel,
  onDoneChanged: (Boolean) -> Unit,
  onTextChanged: (String) -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isFocussed = focusLevel == TaskFocusLevel.FOCUSSED
  val isBackground = focusLevel == TaskFocusLevel.BACKGROUND
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(true) { if (isFocussed) focusRequester.requestFocus() }

  val checkBoxWidth = 48.dp
  Surface(
    shape = RoundedCornerShape(16.dp),
    tonalElevation = if (isFocussed) 2.dp else 0.dp,
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(end = 16.dp).fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TaskCheckbox(
          checked = doneDate != null,
          onCheckedChange = onDoneChanged,
          enabled = !isBackground,
          modifier = Modifier.width(checkBoxWidth)
        )
        val textColor = LocalContentColor.current.copy(alpha = if (isBackground) 0.38f else 1f)
        val textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = textColor))

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          CachedUpdate(value = text, onValueChanged = onTextChanged) {
            val (cachedText, setCachedText) = it
            BasicTextField(
              value = cachedText,
              onValueChange = setCachedText,
              enabled = isFocussed,
              textStyle = textStyle,
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              modifier = Modifier.focusRequester(focusRequester)
            )
          }
          // put task info here if collapsed
        }
      }

      if (isFocussed) {
        TaskSettings(
          dueDate = dueDate,
          onRemove = onRemove,
          modifier = Modifier.padding(start = checkBoxWidth)
        )
      }
    }
  }
}

@Composable
fun TaskCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  IconToggleButton(
    checked = checked,
    onCheckedChange = onCheckedChange,
    enabled = enabled,
    modifier = modifier
  ) {
    Icon(
      painter =
        painterResource(if (checked) R.drawable.circle_checked else R.drawable.circle_unchecked),
      contentDescription = null
    )
  }
}

@Composable
fun TaskSettings(
  onRemove: () -> Unit,
  modifier: Modifier = Modifier,
  dueDate: LocalDate,
) {
  Column(modifier = modifier) {
    Divider()
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      TaskSetting(R.drawable.scheduled_date, "this week") // TODO: use actual data
      TaskSetting(R.drawable.time_estimate, "30min") // TODO: use actual data
    }
    Divider()
    TaskSetting(R.drawable.due_date, "12.09.") // TODO: use actual data
    Divider()
    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
      TaskAction(R.drawable.delete, onClick = onRemove)
    }
  }
}

@Composable
fun TaskSetting(@DrawableRes iconId: Int, text: String) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
    Icon(painterResource(iconId), contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium)
  }
}

@Composable
fun TaskAction(@DrawableRes iconId: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Icon(
    painterResource(iconId),
    contentDescription = null,
    modifier =
      modifier
        .padding(start = 24.dp, top = 12.dp, bottom = 12.dp)
        .clickable(
          onClick = onClick,
          role = Role.Button,
          interactionSource = remember { MutableInteractionSource() },
          indication = rememberRipple(bounded = false, radius = 20.dp)
        )
  )
}

@Preview
@Composable
fun TodoItemPreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App"), onEvent = {}) }
}

@Preview
@Composable
fun TodoItemDonePreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App", doneDate = LocalDate.now()), onEvent = {}) }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TodoItemDarkPreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App"), onEvent = {}) }
}

@Preview
@Composable
fun TodoItemBackgroundPreview() {
  ToDueTheme {
    Task(
      TaskState(
        text = "Create Todo App",
        doneDate = LocalDate.now(),
        focusLevel = TaskFocusLevel.BACKGROUND
      ),
      onEvent = {}
    )
  }
}

@Preview
@Composable
fun TodoItemMultiLinePreview() {
  ToDueTheme {
    Task(
      TaskState(text = "This is a relatively long task spanning exactly two lines"),
      onEvent = {}
    )
  }
}

@Preview
@Composable
fun TodoItemExpandedPreview() {
  ToDueTheme {
    Task(TaskState(text = "Create Todo App", focusLevel = TaskFocusLevel.FOCUSSED), onEvent = {})
  }
}
