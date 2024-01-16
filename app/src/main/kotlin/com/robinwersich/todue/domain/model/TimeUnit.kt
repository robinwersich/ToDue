package com.robinwersich.todue.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.threeten.extra.YearWeek

enum class TimeUnit(
  val referenceSize: Float,
  private val instanceConstructor: (LocalDate) -> TimeUnitInstance<*>
) {
  DAY(1f, ::Day),
  WEEK(7f, ::Week),
  MONTH(30.5f, ::Month);

  fun instanceFrom(date: LocalDate) = instanceConstructor(date)
}

/**
 * A time unit instance is a specific instance of a time unit. For example, a time unit instance of
 * the time unit [week][TimeUnit.WEEK] is the week 2021-W02. All time unit instances can either be
 * created from a corresponding [Temporal][java.time.temporal.Temporal] or from a [LocalDate], which
 * results in the time unit instance that *contains* this date.
 *
 * @param T The type of the time unit instance. This is used to make sure that only instances of the
 *   same time unit can be compared.
 */
sealed interface TimeUnitInstance<T : TimeUnitInstance<T>> {
  /** The earliest date that is contained in this time unit instance. */
  abstract val startDate: LocalDate
  /** The latest date that is contained in this time unit instance. */
  abstract val endDate: LocalDate
  /** The [TimeUnit] enum entry of this instance. */
  abstract val unit: TimeUnit

  /** Returns a new instance that is [amount] time units after this instance. */
  abstract operator fun plus(amount: Long): T
  /** Returns a new instance that is [amount] time units before this instance. */
  operator fun minus(amount: Long): T = this + -amount

  abstract operator fun compareTo(other: TimeUnitInstance<T>): Int

  val sequence: Sequence<T>
    get() = generateSequence(this as T) { it + 1 }
}

data class Day(val date: LocalDate = LocalDate.now()) : TimeUnitInstance<Day> {
  override val unit: TimeUnit
    get() = TimeUnit.DAY

  override val startDate: LocalDate = date
  override val endDate: LocalDate = date

  override operator fun plus(amount: Long) = Day(date.plusDays(amount))

  override operator fun compareTo(other: TimeUnitInstance<Day>) =
    date.compareTo((other as Day).date)

  override fun toString() = date.toString()
}

data class Week(val yearWeek: YearWeek = YearWeek.now()) : TimeUnitInstance<Week> {
  override val unit
    get() = TimeUnit.WEEK

  override val startDate: LocalDate = yearWeek.atDay(DayOfWeek.MONDAY)
  override val endDate: LocalDate = yearWeek.atDay(DayOfWeek.SUNDAY)

  constructor(date: LocalDate) : this(YearWeek.from(date))

  override operator fun plus(amount: Long) = Week(yearWeek.plusWeeks(amount))

  override operator fun compareTo(other: TimeUnitInstance<Week>) =
    yearWeek.compareTo((other as Week).yearWeek)

  override fun toString() = yearWeek.toString()
}

data class Month(val yearMonth: YearMonth = YearMonth.now()) : TimeUnitInstance<Month> {
  override val unit
    get() = TimeUnit.MONTH

  override val startDate: LocalDate = yearMonth.atDay(1)
  override val endDate: LocalDate = yearMonth.atEndOfMonth()

  constructor(date: LocalDate) : this(YearMonth.from(date))

  override operator fun plus(amount: Long) = Month(yearMonth.plusMonths(amount))

  override operator fun compareTo(other: TimeUnitInstance<Month>) =
    yearMonth.compareTo((other as Month).yearMonth)

  override fun toString() = yearMonth.toString()
}

data class Timeline(val id: Int, val timeBlockUnit: TimeUnit) {
  val now
    get() = timeBlockUnit.instanceFrom(LocalDate.now())

  operator fun compareTo(other: Timeline) =
    timeBlockUnit.referenceSize.compareTo(other.timeBlockUnit.referenceSize)
}
