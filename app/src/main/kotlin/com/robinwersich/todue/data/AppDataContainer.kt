package com.robinwersich.todue.data

import android.content.Context
import com.robinwersich.todue.data.database.ToDueDatabase
import com.robinwersich.todue.data.repository.DatabaseTaskRepository
import com.robinwersich.todue.data.repository.DatabaseTimeBlockRepository

class AppDataContainer(private val context: Context) {
  val tasksRepository: DatabaseTaskRepository by lazy {
    DatabaseTaskRepository(ToDueDatabase.getDatabase(context).todoDao())
  }
  val timeBlockRepository: DatabaseTimeBlockRepository by lazy {
    DatabaseTimeBlockRepository(ToDueDatabase.getDatabase(context).timelineDao())
  }
}
