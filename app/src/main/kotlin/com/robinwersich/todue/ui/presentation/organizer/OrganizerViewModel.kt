package com.robinwersich.todue.ui.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import java.time.LocalDate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.domain.repository.TimeBlockRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.utility.mergeTo

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

  /** Preview data for all active task blocks in preview mode */
  private val taskBlockPreviewsFlow =
    navigationState.childTimelineBlocksFlow.flatMapLatest {
      taskRepository.getTaskBlockPreviewDataFlows(it)
    }

  /** Main data for the currently focussed task blocks (a subset of the active blocks) */
  private val focussedTaskBlockDataFlow =
    navigationState.focussedTimelineBlocksFlow.flatMapLatest { timelineBlocks ->
      taskRepository.getTaskBlockMainDataFlows(timelineBlocks)
    }

  /**
   * Main and preview data for all active blocks, with main data taking precedence over preview data
   */
  val taskBlockDataFlow =
    combine(
      taskBlockPreviewsFlow,
      focussedTaskBlockDataFlow,
    ) { activeTimelineBlocks, focussedTimelineBlocks ->
      activeTimelineBlocks
        .mergeTo(
          persistentMapOf<TimelineBlock, TaskBlock>().builder(),
          focussedTimelineBlocks,
        ) { _, preview, main ->
          TaskBlock(
            timelineBlock = main.timelineBlock,
            mainData = main.mainData,
            previewData = preview.previewData,
          )
        }
        .build()
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
