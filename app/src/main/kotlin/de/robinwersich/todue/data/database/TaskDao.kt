package de.robinwersich.todue.data.database

import androidx.room.*
import de.robinwersich.todue.data.entities.Task
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task)
  @Update suspend fun update(task: Task)
  @Query("DELETE FROM todo WHERE id = :id") suspend fun delete(id: Int)
  @Query("UPDATE todo SET text = :text WHERE id = :id") suspend fun setText(id: Int, text: String)
  @Query("UPDATE todo SET done_date = :doneDate WHERE id = :id")
  suspend fun setDoneDate(id: Int, doneDate: LocalDate?)
  @Query("SELECT * FROM todo WHERE id = :id") fun getTask(id: Int): Flow<Task>
  @Query("SELECT * FROM todo") fun getAllTasks(): Flow<List<Task>>
}
