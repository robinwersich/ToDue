package com.robinwersich.todue.ui.presentation.utility

import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeUnitInstance
import com.robinwersich.todue.domain.model.Week

val TimeUnitInstance<*>.displayName: String
  get() =
    when (this) {
      is Day -> date.toString()
      is Week -> "$startDate - $endDate"
      is Month -> yearMonth.toString()
    }
