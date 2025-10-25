package com.robinwersich.todue.data.database

import androidx.room.*
import com.robinwersich.todue.data.entity.TimelineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineDao {
  @Query("SELECT * FROM timeline") fun getTimelines(): Flow<List<TimelineEntity>>
}
