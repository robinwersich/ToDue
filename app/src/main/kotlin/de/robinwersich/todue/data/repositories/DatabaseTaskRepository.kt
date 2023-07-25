package de.robinwersich.todue.data.repositories

import de.robinwersich.todue.data.database.TaskDao
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task)
  override suspend fun updateTask(task: Task) = taskDao.update(task)
  override suspend fun deleteTask(id: Long) = taskDao.delete(id)
  override suspend fun setText(id: Long, text: String) = taskDao.setText(id, text)
  override suspend fun setDoneDate(id: Long, doneDate: LocalDate?) =
    taskDao.setDoneDate(id, doneDate)
  override fun getTask(id: Long) = taskDao.getTask(id)
  override fun getAllTasks() = taskDao.getAllTasks()
}
