package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.robinwersich.todue.domain.model.TimeUnit

@Entity(tableName = "timeline")
data class TimelineEntity(
  @ColumnInfo("id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo("time_block_unit") val timeBlockUnit: TimeUnit,
  @ColumnInfo("time_block_size") val timeBlockSize: Int,
)
