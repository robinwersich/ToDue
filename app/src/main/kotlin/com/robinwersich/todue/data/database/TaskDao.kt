package com.robinwersich.todue.data.database

import androidx.room.*
import com.robinwersich.todue.data.entity.TaskEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: TaskEntity): Long

  @Update suspend fun update(task: TaskEntity)

  @Query("DELETE FROM todo WHERE id = :id") suspend fun delete(id: Long)

  @Query("UPDATE todo SET text = :text WHERE id = :id") suspend fun setText(id: Long, text: String)

  @Query("UPDATE todo SET scheduled_start = :startDate, scheduled_end = :endDate WHERE id = :id")
  suspend fun setScheduledRange(id: Long, startDate: LocalDate, endDate: LocalDate)

  @Query("UPDATE todo SET due_date = :date WHERE id = :id")
  suspend fun setDueDate(id: Long, date: LocalDate)

  @Query("UPDATE todo SET done_date = :date WHERE id = :id")
  suspend fun setDoneDate(id: Long, date: LocalDate?)

  @Query("SELECT * FROM todo WHERE id = :id") fun getTask(id: Long): Flow<TaskEntity>

  @Query("SELECT * FROM todo") fun getAllTasks(): Flow<List<TaskEntity>>
}
