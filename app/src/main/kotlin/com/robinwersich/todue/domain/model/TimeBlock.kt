package com.robinwersich.todue.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import org.threeten.extra.YearWeek

sealed interface TimeBlock {
  abstract val startDate: LocalDate
  abstract val endDate: LocalDate

  data class Day(public val date: LocalDate = LocalDate.now()) : TimeBlock {
    override val startDate: LocalDate
      get() = date
    override val endDate: LocalDate
      get() = date
  }

  data class Week(public val yearWeek: YearWeek = YearWeek.now()) : TimeBlock {
    private val firstDayOfWeek: DayOfWeek
      get() = WeekFields.of(Locale.getDefault()).firstDayOfWeek

    constructor(date: LocalDate) : this(YearWeek.from(date))
    override val startDate: LocalDate
      get() = yearWeek.atDay(firstDayOfWeek)
    override val endDate: LocalDate
      get() = yearWeek.atDay(firstDayOfWeek - 1)
  }

  data class Month(public val yearMonth: YearMonth = YearMonth.now()) : TimeBlock {
    constructor(date: LocalDate) : this(YearMonth.from(date))
    override val startDate: LocalDate
      get() = yearMonth.atDay(1)
    override val endDate: LocalDate
      get() = yearMonth.atEndOfMonth()
  }
}
