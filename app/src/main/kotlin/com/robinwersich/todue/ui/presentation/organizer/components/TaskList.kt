package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.composeextensions.mutablePeekableStateOf
import com.robinwersich.todue.ui.presentation.organizer.OrganizerEvent
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.letIf
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@Composable
fun TaskList(
  tasks: List<Task>,
  modifier: Modifier = Modifier,
  mode: TaskBlockContentMode = TaskBlockContentMode.FULLSCREEN,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  val focusManager = LocalFocusManager.current
  val state = remember { TaskListState(onEvent, focusManager) }

  if (mode.isFullscreen) {
    state.update(tasks)
    DisposableEffect(Unit) { onDispose { state.saveOrDeleteFocussedTask() } }
  } else {
    state.removeFocus()
  }

  LookaheadScope {
    LazyColumn(
      modifier =
        modifier
          .letIf(mode.isFullscreen) {
            it.clickable(interactionSource = null, indication = null) {
              state.removeFocus()
            }
          }
          .padding(horizontal = 8.dp)
    ) {
      items(tasks, key = { it.id }) { task ->
        TaskView(
          state = state.getViewState(task, isFullscreen = mode.isFullscreen),
          onEvent = state::onTaskEvent,
          modifier = Modifier.animateItem(fadeOutSpec = spring(stiffness = Spring.StiffnessHigh)),
        )
      }
    }
  }
}

/**
 * Stores which task is currently focussed and has a local task state used for editing the focussed
 * task until it is saved to the DB.
 */
class TaskListState(
  private val onEvent: (OrganizerEvent) -> Unit,
  private val focusManager: FocusManager,
) {
  private val focussedTaskId = mutablePeekableStateOf<Long?>(null)
  private val localTaskStateId = mutablePeekableStateOf<Long?>(null)
  private var localTaskState by mutableStateOf<Task?>(null)
  private var isFocussedTaskEmpty: Boolean = false

  private val isLocalTaskStateFocussed
    get() = focussedTaskId.peek() != null && focussedTaskId.peek() == localTaskStateId.peek()

  private val isAnyTaskFocussed
    get() = focussedTaskId.value != null

  fun getViewState(task: Task, isFullscreen: Boolean): TaskViewState {
    return TaskViewState(
      task = getMostRecentState(task),
      isFocussed = task.id == focussedTaskId.value,
      isEnabled = !isAnyTaskFocussed,
      isClickable = isFullscreen,
    )
  }

  fun onTaskEvent(task: Task, event: TaskViewEvent) {
    when (event) {
      is TaskViewEvent.Click -> {
        when {
          task.id == focussedTaskId.peek() -> {}
          isAnyTaskFocussed -> removeFocus()
          else -> focus(task)
        }
      }
      is TaskViewEvent.Update -> updateFocussedTaskState(task)
      is TaskViewEvent.Delete -> deleteTask(task.id)
      is TaskViewEvent.SetDone -> onEvent(OrganizerEvent.SetTaskDone(task.id, event.done))
    }
  }

  fun removeFocus(save: Boolean = true) {
    if (save) saveOrDeleteFocussedTask()
    focussedTaskId.value = null
    focusManager.clearFocus()
  }

  fun saveOrDeleteFocussedTask() {
    focussedTaskId.peek()?.let { focussedId ->
      if (isFocussedTaskEmpty) {
        deleteTask(focussedId)
      } else {
        localTaskState?.let { if (it.id == focussedId) onEvent(OrganizerEvent.UpdateTask(it)) }
      }
    }
  }

  private fun deleteTask(taskId: Long) {
    onEvent(OrganizerEvent.DeleteTask(taskId))
    if (taskId == focussedTaskId.peek()) {
      removeFocus(save = false)
    }
  }

  /**
   * Adjust the state for an updated task list. This means invalidating the [localTaskState] state
   * if it's not focussed anymore and removing focus if the currently [focussed][focussedTaskId]
   * task is not part of the new [tasks] anymore. If the new [tasks] list contains an empty task it
   * will be focussed.
   */
  fun update(tasks: List<Task>) {
    if (!isLocalTaskStateFocussed) invalidateLocalTaskState()
    val emptyTask = tasks.findLast { it.text.isEmpty() }
    if (emptyTask != null) {
      focus(emptyTask)
    }
  }

  private fun focus(task: Task) {
    if (focussedTaskId.peek() == task.id) return
    saveOrDeleteFocussedTask()
    focussedTaskId.value = task.id
    isFocussedTaskEmpty = task.text.isEmpty()
  }

  private fun getMostRecentState(task: Task) =
    if (task.id == localTaskStateId.value) localTaskState ?: task else task

  private fun updateFocussedTaskState(task: Task) {
    localTaskState = task
    focussedTaskId.value = task.id
    localTaskStateId.value = task.id
    isFocussedTaskEmpty = task.text.isEmpty()
  }

  private fun invalidateLocalTaskState() {
    localTaskStateId.value = null
    localTaskState = null
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
  ToDueTheme { TaskList(tasks = tasks, modifier = Modifier.fillMaxSize()) }
}
