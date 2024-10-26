package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline

@Entity(tableName = "timeline")
data class TimelineEntity(
  @ColumnInfo("id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
  @ColumnInfo("time_block_unit") val timeUnit: TimeUnit,
)

fun TimelineEntity.toModel(): Timeline = Timeline(id = id, timeUnit = timeUnit)

fun Timeline.toEntity() = TimelineEntity(id = id, timeUnit = timeUnit)
