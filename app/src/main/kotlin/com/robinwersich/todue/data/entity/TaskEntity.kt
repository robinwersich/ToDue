package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimeUnitInstance
import java.time.LocalDate

@Entity(
  tableName = "todo",
  foreignKeys =
    [
      ForeignKey(
        entity = TimelineEntity::class,
        parentColumns = ["id"],
        childColumns = ["timeline_id"],
        onDelete = ForeignKey.RESTRICT,
      )
    ],
  indices = [Index("timeline_id"), Index("scheduled_start"), Index("scheduled_end")]
)
data class TaskEntity(
  /** The unique identifier of the task. */
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
  /** Description of the task. */
  @ColumnInfo(name = "text") val text: String,
  /** The timeline in which the task is currently scheduled. */
  @ColumnInfo(name = "timeline_id") val timelineId: Int,
  /** The inclusive start date of the time block in which the task is currently scheduled. */
  @ColumnInfo(name = "scheduled_start") val scheduledStart: LocalDate,
  /** The inclusive end date of the time block in which the task is currently scheduled. */
  @ColumnInfo(name = "scheduled_end") val scheduledEnd: LocalDate,
  /** The date on which the task must be done at latest. */
  @ColumnInfo(name = "due_date") val dueDate: LocalDate,
  /** The date on which the task was done (null if it wasn't done yet). */
  @ColumnInfo(name = "done_date") val doneDate: LocalDate? = null
)

fun TaskEntity.toModel() =
  Task(
    id = id,
    text = text,
    // TODO: This is a hack to make the app work with the new data model. Remove this once the new
    // data model is fully implemented.
    scheduledTimeBlock =
      TimeBlock(
        timelineId,
        start = TimeUnitInstance.Day(scheduledStart),
        TimeUnitInstance.Day(scheduledEnd)
      ),
    dueDate = dueDate,
    doneDate = doneDate
  )

fun Task.toEntity() =
  TaskEntity(
    id = id,
    text = text,
    timelineId = scheduledTimeBlock.timelineId,
    scheduledStart = scheduledTimeBlock.startDate,
    scheduledEnd = scheduledTimeBlock.endDate,
    dueDate = dueDate,
    doneDate = doneDate
  )
