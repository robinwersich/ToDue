package de.robinwersich.todue.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.robinwersich.todue.data.entities.Task
import de.robinwersich.todue.data.repositories.DatabaseTaskRepository
import de.robinwersich.todue.toDueApplication
import de.robinwersich.todue.ui.components.TaskUiState
import de.robinwersich.todue.ui.components.toUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
  private val taskRepository: DatabaseTaskRepository,
) : ViewModel() {
  private val expandedTaskId: MutableStateFlow<Int?> = MutableStateFlow(null)
  val taskList: StateFlow<List<TaskUiState>> =
    taskRepository
      .getAllTasks()
      .combine(expandedTaskId) { tasks, expandedTaskId ->
        tasks.map { it.toUiState(expanded = it.id == expandedTaskId) }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
      )

  fun addTask() {
    viewModelScope.launch { taskRepository.insertTask(Task(text = "", dueDate = LocalDate.now())) }
  }

  fun deleteTask(taskId: Int) {
    viewModelScope.launch { taskRepository.deleteTask(taskId) }
  }

  fun toggleExpansion(taskId: Int?) {
    expandedTaskId.value = if (taskId == expandedTaskId.value) null else taskId
  }

  fun setText(taskId: Int, text: String) {
    viewModelScope.launch { taskRepository.setText(taskId, text) }
  }

  fun setDone(taskId: Int, done: Boolean) {
    val doneDate = if (done) LocalDate.now() else null
    viewModelScope.launch { taskRepository.setDoneDate(taskId, doneDate) }
  }

  companion object {
    val Factory = viewModelFactory {
      initializer { HomeScreenViewModel(toDueApplication().container.tasksRepository) }
    }
  }
}
