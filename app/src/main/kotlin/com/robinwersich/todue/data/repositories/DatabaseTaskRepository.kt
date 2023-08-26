package com.robinwersich.todue.data.repositories

import com.robinwersich.todue.data.database.TaskDao
import com.robinwersich.todue.data.entities.Task
import com.robinwersich.todue.data.entities.TimeBlockSpec
import java.time.LocalDate

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task)
  override suspend fun updateTask(task: Task) = taskDao.update(task)
  override suspend fun deleteTask(id: Long) = taskDao.delete(id)
  override suspend fun setTimeBlockSpec(id: Long, spec: TimeBlockSpec) {
    taskDao.setTimeBlockSpec(id, spec.endDate, spec.unit)
  }
  override suspend fun setDueDate(id: Long, date: LocalDate) = taskDao.setDueDate(id, date)
  override suspend fun setText(id: Long, text: String) = taskDao.setText(id, text)
  override suspend fun setDoneDate(id: Long, date: LocalDate?) = taskDao.setDoneDate(id, date)
  override fun getTask(id: Long) = taskDao.getTask(id)
  override fun getAllTasks() = taskDao.getAllTasks()
}
