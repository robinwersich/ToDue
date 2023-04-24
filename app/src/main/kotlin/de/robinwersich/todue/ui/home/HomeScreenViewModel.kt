package de.robinwersich.todue.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.robinwersich.todue.data.entities.Task
import de.robinwersich.todue.data.repositories.TaskRepository
import de.robinwersich.todue.toDueApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeScreenViewModel(
  private val taskRepository: TaskRepository,
) : ViewModel() {
  val taskList: StateFlow<List<Task>> =
    taskRepository
      .getAllTasks()
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
      )

  companion object {
    val Factory = viewModelFactory {
      initializer { HomeScreenViewModel(toDueApplication().container.tasksRepository) }
    }
  }
}
