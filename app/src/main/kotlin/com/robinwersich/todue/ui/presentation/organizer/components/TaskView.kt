package com.robinwersich.todue.ui.presentation.organizer.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.TimeUnitInstance
import com.robinwersich.todue.ui.presentation.organizer.FocusLevel
import com.robinwersich.todue.ui.presentation.organizer.ModifyTaskEvent
import com.robinwersich.todue.ui.presentation.organizer.TaskViewState
import com.robinwersich.todue.ui.presentation.utility.displayName
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.ui.utility.DebouncedUpdate
import com.robinwersich.todue.ui.utility.signedPadding
import java.time.LocalDate

@Composable
fun TaskView(
  state: TaskViewState,
  modifier: Modifier = Modifier,
  onEvent: (ModifyTaskEvent) -> Unit = {}
) {
  TaskView(
    text = state.text,
    timeBlock = state.timeBlock,
    dueDate = state.dueDate,
    doneDate = state.doneDate,
    focusLevel = state.focusLevel,
    onEvent = onEvent,
    modifier = modifier,
  )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TaskView(
  text: String,
  timeBlock: TimeUnitInstance?,
  dueDate: LocalDate,
  doneDate: LocalDate?,
  focusLevel: FocusLevel,
  onEvent: (ModifyTaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  // auto focus on create
  LaunchedEffect(true) { if (focusLevel == FocusLevel.FOCUSSED) focusRequester.requestFocus() }

  val focusManager = LocalFocusManager.current
  // clear focus on collapse
  if (focusLevel == FocusLevel.FOCUSSED)
    DisposableEffect(true) { onDispose { focusManager.clearFocus() } }

  val focusTransition = updateTransition(focusLevel, label = "Focus Level")

  val surfaceColor by
    focusTransition.animateColor(label = "Task Color") {
      if (it == FocusLevel.FOCUSSED) MaterialTheme.colorScheme.surfaceContainer
      else Color.Transparent
    }
  Surface(shape = RoundedCornerShape(24.dp), color = surfaceColor, modifier = modifier) {
    val checkBoxWidth = 48.dp
    Column(modifier = Modifier.padding(end = 16.dp).fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TaskCheckbox(
          checked = doneDate != null,
          onCheckedChange = { onEvent(ModifyTaskEvent.SetDone(it)) },
          enabled = focusLevel != FocusLevel.BACKGROUND,
          modifier = Modifier.width(checkBoxWidth)
        )

        val textColor =
          LocalContentColor.current.copy(
            alpha = if (focusLevel == FocusLevel.BACKGROUND) 0.38f else 1f
          )
        val textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = textColor))
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          DebouncedUpdate(
            value = text,
            onValueChanged = { onEvent(ModifyTaskEvent.SetText(it)) },
            emitUpdates = focusLevel == FocusLevel.FOCUSSED
          ) {
            val (cachedText, setCachedText) = it
            BasicTextField(
              value = cachedText,
              onValueChange = setCachedText,
              enabled = focusLevel == FocusLevel.FOCUSSED,
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
        visible = { it == FocusLevel.FOCUSSED },
        enter = fadeIn() + expandVertically(expandFrom = animationAnchor),
        exit = fadeOut() + shrinkVertically(shrinkTowards = animationAnchor)
      ) {
        TaskProperties(
          timeBlock = timeBlock,
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
  timeBlock: TimeUnitInstance?,
  dueDate: LocalDate,
  onEvent: (ModifyTaskEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    HorizontalDivider()
    ScheduledTimeBlockProperty(timeBlock = timeBlock, onEvent = onEvent)
    /*Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      TaskProperty(R.drawable.scheduled_date, "this week", onClick = {})
      TaskProperty(R.drawable.time_estimate, "30min", onClick = {})
    }*/
    HorizontalDivider()
    DueDateProperty(dueDate = dueDate, onEvent = onEvent)
    HorizontalDivider()
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
private fun ScheduledTimeBlockProperty(
  timeBlock: TimeUnitInstance?,
  onEvent: (ModifyTaskEvent) -> Unit
) {
  var showSelection by rememberSaveable { mutableStateOf(false) }
  if (showSelection) {
    DueDatePicker(
      initialSelection = timeBlock?.endDate ?: LocalDate.now(),
      onConfirm = {
        // TODO: put meaningful timeline ID here
        onEvent(ModifyTaskEvent.SetTimeBlock(TimeUnitInstance.Day(it)))
        showSelection = false
      },
      onCancel = { showSelection = false },
    )
  }
  val timeBlockName = timeBlock?.displayName ?: "unscheduled"
  TaskProperty(R.drawable.scheduled_date, timeBlockName, onClick = { showSelection = true })
}

@Composable
private fun DueDateProperty(dueDate: LocalDate, onEvent: (ModifyTaskEvent) -> Unit) {
  var showSelection by rememberSaveable { mutableStateOf(false) }
  if (showSelection) {
    DueDatePicker(
      initialSelection = dueDate,
      onConfirm = {
        onEvent(ModifyTaskEvent.SetDueDate(it))
        showSelection = false
      },
      onCancel = { showSelection = false },
    )
  }
  // TODO: use custom formatting
  TaskProperty(R.drawable.due_date, dueDate.toString(), onClick = { showSelection = true })
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
        .clickable(role = Role.Button, onClick = onClick)
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

class TaskPreviewProvider : PreviewParameterProvider<TaskViewState> {
  override val values: Sequence<TaskViewState> = sequence {
    for (focusLevel in FocusLevel.entries) {
      for (doneDate in listOf(null, LocalDate.now())) {
        yield(TaskViewState(text = "Create Todo App", doneDate = doneDate, focusLevel = focusLevel))
      }
      yield(
        TaskViewState(
          text = "This is a relatively long task spanning over two lines.",
          focusLevel = focusLevel
        )
      )
    }
  }
}

@Preview()
@Composable
private fun TaskPreview(@PreviewParameter(TaskPreviewProvider::class) state: TaskViewState) {
  ToDueTheme {
    TaskView(state, modifier = Modifier.background(MaterialTheme.colorScheme.background))
  }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TaskPreviewDark(@PreviewParameter(TaskPreviewProvider::class) state: TaskViewState) {
  ToDueTheme {
    TaskView(state, modifier = Modifier.background(MaterialTheme.colorScheme.background))
  }
}
