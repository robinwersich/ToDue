package com.robinwersich.todue.ui.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.domain.repository.TimeBlockRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState

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
      focussedTimelineBlocks.associateWith {
        if (it == currentTaskBlock.timelineBlock) currentTaskBlock
        else activeTaskBlocks.getOrElse(it) { TaskBlock(it) }
      }
    }

  fun handleEvent(event: OrganizerEvent) {
    when (event) {
      is OrganizerEvent.AddTask ->
        viewModelScope.launch {
          taskRepository.insertTask(
            Task(
              text = "",
              scheduledBlock = event.timelineBlock,
              dueDate = event.timelineBlock.section.endInclusive,
            )
          )
        }
      is OrganizerEvent.DeleteTask -> {
        viewModelScope.launch { taskRepository.deleteTask(event.taskId) }
      }
      is OrganizerEvent.UpdateTask ->
        viewModelScope.launch { taskRepository.updateTask(event.task) }
      is OrganizerEvent.SetTaskDone -> {
        val doneDate = if (event.done) LocalDate.now() else null
        viewModelScope.launch { taskRepository.setDoneDate(event.taskId, doneDate) }
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
