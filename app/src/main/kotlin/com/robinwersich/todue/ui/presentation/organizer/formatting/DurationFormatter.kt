package com.robinwersich.todue.ui.presentation.organizer.formatting

import java.time.Duration

fun formatDuration(duration: Duration): String {
  val totalMinutes = duration.toMinutes()
  val hours = totalMinutes / 60
  val minutes = totalMinutes % 60
  return when {
    hours == 0L -> "${minutes}m"
    minutes == 0L -> "${hours}h"
    else -> "${hours}h ${minutes}m"
  }
}
