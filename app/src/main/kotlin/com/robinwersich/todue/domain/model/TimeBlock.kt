package com.robinwersich.todue.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.threeten.extra.YearWeek

/** A time range with a name */
interface TimeBlock : ClosedRange<LocalDate> {
  /** The human-readable name of this block. */
  val displayName: String
}

/**
 * Represents a time amount unit, such as a day or week. Units can be compared based on their size.
 */
enum class TimeUnit(
  val referenceSize: Float,
  private val instanceConstructor: (LocalDate) -> TimeUnitInstance,
) {
  DAY(1f, ::Day),
  WEEK(7f, ::Week),
  MONTH(30.5f, ::Month);

  fun instanceFrom(date: LocalDate) = instanceConstructor(date)
}

/**
 * A time unit instance is a specific instance of a [TimeUnit]. For example, a time unit instance of
 * the time unit [week][TimeUnit.WEEK] is the week 2021-W02. All time unit instances can either be
 * created from a corresponding [Temporal][java.time.temporal.Temporal] or from a [LocalDate], which
 * results in the time unit instance that *contains* this date.
 *
 * @param T The type of the time unit instance. This is used to make sure that only instances of the
 *   same time unit can be compared.
 */
sealed interface TimeUnitInstance : TimeBlock, Comparable<TimeUnitInstance> {

  /** The [TimeUnit] enum entry of this instance. */
  val unit: TimeUnit

  /** Returns a new instance that is [amount] time units after this instance. */
  operator fun plus(amount: Long): TimeUnitInstance

  /** Returns a new instance that is [amount] time units before this instance. */
  operator fun minus(amount: Long) = this + -amount

  /** Returns the next [TimeUnitInstance]. */
  operator fun inc() = this + 1

  /** Returns the previous [TimeUnitInstance]. */
  operator fun dec() = this - 1

  operator fun rangeTo(other: TimeUnitInstance) = TimeUnitInstanceRange(this, other)
}

data class Day(val date: LocalDate = LocalDate.now()) : TimeUnitInstance {
  override val unit
    get() = TimeUnit.DAY

  override val start: LocalDate = date
  override val endInclusive: LocalDate = date
  override val displayName: String = date.toString()

  override operator fun plus(amount: Long) = Day(date.plusDays(amount))

  override operator fun compareTo(other: TimeUnitInstance): Int {
    require(other is Day) { "Cannot compare different time units." }
    return date.compareTo(other.date)
  }

  override fun toString() = date.toString()
}

data class Week(val yearWeek: YearWeek = YearWeek.now()) : TimeUnitInstance {
  override val unit
    get() = TimeUnit.WEEK

  override val start: LocalDate = yearWeek.atDay(DayOfWeek.MONDAY)
  override val endInclusive: LocalDate = yearWeek.atDay(DayOfWeek.SUNDAY)
  override val displayName: String = "$start - $endInclusive"

  constructor(date: LocalDate) : this(YearWeek.from(date))

  override operator fun plus(amount: Long) = Week(yearWeek.plusWeeks(amount))

  override operator fun compareTo(other: TimeUnitInstance): Int {
    require(other is Week) { "Cannot compare different time units." }
    return yearWeek.compareTo(other.yearWeek)
  }

  override fun toString() = yearWeek.toString()
}

data class Month(val yearMonth: YearMonth = YearMonth.now()) : TimeUnitInstance {
  override val unit
    get() = TimeUnit.MONTH

  override val start: LocalDate = yearMonth.atDay(1)
  override val endInclusive: LocalDate = yearMonth.atEndOfMonth()
  override val displayName: String = yearMonth.toString()

  constructor(date: LocalDate) : this(YearMonth.from(date))

  override operator fun plus(amount: Long) = Month(yearMonth.plusMonths(amount))

  override operator fun compareTo(other: TimeUnitInstance): Int {
    require(other is Month) { "Cannot compare different time units." }
    return yearMonth.compareTo(other.yearMonth)
  }

  override fun toString() = yearMonth.toString()
}

class TimeUnitInstanceRange(
  override val start: TimeUnitInstance,
  override val endInclusive: TimeUnitInstance,
) : ClosedRange<TimeUnitInstance>, Sequence<TimeUnitInstance>, Iterable<TimeUnitInstance> {
  init {
    require(start.unit == endInclusive.unit) { "Cannot create range from different time units." }
  }

  override fun iterator() =
    object : Iterator<TimeUnitInstance> {
      private var current = start

      override fun hasNext() = current <= endInclusive

      override fun next() = current++
    }
}
