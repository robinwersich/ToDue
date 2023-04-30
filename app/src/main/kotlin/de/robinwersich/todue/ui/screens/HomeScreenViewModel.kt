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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenViewModel(
  private val taskRepository: DatabaseTaskRepository,
) : ViewModel() {
  val taskList: StateFlow<List<TaskUiState>> =
    taskRepository
      .getAllTasks()
      .map { tasks -> tasks.map { it.toUiState() } }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
      )

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
