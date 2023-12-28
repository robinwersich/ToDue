package com.robinwersich.todue.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.threeten.extra.YearWeek

enum class TimeUnit(private val instanceConstructor: (LocalDate) -> TimeUnitInstance) {
  DAY(TimeUnitInstance::Day),
  WEEK(TimeUnitInstance::Week),
  MONTH(TimeUnitInstance::Month);

  fun instanceFrom(date: LocalDate) = instanceConstructor(date)
}

sealed interface TimeUnitInstance {
  abstract val startDate: LocalDate
  abstract val endDate: LocalDate
  abstract val unit: TimeUnit

  data class Day(val date: LocalDate = LocalDate.now()) : TimeUnitInstance {
    override val unit: TimeUnit
      get() = TimeUnit.DAY

    override val startDate: LocalDate = date
    override val endDate: LocalDate = date

    override fun toString() = date.toString()
  }

  data class Week(val yearWeek: YearWeek = YearWeek.now()) : TimeUnitInstance {
    override val unit
      get() = TimeUnit.WEEK

    override val startDate: LocalDate = yearWeek.atDay(DayOfWeek.MONDAY)
    override val endDate: LocalDate = yearWeek.atDay(DayOfWeek.SUNDAY)

    constructor(date: LocalDate) : this(YearWeek.from(date))

    override fun toString() = yearWeek.toString()
  }

  data class Month(val yearMonth: YearMonth = YearMonth.now()) : TimeUnitInstance {
    override val unit
      get() = TimeUnit.MONTH

    override val startDate: LocalDate = yearMonth.atDay(1)
    override val endDate: LocalDate = yearMonth.atEndOfMonth()

    constructor(date: LocalDate) : this(YearMonth.from(date))

    override fun toString() = yearMonth.toString()
  }
}

data class TimeBlock(
  val timelineId: Int,
  val start: TimeUnitInstance,
  val end: TimeUnitInstance
) {
  val startDate: LocalDate
    get() = start.startDate

  val endDate: LocalDate
    get() = end.endDate

  constructor(
    timelineId: Int,
    timeUnitInstance: TimeUnitInstance
  ) : this(timelineId = timelineId, start = timeUnitInstance, end = timeUnitInstance)
}

data class Timeline(val id: Int, val timeBlockUnit: TimeUnit, val timeBlockSize: Int)
