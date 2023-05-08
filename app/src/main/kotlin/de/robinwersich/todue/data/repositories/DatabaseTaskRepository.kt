package de.robinwersich.todue.data.repositories

import de.robinwersich.todue.data.database.TaskDao
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task)
  override suspend fun updateTask(task: Task) = taskDao.update(task)
  override suspend fun deleteTask(task: Task) = taskDao.delete(task)

  override suspend fun setText(id: Int, text: String) = taskDao.setText(id, text)
  override suspend fun setDoneDate(id: Int, doneDate: LocalDate?) =
    taskDao.setDoneDate(id, doneDate)
  override fun getTask(id: Int) = taskDao.getTask(id)
  override fun getAllTasks() = taskDao.getAllTasks()
}
