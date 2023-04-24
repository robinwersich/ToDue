package de.robinwersich.todue.data

import android.content.Context
import de.robinwersich.todue.data.database.ToDueDatabase
import de.robinwersich.todue.data.repositories.TaskRepository

class AppDataContainer(private val context: Context) {
  val tasksRepository: TaskRepository by lazy {
    TaskRepository(ToDueDatabase.getDatabase(context).todoDao())
  }
}
