package com.robinwersich.todue.data.repositories

import com.robinwersich.todue.data.entities.Task
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  suspend fun insertTask(task: Task): Long
  suspend fun updateTask(task: Task)
  suspend fun deleteTask(id: Long)
  suspend fun setText(id: Long, text: String)
  suspend fun setDoneDate(id: Long, doneDate: LocalDate?)
  fun getTask(id: Long): Flow<Task>
  fun getAllTasks(): Flow<List<Task>>
}
