package com.robinwersich.todue.data.repository

import com.robinwersich.todue.data.database.TaskDao
import com.robinwersich.todue.data.entity.toEntity
import com.robinwersich.todue.data.entity.toModel
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimeUnitInstance
import com.robinwersich.todue.domain.repository.TaskRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.map

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())

  override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

  override suspend fun deleteTask(id: Long) = taskDao.delete(id)

  override suspend fun setTimeBlock(id: Long, timeBlock: TimeUnitInstance<*>) =
    taskDao.setScheduledRange(id, startDate = timeBlock.startDate, endDate = timeBlock.endDate)

  override suspend fun setDueDate(id: Long, date: LocalDate) = taskDao.setDueDate(id, date)

  override suspend fun setText(id: Long, text: String) = taskDao.setText(id, text)

  override suspend fun setDoneDate(id: Long, date: LocalDate?) = taskDao.setDoneDate(id, date)

  override fun getTask(id: Long) = taskDao.getTask(id).map { it.toModel() }

  override fun getAllTasks() = taskDao.getAllTasks().map { tasks -> tasks.map { it.toModel() } }
}
