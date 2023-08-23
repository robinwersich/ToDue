package com.robinwersich.todue.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.R
import com.robinwersich.todue.ui.components.TaskFocusLevel.BACKGROUND
import com.robinwersich.todue.ui.components.TaskFocusLevel.FOCUSSED
import com.robinwersich.todue.ui.screens.main.ModifyTaskEvent
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.ui.utility.DebouncedUpdate
import com.robinwersich.todue.ui.utility.signedPadding
import java.time.LocalDate

@Composable
fun Task(state: TaskState, modifier: Modifier = Modifier, onEvent: (ModifyTaskEvent) -> Unit = {}) {
  Task(state.text, state.dueDate, state.doneDate, state.focusLevel, onEvent, modifier)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Task(
  text: String,
  dueDate: LocalDate,
  doneDate: LocalDate?,
  focusLevel: TaskFocusLevel,
  onEvent: (ModifyTaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(true) { if (focusLevel == FOCUSSED) focusRequester.requestFocus() }

  val focusTransition = updateTransition(focusLevel, label = "Focus Level")

  val tonalElevation by
    focusTransition.animateDp(label = "Tonal Elevation") { if (it == FOCUSSED) 2.dp else 0.dp }
  Surface(shape = RoundedCornerShape(16.dp), tonalElevation = tonalElevation, modifier = modifier) {
    val checkBoxWidth = 48.dp
    Column(modifier = Modifier.padding(end = 16.dp).fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TaskCheckbox(
          checked = doneDate != null,
          onCheckedChange = { onEvent(ModifyTaskEvent.SetDone(it)) },
          enabled = focusLevel != BACKGROUND,
          modifier = Modifier.width(checkBoxWidth)
        )

        val textColor =
          LocalContentColor.current.copy(alpha = if (focusLevel == BACKGROUND) 0.38f else 1f)
        val textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = textColor))
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          DebouncedUpdate(
            value = text,
            onValueChanged = { onEvent(ModifyTaskEvent.SetText(it)) },
            emitUpdates = focusLevel == FOCUSSED
          ) {
            val (cachedText, setCachedText) = it
            BasicTextField(
              value = cachedText,
              onValueChange = setCachedText,
              enabled = focusLevel == FOCUSSED,
              textStyle = textStyle,
              cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
              modifier = Modifier.focusRequester(focusRequester)
            )
          }
          // put task info here if collapsed
        }
      }

      val animationAnchor = Alignment.Top
      focusTransition.AnimatedVisibility(
        visible = { it == FOCUSSED },
        enter = fadeIn() + expandVertically(expandFrom = animationAnchor),
        exit = fadeOut() + shrinkVertically(shrinkTowards = animationAnchor)
      ) {
        TaskProperties(
          dueDate = dueDate,
          onEvent = onEvent,
          modifier =
            Modifier.padding(start = checkBoxWidth)
              .wrapContentHeight(animationAnchor, unbounded = true)
        )
      }
    }
  }
}

@Composable
private fun TaskCheckbox(
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
private fun TaskProperties(
  dueDate: LocalDate,
  onEvent: (ModifyTaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Divider()
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      TaskProperty(R.drawable.scheduled_date, "this week", onClick = {}) // TODO: use actual data
      TaskProperty(R.drawable.time_estimate, "30min", onClick = {}) // TODO: use actual data
    }
    Divider()
    DueDateProperty(dueDate = dueDate, onEvent = onEvent)
    Divider()
    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier.signedPadding(end = (-12).dp)
    ) {
      TaskAction(
        R.drawable.delete,
        onClick = { onEvent(ModifyTaskEvent.Delete) },
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
      )
    }
  }
}

@Composable
private fun DueDateProperty(dueDate: LocalDate, onEvent: (ModifyTaskEvent) -> Unit) {
  // TODO: use custom formatting
  var showDueDateSelection by rememberSaveable { mutableStateOf(false) }
  if (showDueDateSelection) {
    DueDatePicker(
      initialSelection = dueDate,
      onConfirm = {
        onEvent(ModifyTaskEvent.SetDueDate(it))
        showDueDateSelection = false
      },
      onCancel = { showDueDateSelection = false },
    )
  }
  TaskProperty(R.drawable.due_date, dueDate.toString(), onClick = { showDueDateSelection = true })
}

@Composable
private fun TaskProperty(
  @DrawableRes iconId: Int,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  // additional space for the ripple effect, does not influence layout
  val clickAreaMargin = 8.dp

  Row(
    modifier =
      modifier
        .height(48.dp)
        .wrapContentHeight(Alignment.CenterVertically)
        .signedPadding(-clickAreaMargin)
        .clip(RoundedCornerShape(clickAreaMargin))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = rememberRipple(),
          role = Role.Button,
          onClick = onClick,
        )
        .signedPadding(clickAreaMargin)
  ) {
    Icon(painterResource(iconId), contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text(text, style = MaterialTheme.typography.bodyMedium)
  }
}

@Composable
private fun TaskAction(
  @DrawableRes iconId: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  IconButton(onClick = onClick, modifier = modifier) {
    Icon(painterResource(id = iconId), contentDescription = null)
  }
}

@Preview
@Composable
private fun TodoItemDonePreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App", doneDate = LocalDate.now())) }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TodoItemDarkPreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App")) }
}

@Preview
@Composable
private fun TodoItemBackgroundPreview() {
  ToDueTheme {
    Task(TaskState(text = "Create Todo App", doneDate = LocalDate.now(), focusLevel = BACKGROUND))
  }
}

@Preview
@Composable
private fun TodoItemMultiLinePreview() {
  ToDueTheme { Task(TaskState(text = "This is a relatively long task spanning exactly two lines")) }
}

@Preview
@Composable
private fun TodoItemExpandedPreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App", focusLevel = FOCUSSED)) }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TodoItemExpandedDarkPreview() {
  ToDueTheme { Task(TaskState(text = "Create Todo App", focusLevel = FOCUSSED)) }
}
