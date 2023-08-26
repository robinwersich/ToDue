package com.robinwersich.todue.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import org.threeten.extra.YearWeek

enum class TimeBlockUnit {
  DAY,
  WEEK,
  MONTH
}

@Immutable
data class TimeBlockSpec(
  @ColumnInfo(name = "unit") val unit: TimeBlockUnit,
  @ColumnInfo(name = "end_date") val endDate: LocalDate,
)

fun TimeBlockSpec.toTimeBlock() =
  when (unit) {
    TimeBlockUnit.DAY -> TimeBlock.Day(endDate)
    TimeBlockUnit.WEEK -> TimeBlock.Week(endDate)
    TimeBlockUnit.MONTH -> TimeBlock.Month(endDate)
  }

sealed interface TimeBlock {
  abstract val unit: TimeBlockUnit
  abstract val endDate: LocalDate
  abstract val name: String

  data class Day(private val date: LocalDate = LocalDate.now()) : TimeBlock {
    override val unit = TimeBlockUnit.DAY
    override val endDate: LocalDate
      get() = date
    override val name: String
      get() = date.toString()
  }
  data class Week(private val yearWeek: YearWeek = YearWeek.now()) : TimeBlock {
    constructor(date: LocalDate) : this(YearWeek.from(date))
    override val unit = TimeBlockUnit.WEEK
    override val endDate: LocalDate
      get() = yearWeek.atDay(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
    override val name: String
      get() = yearWeek.toString()
  }
  data class Month(private val yearMonth: YearMonth = YearMonth.now()) : TimeBlock {
    constructor(date: LocalDate) : this(YearMonth.from(date))
    override val unit = TimeBlockUnit.MONTH
    override val endDate: LocalDate
      get() = yearMonth.atEndOfMonth()
    override val name: String
      get() = yearMonth.toString()
  }

  fun toSpec() = TimeBlockSpec(unit, endDate)
}
