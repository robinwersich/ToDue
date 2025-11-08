package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TaskList(
  tasks: List<Task>,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
  state: TaskListState = remember { TaskListState(onEvent) },
) {
  state.update(tasks)
  DisposableEffect(Unit) { onDispose { state.saveOrDeleteFocussedTask() } }
  val focusManager = LocalFocusManager.current

  LookaheadScope {
    LazyColumn(
      modifier =
        modifier
          .clickable(interactionSource = null, indication = null) {
            state.removeFocus()
            focusManager.clearFocus()
          }
          .padding(horizontal = 8.dp)
    ) {
      items(tasks, key = { it.id }) { task ->
        SharedTransitionScope { sharedTransitionModifier ->
          AnimatedContent(
            state.isFocussed(task),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier =
              sharedTransitionModifier
                .animateItem(fadeOutSpec = spring(stiffness = Spring.StiffnessHigh))
                .clickable(interactionSource = null, indication = null) {
                  when {
                    state.isFocussed(task) -> {}
                    state.isAnyTaskFocussed -> {
                      state.removeFocus()
                      focusManager.clearFocus()
                    }
                    else -> state.focus(task)
                  }
                },
          ) { isFocussed ->
            if (isFocussed) {
              ExpandedTaskView(
                task = state.getMostRecentState(task),
                onChange = { state.updateFocussedTaskState(it) },
                onDelete = { state.deleteTask(task.id) },
                animatedVisibilityScope = this@AnimatedContent,
              )
            } else {
              CollapsedTaskView(
                task = state.getMostRecentState(task),
                onDone = { onEvent(OrganizerEvent.SetTaskDone(task.id, it)) },
                enabled = !state.isAnyTaskFocussed,
                animatedVisibilityScope = this@AnimatedContent,
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Stores which task is currently focussed and has a local task state used for editing the focussed
 * task until it is saved to the DB.
 */
class TaskListState(val onEvent: (OrganizerEvent) -> Unit) {
  private val focussedTaskId = mutablePeekableStateOf<Long?>(null)

  private val localTaskStateId = mutablePeekableStateOf<Long?>(null)

  private var localTaskState by mutableStateOf<Task?>(null)

  private var isFocussedTaskEmpty: Boolean = false

  private val isLocalTaskStateFocussed
    get() = focussedTaskId.peek() != null && focussedTaskId.peek() == localTaskStateId.peek()

  fun focus(task: Task) {
    if (focussedTaskId.peek() == task.id) return
    saveOrDeleteFocussedTask()
    focussedTaskId.value = task.id
    isFocussedTaskEmpty = task.text.isEmpty()
    // don't update currentTask yet, only update on changes to keep modified state of previous task
  }

  fun removeFocus(save: Boolean = true) {
    if (save) saveOrDeleteFocussedTask()
    focussedTaskId.value = null
  }

  private fun invalidateLocalTaskState() {
    localTaskStateId.value = null
    localTaskState = null
  }

  fun isFocussed(task: Task) = task.id == focussedTaskId.value

  val isAnyTaskFocussed
    get() = focussedTaskId.value != null

  fun updateFocussedTaskState(task: Task) {
    localTaskState = task
    focussedTaskId.value = task.id
    localTaskStateId.value = task.id
    isFocussedTaskEmpty = task.text.isEmpty()
  }

  /** Returns the passed in [task] except if [localTaskState] has newer data for this task. */
  fun getMostRecentState(task: Task) =
    if (task.id == localTaskStateId.value) localTaskState ?: task else task

  /** Saves the focussed task, except if its [text][Task.text] is blank, in this case deletes it. */
  fun saveOrDeleteFocussedTask() {
    focussedTaskId.peek()?.let { focussedId ->
      if (isFocussedTaskEmpty) {
        deleteTask(focussedId)
      } else {
        localTaskState?.let { if (it.id == focussedId) onEvent(OrganizerEvent.UpdateTask(it)) }
      }
    }
  }

  fun deleteTask(taskId: Long) {
    onEvent(OrganizerEvent.DeleteTask(taskId))
    if (taskId == focussedTaskId.peek()) {
      // no need to save mutableState since we delete the task
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
    if (isLocalTaskStateFocussed) invalidateLocalTaskState()
    val emptyTask = tasks.findLast { it.text.isEmpty() }
    if (emptyTask != null) {
      focus(emptyTask)
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
  ToDueTheme { TaskList(tasks = tasks, modifier = Modifier.fillMaxSize()) }
}
