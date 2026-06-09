package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.isDone
import com.robinwersich.todue.ui.composeextensions.modifiers.signedPadding
import com.robinwersich.todue.ui.presentation.organizer.formatting.formatDuration
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.letIf
import java.time.Duration
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf

data class TaskViewState(
  val task: Task,
  val isFocussed: Boolean = false,
  val isEnabled: Boolean = true,
  val isClickable: Boolean = true,
)

sealed interface TaskViewEvent {
  data object Click : TaskViewEvent

  data object Update : TaskViewEvent

  data object Delete : TaskViewEvent

  data class SetDone(val done: Boolean) : TaskViewEvent
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskView(
  state: TaskViewState,
  onEvent: (Task, TaskViewEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  SharedTransitionScope { sharedTransitionModifier ->
    AnimatedContent(
      state.isFocussed,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      modifier =
        sharedTransitionModifier.then(modifier).letIf(state.isClickable) {
          it.clickable(interactionSource = null, indication = null) {
            onEvent(state.task, TaskViewEvent.Click)
          }
        },
    ) { isFocussed ->
      if (isFocussed) {
        ExpandedTaskView(
          task = state.task,
          onChange = { onEvent(it, TaskViewEvent.Update) },
          onDelete = { onEvent(state.task, TaskViewEvent.Delete) },
          animatedVisibilityScope = this@AnimatedContent,
        )
      } else {
        CollapsedTaskView(
          task = state.task,
          onDone = { onEvent(state.task, TaskViewEvent.SetDone(it)) },
          enabled = state.isEnabled,
          animatedVisibilityScope = this@AnimatedContent,
        )
      }
    }
  }
}

val checkboxSize = 48.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.CollapsedTaskView(
  task: Task,
  onDone: (Boolean) -> Unit,
  enabled: Boolean,
  animatedVisibilityScope: AnimatedVisibilityScope,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .sharedBounds(
          rememberSharedContentState(SharedTaskElement.CARD),
          animatedVisibilityScope,
          resizeMode = RemeasureToBounds,
        )
        .padding(end = 16.dp)
        .fillMaxWidth(),
  ) {
    TaskCheckbox(
      checked = task.doneDate != null,
      onCheckedChange = onDone,
      enabled = enabled,
      modifier =
        Modifier.size(checkboxSize)
          .sharedElement(
            rememberSharedContentState(SharedTaskElement.CHECKBOX),
            animatedVisibilityScope,
          ),
    )
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
      val textColor =
        LocalContentColor.current.copy(alpha = if (enabled && !task.isDone) 1f else 0.38f)
      val textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
      val textStyle =
        MaterialTheme.typography.bodyMedium.merge(
          color = textColor,
          textDecoration = textDecoration,
        )
      BasicTextField(
        value = task.text,
        onValueChange = {},
        enabled = false,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier =
          Modifier.sharedElement(
            rememberSharedContentState(SharedTaskElement.TEXT),
            animatedVisibilityScope,
          ),
      )
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.ExpandedTaskView(
  task: Task,
  onChange: (Task) -> Unit,
  onDelete: () -> Unit,
  animatedVisibilityScope: AnimatedVisibilityScope,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier =
      modifier.sharedBounds(
        rememberSharedContentState(SharedTaskElement.CARD),
        animatedVisibilityScope,
        resizeMode = RemeasureToBounds,
      ),
  ) {
    Column(modifier = modifier.padding(end = 16.dp).fillMaxWidth()) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        TaskCheckbox(
          checked = task.doneDate != null,
          onCheckedChange = { checked ->
            onChange(task.copy(doneDate = if (checked) LocalDate.now() else null))
          },
          modifier =
            Modifier.size(checkboxSize)
              .sharedElement(
                rememberSharedContentState(SharedTaskElement.CHECKBOX),
                animatedVisibilityScope,
              ),
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
          val focusRequester = remember { FocusRequester() }
          LaunchedEffect(task.text.isEmpty()) {
            if (task.text.isEmpty()) focusRequester.requestFocus()
          }

          BasicTextField(
            value = task.text,
            onValueChange = { it: String -> onChange(task.copy(text = it)) },
            enabled = true,
            textStyle =
              MaterialTheme.typography.bodyMedium.merge(color = LocalContentColor.current),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier =
              Modifier.sharedElement(
                  rememberSharedContentState(SharedTaskElement.TEXT),
                  animatedVisibilityScope,
                )
                .focusRequester(focusRequester),
          )
        }
      }

      TaskProperties(
        dueDate = task.dueDate,
        onDueDateChanged = { onChange(task.copy(dueDate = it)) },
        estimatedDuration = task.estimatedDuration,
        onEstimatedDurationChanged = { onChange(task.copy(estimatedDuration = it)) },
        onDelete = onDelete,
        modifier =
          Modifier.padding(start = checkboxSize).wrapContentHeight(Alignment.Top, unbounded = true),
      )
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
    modifier = modifier,
  ) {
    Icon(
      painter =
        painterResource(if (checked) R.drawable.circle_checked else R.drawable.circle_unchecked),
      contentDescription = null,
      tint = with(MaterialTheme.colorScheme) { if (checked) primary else outline },
    )
  }
}

@Composable
private fun TaskProperties(
  dueDate: LocalDate,
  onDueDateChanged: (LocalDate) -> Unit,
  estimatedDuration: Duration,
  onEstimatedDurationChanged: (Duration) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    HorizontalDivider()
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      DueDateProperty(dueDate = dueDate, onChange = onDueDateChanged)
      TimeEstimateProperty(
        duration = estimatedDuration,
        onChange = onEstimatedDurationChanged,
      )
    }
    HorizontalDivider()
    Row(
      horizontalArrangement = Arrangement.End,
      modifier = Modifier.signedPadding(end = (-12).dp),
    ) {
      TaskAction(
        R.drawable.delete,
        onClick = onDelete,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End),
      )
    }
  }
}

