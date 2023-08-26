package com.robinwersich.todue.data.database

import androidx.room.*
import com.robinwersich.todue.data.entities.Task
import com.robinwersich.todue.data.entities.TimeBlockUnit
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task): Long
  @Update suspend fun update(task: Task)
  @Query("DELETE FROM todo WHERE id = :id") suspend fun delete(id: Long)
  @Query("UPDATE todo SET text = :text WHERE id = :id") suspend fun setText(id: Long, text: String)
  @Query("UPDATE todo SET time_block_end_date = :endDate, time_block_unit = :unit WHERE id = :id")
  suspend fun setTimeBlockSpec(id: Long, endDate: LocalDate, unit: TimeBlockUnit)
  @Query("UPDATE todo SET due_date = :date WHERE id = :id")
  suspend fun setDueDate(id: Long, date: LocalDate)
  @Query("UPDATE todo SET done_date = :date WHERE id = :id")
  suspend fun setDoneDate(id: Long, date: LocalDate?)
  @Query("SELECT * FROM todo WHERE id = :id") fun getTask(id: Long): Flow<Task>
  @Query("SELECT * FROM todo") fun getAllTasks(): Flow<List<Task>>
}
