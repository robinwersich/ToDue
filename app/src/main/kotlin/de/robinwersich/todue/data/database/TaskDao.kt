package de.robinwersich.todue.data.database

import androidx.room.*
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task)
  @Update suspend fun update(task: Task)
  @Delete suspend fun delete(task: Task)

  @Query("UPDATE todo SET done_date = :doneDate WHERE id = :id")
  suspend fun setDoneDate(id: Int, doneDate: LocalDate?)
  @Query("SELECT * FROM todo WHERE id = :id") fun getTask(id: Int): Flow<Task>
  @Query("SELECT * FROM todo") fun getAllTasks(): Flow<List<Task>>
}
