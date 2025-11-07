package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimelineBlock
import java.time.Duration
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

@Entity(
  tableName = "todo",
  foreignKeys =
    [
      ForeignKey(
        entity = TimelineEntity::class,
        parentColumns = ["id"],
        childColumns = ["scheduled_timeline_id"],
        onDelete = ForeignKey.RESTRICT,
      )
    ],
  indices =
    [Index("scheduled_timeline_id"), Index("scheduled_start"), Index("scheduled_end_inclusive")],
)
data class TaskEntity(
  /** The unique identifier of the task. */
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
  /** Description of the task. */
  @ColumnInfo(name = "text") val text: String,
  /** The timeline and date range this task is scheduled for. */
  @Embedded(prefix = "scheduled_") val scheduledTimelineSection: TimelineSectionColumns,
  /** The date on which the task must be done at latest. */
  @ColumnInfo(name = "due_date") val dueDate: LocalDate,
  /** The estimated time this task takes to execute. */
  @ColumnInfo(name = "estimated_duration") val estimatedDuration: Duration,
  /** The date on which the task was done (null if it wasn't done yet). */
  @ColumnInfo(name = "done_date") val doneDate: LocalDate? = null,
)

fun TaskEntity.toModel(scheduledBlock: TimelineBlock) =
  Task(
    id = id,
    text = text,
    scheduledBlock = scheduledBlock,
    dueDate = dueDate,
    doneDate = doneDate,
  )

fun Task.toEntity() =
  TaskEntity(
    id = id,
    text = text,
    scheduledTimelineSection = scheduledBlock.toEntity(),
    dueDate = dueDate,
    estimatedDuration = 1.hours.toJavaDuration(),
    doneDate = doneDate,
  )
