package com.robinwersich.todue.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import com.robinwersich.todue.data.entity.TaskEntity

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: TaskEntity): Long

  @Query("DELETE FROM todo WHERE id = :id") suspend fun delete(id: Long)

  @Query("UPDATE todo SET text = :text WHERE id = :id") suspend fun setText(id: Long, text: String)

  @Query(
    "UPDATE todo SET scheduled_start = :startDate, scheduled_end_inclusive = :endDateInclusive WHERE id = :id"
  )
  suspend fun setScheduledRange(id: Long, startDate: LocalDate, endDateInclusive: LocalDate)

  @Query("UPDATE todo SET due_date = :date WHERE id = :id")
  suspend fun setDueDate(id: Long, date: LocalDate)

  @Query("UPDATE todo SET done_date = :date WHERE id = :id")
  suspend fun setDoneDate(id: Long, date: LocalDate?)

  /** All tasks with a scheduled range overlapping with the given date range. */
  @Query(
    """
    SELECT * FROM todo
    WHERE todo.scheduled_timeline_id = :timelineId
    AND todo.scheduled_end_inclusive >= :start
    AND todo.scheduled_start <= :endInclusive
    """
  )
  suspend fun getTasks(
    timelineId: Long,
    start: LocalDate,
    endInclusive: LocalDate,
  ): List<TaskEntity>

  /** Returns a [Flow] of all tasks with a scheduled range overlapping with the given date range. */
  @Query(
    """
    SELECT * FROM todo
    WHERE todo.scheduled_timeline_id = :timelineId
    AND todo.scheduled_end_inclusive >= :start
    AND todo.scheduled_start <= :endInclusive
    """
  )
  fun getTasksFlow(
    timelineId: Long,
    start: LocalDate,
    endInclusive: LocalDate,
  ): Flow<List<TaskEntity>>
}
