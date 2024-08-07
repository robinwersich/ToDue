package com.robinwersich.todue.domain.model

import com.robinwersich.todue.utility.requireSame
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.ranges.rangeTo as nativeRangeTo
import org.threeten.extra.YearWeek

/** A range with a semantic meaning */
interface TimeBlock : DateRange, Comparable<TimeBlock> {
  /** The human-readable name of this block. */
  val displayName: String

  /** The sequence of days contained in this block */
  val days: DateSequence
    get() = start..endInclusive

  /**
   * While comparison mostly makes sense for non-overlapping or exactly overlapping TimeBlocks, the
   * comparison for overlapping TimeBlocks is also defined as follows:
   * - If all dates of this block are before or in the other block, this block is smaller.
   * - If all dates of this block are in or after the other block, this block is larger.
   * - Otherwise, the blocks are considered equal. Note that this is *not consistent with [equals]*!
   */
  override fun compareTo(other: TimeBlock) =
    when {
      this.start < other.start && this.endInclusive < other.endInclusive -> -1
      this.start > other.start && this.endInclusive > other.endInclusive -> 1
      else -> 0
    }
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
 */
sealed interface TimeUnitInstance : TimeBlock {
  /** The [TimeUnit] enum entry of this instance. */
  val unit: TimeUnit

  /** Returns a new instance that is [amount] time units after this instance. */
  operator fun plus(amount: Long): TimeUnitInstance

  /** Returns a new instance that is [amount] time units before this instance. */
  operator fun minus(amount: Long) = this + -amount

  /** Returns the next [TimeUnitInstance]. */
  fun next() = this + 1

  /** Returns the previous [TimeUnitInstance]. */
  fun previous() = this - 1

  /**
   * Creates a sequence of instances from this instance to [other]. Must only be called with
   * instances of the same [TimeUnit].
   */
  operator fun rangeTo(other: TimeUnitInstance): TimeUnitInstanceSequence =
    TimeUnitInstanceSequence(this, other)
}

data class Day(val date: LocalDate = LocalDate.now()) : TimeUnitInstance {
  override val unit
    get() = TimeUnit.DAY

  override val start: LocalDate = date
  override val endInclusive: LocalDate = date
  override val displayName: String = date.toString()

  constructor(year: Int, month: Int, day: Int) : this(LocalDate.of(year, month, day))

  override operator fun plus(amount: Long) = Day(date.plusDays(amount))

  override fun compareTo(other: TimeBlock) =
    when (other) {
      is Day -> date.compareTo(other.date)
      else -> super.compareTo(other)
    }

  override fun toString() = date.toString()
}

data class Week(val yearWeek: YearWeek = YearWeek.now()) : TimeUnitInstance {
  companion object {
    var firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
      set(value) {
        field = value
        lastDayOfWeek = value.minus(1)
      }

    private var lastDayOfWeek: DayOfWeek = firstDayOfWeek.minus(1)
  }

  override val unit
    get() = TimeUnit.WEEK

  override val start: LocalDate = yearWeek.atDay(firstDayOfWeek)
  override val endInclusive: LocalDate = yearWeek.atDay(lastDayOfWeek)
  override val displayName: String = yearWeek.toString()

  constructor(weekBasedYear: Int, week: Int) : this(YearWeek.of(weekBasedYear, week))

  constructor(date: LocalDate) : this(YearWeek.from(date))

  override operator fun plus(amount: Long) = Week(yearWeek.plusWeeks(amount))

  override fun compareTo(other: TimeBlock) =
    when (other) {
      is Week -> yearWeek.compareTo(other.yearWeek)
      else -> super.compareTo(other)
    }

  override fun toString() = yearWeek.toString()
}

data class Month(val yearMonth: YearMonth = YearMonth.now()) : TimeUnitInstance {
  override val unit
    get() = TimeUnit.MONTH

  override val start: LocalDate = yearMonth.atDay(1)
  override val endInclusive: LocalDate = yearMonth.atEndOfMonth()
  override val displayName: String = yearMonth.toString()

  constructor(year: Int, month: Int) : this(YearMonth.of(year, month))

  constructor(date: LocalDate) : this(YearMonth.from(date))

  override operator fun plus(amount: Long) = Month(yearMonth.plusMonths(amount))

  override fun compareTo(other: TimeBlock) =
    when (other) {
      is Month -> yearMonth.compareTo(other.yearMonth)
      else -> super.compareTo(other)
    }

  override fun toString() = yearMonth.toString()
}

/**
 * A sequence of [TimeUnitInstances][TimeUnitInstance] that are all of the same [TimeUnit]. While
 * representing a sequence of TimeBlocks, this class is also a [TimeBlock] itself.
 */
class TimeUnitInstanceSequence
private constructor(private val range: ClosedRange<TimeUnitInstance>, val unit: TimeUnit) :
  Sequence<TimeUnitInstance>, TimeBlock {
  constructor(
    start: TimeUnitInstance,
    endInclusive: TimeUnitInstance,
  ) : this(
    start.nativeRangeTo(endInclusive),
    requireSame(start.unit, endInclusive.unit) {
      "TimeUnitInstanceSequence must be created with instances of the same TimeUnit, " +
        "but got ${start.unit} and ${endInclusive.unit}"
    },
  )

  override val displayName: String =
    "${range.start.displayName} - ${range.endInclusive.displayName}"

  /** The first [date][LocalDate] of this [TimeBlock]. */
  override val start: LocalDate
    get() = range.start.start

  /** The last [date][LocalDate] of this [TimeBlock]. */
  override val endInclusive: LocalDate
    get() = range.endInclusive.endInclusive

  /** The first [TimeUnitInstance] of this range. */
  val startBlock: TimeUnitInstance
    get() = range.start

  /** The last [TimeUnitInstance] of this range. */
  val endBlock: TimeUnitInstance
    get() = range.endInclusive

  /**
   * Returns this as a [ClosedRange] of [TimeUnitInstances][TimeUnitInstance]. Because this class is
   * also a [TimeBlock] itself and thus a [DateRange], it cannot implement this interface directly.
   */
  fun asTimeBlockRange() = range

  override fun iterator(): Iterator<TimeUnitInstance> =
    object : Iterator<TimeUnitInstance> {
      private var next: TimeUnitInstance? = if (startBlock <= endBlock) startBlock else null

      override fun hasNext() = next != null

      override fun next(): TimeUnitInstance {
        next?.let {
          next = if (it >= endBlock) null else it.next()
          return it
        }
        throw NoSuchElementException()
      }
    }
}
