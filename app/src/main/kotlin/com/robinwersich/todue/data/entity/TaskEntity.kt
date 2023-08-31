package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.robinwersich.todue.domain.model.Task
import java.time.LocalDate

@Entity(tableName = "todo")
data class TaskEntity(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "text") val text: String,
  @Embedded(prefix = "time_block_") val timeBlockModel: TimeBlockEntity,
  @ColumnInfo(name = "due_date") val dueDate: LocalDate,
  @ColumnInfo(name = "done_date") val doneDate: LocalDate? = null
)

fun TaskEntity.toModel() =
  Task(
    id = id,
    text = text,
    timeBlock = timeBlockModel.toModel(),
    dueDate = dueDate,
    doneDate = doneDate
  )

fun Task.toEntity() =
  TaskEntity(
    id = id,
    text = text,
    timeBlockModel = timeBlock.toEntity(),
    dueDate = dueDate,
    doneDate = doneDate
  )
