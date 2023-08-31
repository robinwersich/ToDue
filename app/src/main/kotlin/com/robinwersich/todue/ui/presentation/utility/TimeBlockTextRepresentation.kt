package com.robinwersich.todue.ui.presentation.utility

import com.robinwersich.todue.domain.model.TimeBlock

val TimeBlock.name: String
  get() =
    when (this) {
      is TimeBlock.Day -> date.toString()
      is TimeBlock.Week -> yearWeek.toString()
      is TimeBlock.Month -> yearMonth.toString()
    }