@Composable
private fun <T> PickerProperty(
  @DrawableRes iconId: Int,
  displayText: String,
  picker: @Composable (onChange: (T) -> Unit, onCancel: () -> Unit) -> Unit,
  onChange: (T) -> Unit,
) {
  var showSelection by rememberSaveable { mutableStateOf(false) }
  if (showSelection) {
    picker(
      {
        onChange(it)
        showSelection = false
      },
      { showSelection = false },
    )
  }
  TaskProperty(iconId, displayText, onClick = { showSelection = true })
}

@Composable
private fun DueDateProperty(dueDate: LocalDate, onChange: (LocalDate) -> Unit) {
  // TODO: use custom formatting
  PickerProperty(
    iconId = R.drawable.due_date,
    displayText = dueDate.toString(),
    picker = { onConfirm, onCancel ->
      DueDatePicker(initialSelection = dueDate, onConfirm = onConfirm, onCancel = onCancel)
    },
    onChange = onChange,
  )
}

private val DURATION_PRESETS =
  persistentListOf(
    Duration.ofMinutes(15),
    Duration.ofMinutes(30),
    Duration.ofMinutes(45),
    Duration.ofHours(1),
  )

@Composable
private fun TimeEstimateProperty(duration: Duration, onChange: (Duration) -> Unit) {
  PickerProperty(
    iconId = R.drawable.time_estimate,
    displayText = formatDuration(duration),
    picker = { onConfirm, onCancel ->
      DurationPickerDialog(
        initialDuration = duration,
        presets = DURATION_PRESETS,
        onConfirm = onConfirm,
        onCancel = onCancel,
      )
    },
    onChange = onChange,
  )
}

@Composable
private fun TaskProperty(
  @DrawableRes iconId: Int,
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // additional space for the ripple effect, does not influence layout
  val clickAreaMargin = 8.dp

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      modifier
        .height(48.dp)
        .wrapContentHeight(Alignment.CenterVertically)
        .signedPadding(-clickAreaMargin)
        .clip(RoundedCornerShape(clickAreaMargin))
        .clickable(role = Role.Button, onClick = onClick)
        .signedPadding(clickAreaMargin),
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
  modifier: Modifier = Modifier,
) {
  IconButton(onClick = onClick, modifier = modifier) {
    Icon(painterResource(id = iconId), contentDescription = null)
  }
}

private enum class SharedTaskElement {
  CARD,
  CHECKBOX,
  TEXT,
}

private val previewTask =
  Task(
    id = 1,
    text = "Buy groceries",
    scheduledBlock = TimelineBlock(0, Day()),
    dueDate = LocalDate.now(),
  )

@Preview(showBackground = true)
@Composable
private fun CollapsedTaskViewPreview() {
  ToDueTheme {
    TaskView(state = TaskViewState(task = previewTask), onEvent = { _, _ -> })
  }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedTaskViewPreview() {
  ToDueTheme {
    TaskView(
      state = TaskViewState(task = previewTask, isFocussed = true),
      onEvent = { _, _ -> },
    )
  }
}
