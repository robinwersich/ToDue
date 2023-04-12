package de.robinwersich.todue.data.repositories

import de.robinwersich.todue.data.database.TaskDao
import de.robinwersich.todue.data.entities.Task

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    fun getTask(id: Long) = taskDao.getTask(id)
    fun getAllTasks() = taskDao.getAllTasks()
}