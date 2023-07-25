package de.robinwersich.todue.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "todo")
data class Task(
  @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long = 0,
  @ColumnInfo(name = "text") val text: String,
  @ColumnInfo(name = "due_date") val dueDate: LocalDate,
  @ColumnInfo(name = "done_date") val doneDate: LocalDate? = null
)
