package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.composeextensions.modifiers.signedPadding
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter

val checkBoxWidth = 48.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CollapsedTaskView(
  task: Task,
  onDone: (Boolean) -> Unit,
  enabled: Boolean,
  animatedVisibilityScope: AnimatedVisibilityScope,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier.sharedBounds(
        rememberSharedContentState(SharedTaskElement.CARD),
        animatedVisibilityScope,
        resizeMode = RemeasureToBounds,
      )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(end = 16.dp).fillMaxWidth(),
    ) {
      TaskCheckbox(
        checked = task.doneDate != null,
        onCheckedChange = onDone,
        modifier =
          Modifier.width(checkBoxWidth)
            .sharedElement(
              rememberSharedContentState(SharedTaskElement.CHECKBOX),
              animatedVisibilityScope,
            ),
      )
      Column(modifier = Modifier.padding(vertical = 8.dp)) {
        val textColor = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f)
        val textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = textColor))
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ExpandedTaskView(
  task: Task,
  onChange: (Task) -> Unit,
  onDelete: () -> Unit,
  animatedVisibilityScope: AnimatedVisibilityScope,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
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
            Modifier.width(checkBoxWidth)
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
            textStyle = MaterialTheme.typography.bodyLarge,
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
        scheduledBlock = task.scheduledBlock,
        dueDate = task.dueDate,
        onBlockChanged = { onChange(task.copy(scheduledBlock = it)) },
        onDueDateChanged = { onChange(task.copy(dueDate = it)) },
        onDelete = onDelete,
        modifier =
          Modifier.padding(start = checkBoxWidth).wrapContentHeight(Alignment.Top, unbounded = true),
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
    )
  }
}

@Composable
private fun TaskProperties(
  scheduledBlock: TimelineBlock,
  dueDate: LocalDate,
  onBlockChanged: (TimelineBlock) -> Unit,
  onDueDateChanged: (LocalDate) -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    HorizontalDivider()
    ScheduledTimelineBlockProperty(block = scheduledBlock, onChange = onBlockChanged)
    /*Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
      TaskProperty(R.drawable.scheduled_date, "this week", onClick = {})
      TaskProperty(R.drawable.time_estimate, "30min", onClick = {})
    }*/
    HorizontalDivider()
    DueDateProperty(dueDate = dueDate, onDueDateChanged)
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
private fun ScheduledTimelineBlockProperty(
  block: TimelineBlock,
  onChange: (TimelineBlock) -> Unit,
) {
  val timeBlockFormatter = rememberTimeBlockFormatter()
  var showSelection by rememberSaveable { mutableStateOf(false) }
  if (showSelection) {
    DueDatePicker(
      initialSelection = block.section.endInclusive,
      onConfirm = {
        onChange(TimelineBlock(block.timelineId, Day(it)))
        showSelection = false
      },
      onCancel = { showSelection = false },
    )
  }
  val timeBlockName = timeBlockFormatter.format(block.section)
  TaskProperty(R.drawable.scheduled_date, timeBlockName, onClick = { showSelection = true })
}

@Composable
private fun DueDateProperty(dueDate: LocalDate, onChange: (LocalDate) -> Unit) {
  var showSelection by rememberSaveable { mutableStateOf(false) }
  if (showSelection) {
    DueDatePicker(
      initialSelection = dueDate,
      onConfirm = {
        onChange(it)
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
  modifier: Modifier = Modifier,
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
