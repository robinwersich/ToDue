package de.robinwersich.todue.data

import android.content.Context
import de.robinwersich.todue.data.database.ToDueDatabase
import de.robinwersich.todue.data.repositories.DatabaseTaskRepository

class AppDataContainer(private val context: Context) {
  val tasksRepository: DatabaseTaskRepository by lazy {
    DatabaseTaskRepository(ToDueDatabase.getDatabase(context).todoDao())
  }
}
