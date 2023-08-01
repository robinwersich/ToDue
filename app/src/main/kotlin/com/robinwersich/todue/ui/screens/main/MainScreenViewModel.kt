package com.robinwersich.todue.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.robinwersich.todue.data.entities.Task
import com.robinwersich.todue.data.repositories.DatabaseTaskRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.components.TaskFocusLevel
import com.robinwersich.todue.ui.components.TaskListEvent
import com.robinwersich.todue.ui.components.TaskModifyEvent
import com.robinwersich.todue.ui.components.TaskState
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainScreenViewModel(
  private val taskRepository: DatabaseTaskRepository,
) : ViewModel() {
  private val focussedTaskIdFlow = MutableStateFlow<Long?>(null)
  private val taskPropertyOverlayFlow = MutableStateFlow<TaskPropertyOverlay?>(null)

  private val taskList: Flow<ImmutableList<TaskState>> =
    taskRepository.getAllTasks().combine(focussedTaskIdFlow) { tasks, focussedTaskId ->
      tasks
        .map { task ->
          TaskState(
            id = task.id,
            text = task.text,
            dueDate = task.dueDate,
            doneDate = task.doneDate,
            focusLevel =
              when (focussedTaskId) {
                null -> TaskFocusLevel.NEUTRAL
                task.id -> TaskFocusLevel.FOCUSSED
                else -> TaskFocusLevel.BACKGROUND
              }
          )
        }
        .toImmutableList()
    }

  val viewState: StateFlow<MainScreenState> =
    taskList
      .combine(taskPropertyOverlayFlow) { taskList, overlay ->
        MainScreenState(tasks = taskList, taskPropertyOverlay = overlay)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainScreenState()
      )

  fun handleEvent(event: MainScreenEvent) {
    when (event) {
      is TaskListEvent.AddTask ->
        viewModelScope.launch {
          focussedTaskIdFlow.value =
            taskRepository.insertTask(Task(text = "", dueDate = LocalDate.now()))
        }
      is TaskListEvent.ExpandTask -> focussedTaskIdFlow.value = event.taskId
      is TaskListEvent.CollapseTasks -> focussedTaskIdFlow.value = null
      is TaskListEvent.ModifyTask -> handleTaskModifyEvent(event.event, event.taskId)
    }
  }

  private fun handleTaskModifyEvent(event: TaskModifyEvent, taskId: Long) {
    when (event) {
      is TaskModifyEvent.Delete -> {
        if (focussedTaskIdFlow.value == taskId) focussedTaskIdFlow.value = null
        viewModelScope.launch { taskRepository.deleteTask(taskId) }
      }
      is TaskModifyEvent.SetText ->
        viewModelScope.launch { taskRepository.setText(taskId, event.text) }
      is TaskModifyEvent.SetDone -> {
        val doneDate = if (event.done) LocalDate.now() else null
        viewModelScope.launch { taskRepository.setDoneDate(taskId, doneDate) }
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer { MainScreenViewModel(toDueApplication().container.tasksRepository) }
    }
  }
}
