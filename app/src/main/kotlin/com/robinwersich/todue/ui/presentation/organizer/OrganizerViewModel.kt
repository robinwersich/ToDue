package com.robinwersich.todue.ui.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.toDueApplication
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrganizerViewModel(
  private val taskRepository: TaskRepository,
) : ViewModel() {
  private val focussedTaskIdFlow = MutableStateFlow<Long?>(null)

  private val taskList: Flow<ImmutableList<TaskViewState>> =
    taskRepository.getAllTasks().combine(focussedTaskIdFlow) { tasks, focussedTaskId ->
      tasks
        .map { task ->
          TaskViewState(
            id = task.id,
            text = task.text,
            timeBlock = task.timeBlock,
            dueDate = task.dueDate,
            doneDate = task.doneDate,
            focusLevel =
              when (focussedTaskId) {
                null -> FokusLevel.NEUTRAL
                task.id -> FokusLevel.FOCUSSED
                else -> FokusLevel.BACKGROUND
              }
          )
        }
        .toImmutableList()
    }

  val viewState: StateFlow<OrganizerState> =
    taskList
      .map { OrganizerState(tasks = it) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OrganizerState()
      )

  fun handleEvent(event: OrganizerEvent) {
    when (event) {
      is AddTask ->
        viewModelScope.launch {
          focussedTaskIdFlow.value =
            taskRepository.insertTask(
              Task(text = "", timeBlock = TimeBlock.Day(), dueDate = LocalDate.now())
            )
        }
      is ExpandTask -> focussedTaskIdFlow.value = event.taskId
      is CollapseTasks -> focussedTaskIdFlow.value = null
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
        if (focussedTaskIdFlow.value == taskId) focussedTaskIdFlow.value = null
        viewModelScope.launch { taskRepository.deleteTask(taskId) }
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer { OrganizerViewModel(toDueApplication().container.tasksRepository) }
    }
  }
}
