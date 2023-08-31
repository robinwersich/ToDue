package com.robinwersich.todue.domain.repository

import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimeBlock
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  suspend fun insertTask(task: Task): Long
  suspend fun updateTask(task: Task)
  suspend fun deleteTask(id: Long)
  suspend fun setText(id: Long, text: String)
  suspend fun setTimeBlock(id: Long, timeBlock: TimeBlock)
  suspend fun setDueDate(id: Long, date: LocalDate)
  suspend fun setDoneDate(id: Long, date: LocalDate?)
  fun getTask(id: Long): Flow<Task>
  fun getAllTasks(): Flow<List<Task>>
}
