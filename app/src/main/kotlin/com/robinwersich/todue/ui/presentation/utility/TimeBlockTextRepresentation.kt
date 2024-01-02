package com.robinwersich.todue.ui.presentation.utility

import com.robinwersich.todue.domain.model.TimeUnitInstance

val TimeUnitInstance.displayName: String
  get() =
    when (this) {
      is TimeUnitInstance.Day -> date.toString()
      is TimeUnitInstance.Week -> "$startDate - $endDate"
      is TimeUnitInstance.Month -> yearMonth.toString()
    }
