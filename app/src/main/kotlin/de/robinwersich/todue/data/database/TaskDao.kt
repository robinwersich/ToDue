package de.robinwersich.todue.data.database

import androidx.room.*
import de.robinwersich.todue.data.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
  @Insert suspend fun insert(task: Task)
  @Update suspend fun update(task: Task)
  @Delete suspend fun delete(task: Task)
  @Query("SELECT * FROM todo WHERE id = :id") fun getTask(id: Long): Flow<Task>
  @Query("SELECT * FROM todo") fun getAllTasks(): Flow<List<Task>>
}
