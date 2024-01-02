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

/**
 * A time unit instance is a specific instance of a time unit. For example, a time unit instance of
 * the time unit [week][TimeUnit.WEEK] is the week 2021-W02. All time unit instances can either be
 * created from a corresponding [Temporal][java.time.temporal.Temporal] or from a [LocalDate], which
 * results in the time unit instance that *contains* this date.
 */
sealed interface TimeUnitInstance {
  /** The earliest date that is contained in this time unit instance. */
  abstract val startDate: LocalDate
  /** The latest date that is contained in this time unit instance. */
  abstract val endDate: LocalDate
  /** The [TimeUnit] enum entry of this instance. */
  abstract val unit: TimeUnit

  /** Returns a new instance that is [amount] time units after this instance. */
  abstract operator fun plus(amount: Long): TimeUnitInstance
  /** Returns a new instance that is [amount] time units before this instance. */
  operator fun minus(amount: Long): TimeUnitInstance = this + -amount

  val sequence: Sequence<TimeUnitInstance>
    get() = generateSequence(this) { it + 1 }

  data class Day(val date: LocalDate = LocalDate.now()) : TimeUnitInstance {
    override val unit: TimeUnit
      get() = TimeUnit.DAY

    override val startDate: LocalDate = date
    override val endDate: LocalDate = date

    override operator fun plus(amount: Long) = Day(date.plusDays(amount))

    override fun toString() = date.toString()
  }

  data class Week(val yearWeek: YearWeek = YearWeek.now()) : TimeUnitInstance {
    override val unit
      get() = TimeUnit.WEEK

    override val startDate: LocalDate = yearWeek.atDay(DayOfWeek.MONDAY)
    override val endDate: LocalDate = yearWeek.atDay(DayOfWeek.SUNDAY)

    constructor(date: LocalDate) : this(YearWeek.from(date))

    override operator fun plus(amount: Long) = Week(yearWeek.plusWeeks(amount))

    override fun toString() = yearWeek.toString()
  }

  data class Month(val yearMonth: YearMonth = YearMonth.now()) : TimeUnitInstance {
    override val unit
      get() = TimeUnit.MONTH

    override val startDate: LocalDate = yearMonth.atDay(1)
    override val endDate: LocalDate = yearMonth.atEndOfMonth()

    constructor(date: LocalDate) : this(YearMonth.from(date))

    override operator fun plus(amount: Long) = Month(yearMonth.plusMonths(amount))

    override fun toString() = yearMonth.toString()
  }
}

data class Timeline(val id: Int, val timeBlockUnit: TimeUnit) {
  val now
    get() = timeBlockUnit.instanceFrom(LocalDate.now())
}
