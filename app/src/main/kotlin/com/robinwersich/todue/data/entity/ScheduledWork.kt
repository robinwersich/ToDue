package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import kotlin.time.Duration

data class ScheduledWork(
  /** The timeline and date range the work is scheduled for. */
  @Embedded(prefix = "scheduled_") val scheduledTimelineSection: TimelineSectionColumns,
  /** The estimated time work takes to execute. */
  @ColumnInfo(name = "estimated_duration") val estimatedDuration: Duration,
)
