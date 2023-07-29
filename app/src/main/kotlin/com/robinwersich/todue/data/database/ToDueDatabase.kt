package com.robinwersich.todue.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.robinwersich.todue.data.entities.Task

@Database(entities = [Task::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ToDueDatabase : RoomDatabase() {
  abstract fun todoDao(): TaskDao

  companion object {
    @Volatile private var Instance: ToDueDatabase? = null

    fun getDatabase(context: Context): ToDueDatabase =
      Instance
        ?: synchronized(this) {
          Room.databaseBuilder(context, ToDueDatabase::class.java, "todue_database")
            .fallbackToDestructiveMigration()
            .build()
            .also { Instance = it }
        }
  }
}
