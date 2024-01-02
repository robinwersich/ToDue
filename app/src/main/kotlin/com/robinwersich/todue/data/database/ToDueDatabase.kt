package com.robinwersich.todue.data.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.robinwersich.todue.data.entity.TaskEntity
import com.robinwersich.todue.data.entity.TimelineEntity

@Database(entities = [TaskEntity::class, TimelineEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ToDueDatabase : RoomDatabase() {
  abstract fun todoDao(): TaskDao

  companion object {
    @Volatile private var Instance: ToDueDatabase? = null

    fun getDatabase(context: Context): ToDueDatabase =
      Instance
        ?: synchronized(this) {
          Room.databaseBuilder(context, ToDueDatabase::class.java, "ToDue.db")
            .fallbackToDestructiveMigration()
            .addCallback(CreateInitialTimelines)
            .build()
            .also { Instance = it }
        }
  }
}

private object CreateInitialTimelines : RoomDatabase.Callback() {
  override fun onCreate(db: SupportSQLiteDatabase) {
    super.onCreate(db)
    db.insert(
      "timeline",
      SQLiteDatabase.CONFLICT_ABORT,
      ContentValues().apply { put("time_block_unit", "DAY") }
    )
  }
}
