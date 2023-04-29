package de.robinwersich.todue.data.repositories

import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  suspend fun insertTask(task: Task)
  suspend fun updateTask(task: Task)
  suspend fun deleteTask(task: Task)

  suspend fun setDoneDate(id: Int, doneDate: LocalDate?)
  fun getTask(id: Int): Flow<Task>
  fun getAllTasks(): Flow<List<Task>>
}
