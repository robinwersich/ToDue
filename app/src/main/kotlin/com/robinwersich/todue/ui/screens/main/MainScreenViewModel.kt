package com.robinwersich.todue.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.robinwersich.todue.data.entities.Task
import com.robinwersich.todue.data.repositories.DatabaseTaskRepository
import com.robinwersich.todue.toDueApplication
import com.robinwersich.todue.ui.components.TaskEvent
import com.robinwersich.todue.ui.components.TaskFocusLevel
import com.robinwersich.todue.ui.components.TaskState
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainScreenViewModel(
  private val taskRepository: DatabaseTaskRepository,
) : ViewModel() {
  private val focussedTaskIdFlow: MutableStateFlow<Long?> = MutableStateFlow(null)
  private val taskList: Flow<List<TaskState>> =
    taskRepository.getAllTasks().combine(focussedTaskIdFlow) { tasks, focussedTaskId ->
      tasks.map { task ->
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
    }

  val viewState: StateFlow<MainScreenState> =
    taskList
      .map { taskList -> MainScreenState(taskList) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainScreenState()
      )

  fun handleEvent(event: TaskEvent) {
    when (event) {
      is TaskEvent.Add ->
        viewModelScope.launch {
          focussedTaskIdFlow.value =
            taskRepository.insertTask(Task(text = "", dueDate = LocalDate.now()))
        }
      is TaskEvent.Remove -> {
        if (focussedTaskIdFlow.value == event.id) focussedTaskIdFlow.value = null
        viewModelScope.launch { taskRepository.deleteTask(event.id) }
      }
      is TaskEvent.Expand -> focussedTaskIdFlow.value = event.id
      is TaskEvent.Collapse -> focussedTaskIdFlow.value = null
      is TaskEvent.SetText -> viewModelScope.launch { taskRepository.setText(event.id, event.text) }
      is TaskEvent.SetDone -> {
        val doneDate = if (event.done) LocalDate.now() else null
        viewModelScope.launch { taskRepository.setDoneDate(event.id, doneDate) }
      }
    }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer { MainScreenViewModel(toDueApplication().container.tasksRepository) }
    }
  }
}
