package com.robinwersich.todue.ui.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.Duration
import java.time.LocalDate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.domain.repository.TimeBlockRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TaskBlockViewState
import com.robinwersich.todue.ui.presentation.organizer.state.TaskViewState
import com.robinwersich.todue.utility.mapToImmutableList

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerViewModel(
  private val taskRepository: TaskRepository,
  private val timeBlockRepository: TimeBlockRepository,
) : ViewModel() {
  val navigationState = NavigationState()
  private val timelinesFlow = timeBlockRepository.getTimelines()

  init {
    viewModelScope.launch { timelinesFlow.collect { navigationState.setTimelines(it) } }
    viewModelScope.launch { navigationState.updateTimelineAnchorsOnSwipe() }
    viewModelScope.launch { navigationState.updateDateAnchorsOnSwipe() }
  }

  private val activeTaskBlocksFlow =
    navigationState.activeTimelineBlocksFlow.map { taskRepository.getTaskBlocksMap(it) }
  private val currentTaskBlockFlow =
    navigationState.currentTimelineBlockFlow.flatMapLatest { taskRepository.getTaskBlockFlow(it) }
  val focussedTaskBlockViewStatesFlow =
    combine(
      activeTaskBlocksFlow,
      currentTaskBlockFlow,
      navigationState.focussedTimelineBlocksFlow,
    ) { activeTaskBlocks, currentTaskBlock, focussedTimelineBlocks ->
      focussedTimelineBlocks.associateWithTo(
        persistentMapOf<TimelineBlock, TaskBlockViewState>().builder()
      ) {
        val taskBlock =
          if (it == currentTaskBlock.timelineBlock) currentTaskBlock
          else activeTaskBlocks.getOrElse(it) { TaskBlock(it) }
        taskBlock.toViewState()
      }
    }

  private fun TaskBlock.toViewState() =
    TaskBlockViewState(
      timelineBlock,
      tasks.mapToImmutableList { task ->
        TaskViewState(
          id = task.id,
          text = task.text,
          timelineBlock = timelineBlock,
          dueDate = task.dueDate,
          doneDate = task.doneDate,
        )
      },
    )

  fun handleEvent(event: OrganizerEvent) {
    when (event) {
      is OrganizerEvent.AddTask ->
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
        }
      is OrganizerEvent.ForTask -> handleModifyTaskEvent(event.event, event.taskId)
    }
  }

  private fun handleModifyTaskEvent(event: TaskEvent, taskId: Long) {
    when (event) {
      is TaskEvent.SetText -> viewModelScope.launch { taskRepository.setText(taskId, event.text) }
      is TaskEvent.SetDone -> {
        val doneDate = if (event.done) LocalDate.now() else null
        viewModelScope.launch { taskRepository.setDoneDate(taskId, doneDate) }
      }
      is TaskEvent.SetTimeBlock ->
        viewModelScope.launch { taskRepository.setTimeBlock(taskId, event.timeBlock) }
      is TaskEvent.SetDueDate ->
        viewModelScope.launch { taskRepository.setDueDate(taskId, event.date) }
      is TaskEvent.Delete -> {
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
