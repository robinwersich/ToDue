package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import com.robinwersich.todue.domain.model.TimeBlock
import java.time.LocalDate

enum class TimeBlockUnit {
  DAY,
  WEEK,
  MONTH
}

data class TimeBlockEntity(
  @ColumnInfo(name = "unit") val unit: TimeBlockUnit,
  @ColumnInfo(name = "end_date") val endDate: LocalDate,
)

fun TimeBlockEntity.toModel() =
  when (unit) {
    TimeBlockUnit.DAY -> TimeBlock.Day(endDate)
    TimeBlockUnit.WEEK -> TimeBlock.Week(endDate)
    TimeBlockUnit.MONTH -> TimeBlock.Month(endDate)
  }

fun TimeBlock.toEntity() = TimeBlockEntity(
  unit = when (this) {
    is TimeBlock.Day -> TimeBlockUnit.DAY
    is TimeBlock.Week -> TimeBlockUnit.WEEK
    is TimeBlock.Month -> TimeBlockUnit.MONTH
  },
  endDate = endDate
)
