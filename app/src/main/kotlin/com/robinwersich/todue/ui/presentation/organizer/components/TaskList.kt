package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.presentation.organizer.OrganizerEvent
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@Composable
fun TaskList(
  tasks: List<Task>,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
  focussedTaskIdState: MutableState<Long?> = remember { mutableStateOf(null) },
) {
  var focussedTaskId by focussedTaskIdState
  // If the task list contains empty tasks we want to focus those
  val emptyTask = tasks.findLast { it.text.isEmpty() }
  if (emptyTask != null) {
    focussedTaskId = emptyTask.id
  } else if (tasks.find { it.id == focussedTaskId } == null) {
    focussedTaskId = null
  }

  LookaheadScope {
    LazyColumn(
      modifier =
        modifier
          .clickable(interactionSource = null, indication = null) { focussedTaskId = null }
          .padding(horizontal = 8.dp)
    ) {
      items(tasks, key = { it.id }) { taskState ->
        // extract task ID so that onEvent stays the same, avoiding recomposition
        val taskId = taskState.id
        val focusLevel =
          when (focussedTaskId) {
            null -> FocusLevel.NEUTRAL
            taskId -> FocusLevel.FOCUSSED
            else -> FocusLevel.BACKGROUND
          }
        TaskView(
          task = taskState,
          focusLevel = focusLevel,
          onEvent = onEvent,
          modifier =
            Modifier.animateItem(fadeOutSpec = spring(stiffness = Spring.StiffnessHigh)).clickable(
              interactionSource = null,
              indication = null,
            ) {
              when (focusLevel) {
                FocusLevel.NEUTRAL -> focussedTaskId = taskId
                FocusLevel.BACKGROUND -> focussedTaskId = null
                FocusLevel.FOCUSSED -> {}
              }
            },
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
  val tasks =
    listOf("Task 1", "Task 2", "Task 3").mapIndexedToImmutableList() { id, text ->
      Task(
        id = id.toLong(),
        text = text,
        scheduledBlock = TimelineBlock(0, Day()),
        dueDate = LocalDate.now(),
      )
    }
  ToDueTheme {
    TaskList(
      tasks = tasks,
      focussedTaskIdState = mutableStateOf(1),
      modifier = Modifier.fillMaxSize(),
    )
  }
}
