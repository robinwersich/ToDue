package com.robinwersich.todue.ui.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.Duration
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.domain.repository.TimeBlockRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.presentation.organizer.state.FocusLevel
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TaskViewState
import com.robinwersich.todue.utility.mapToImmutableList

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerViewModel(
  private val taskRepository: TaskRepository,
  private val timeBlockRepository: TimeBlockRepository,
) : ViewModel() {
  val navigationState = NavigationState()
  private val timelinesFlow = timeBlockRepository.getTimelines()
  private val taskFocusFlow = MutableStateFlow<TaskFocus?>(null)

  init {
    viewModelScope.launch { timelinesFlow.collect { navigationState.setTimelines(it) } }
    viewModelScope.launch { navigationState.updateTimelineAnchorsOnSwipe() }
    viewModelScope.launch { navigationState.updateDateAnchorsOnSwipe() }
    viewModelScope.launch {
      navigationState.currentNavPosFlow.collect { handleEvent(CollapseTasks) }
    }
  }

  private val activeTaskBlockFlow =
    navigationState.currentTimelineBlockFlow.flatMapLatest { taskRepository.getTaskBlockFlow(it) }
  //  private val prefetchedTasksFlow =
  //    navigationState.prefetchTimelineRangesFlow.map { taskRepository.getTasks(it) }

  val tasksFlow: Flow<ImmutableMap<TimelineBlock, ImmutableList<TaskViewState>>> =
    combine(activeTaskBlockFlow, taskFocusFlow) { taskBlock, focussedTaskId ->
      persistentMapOf(
        taskBlock.timelineBlock to
          taskBlock.tasks.mapToImmutableList { task ->
            TaskViewState(
              id = task.id,
              text = task.text,
              timelineBlock = taskBlock.timelineBlock,
              dueDate = task.dueDate,
              doneDate = task.doneDate,
              focusLevel =
                when (focussedTaskId) {
                  null -> FocusLevel.NEUTRAL
                  TaskFocus(task.id, false) -> FocusLevel.FOCUSSED
                  TaskFocus(task.id, true) -> FocusLevel.FOCUSSED_REQUEST_KEYBOARD
                  else -> FocusLevel.BACKGROUND
                },
            )
          }
      )
    }

  fun handleEvent(event: OrganizerEvent) {
    when (event) {
      is AddTask ->
        viewModelScope.launch {
          val newTaskId =
            taskRepository.insertTask(
              Task(
                text = "",
                scheduledTimelineRange = event.timelineBlock,
                estimatedDuration = Duration.ofHours(1),
                dueDate = event.timelineBlock.section.endInclusive,
              )
            )
          taskFocusFlow.value = TaskFocus(newTaskId, requestKeyboard = true)
        }
      is ExpandTask ->
        if (!navigationState.isSplitView) {
          taskFocusFlow.value = TaskFocus(event.taskId, requestKeyboard = false)
        }
      is CollapseTasks -> taskFocusFlow.value = null
      is ModifyTask -> handleModifyTaskEvent(event.event, event.taskId)
    }
  }

  private fun handleModifyTaskEvent(event: ModifyTaskEvent, taskId: Long) {
    when (event) {
      is ModifyTaskEvent.SetText ->
        viewModelScope.launch { taskRepository.setText(taskId, event.text) }
      is ModifyTaskEvent.SetDone -> {
        val doneDate = if (event.done) LocalDate.now() else null
        viewModelScope.launch { taskRepository.setDoneDate(taskId, doneDate) }
      }
      is ModifyTaskEvent.SetTimeBlock ->
        viewModelScope.launch { taskRepository.setTimeBlock(taskId, event.timeBlock) }
      is ModifyTaskEvent.SetDueDate ->
        viewModelScope.launch { taskRepository.setDueDate(taskId, event.date) }
      is ModifyTaskEvent.Delete -> {
        if (taskFocusFlow.value?.id == taskId) taskFocusFlow.value = null
        viewModelScope.launch { taskRepository.deleteTask(taskId) }
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer {
        with(toDueApplication().container) {
          OrganizerViewModel(
            taskRepository = tasksRepository,
            timeBlockRepository = timeBlockRepository,
          )
        }
      }
    }
  }
}

private data class TaskFocus(val id: Long, val requestKeyboard: Boolean)
