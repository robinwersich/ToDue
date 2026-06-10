package com.robinwersich.todue.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Converters {
  @TypeConverter
  fun timestampToLocalDate(timestamp: Long?) = timestamp?.let { LocalDate.ofEpochDay(it) }

  @TypeConverter fun localDateToTimestamp(localDate: LocalDate?) = localDate?.toEpochDay()

  @TypeConverter fun secondsToDuration(seconds: Long?) = seconds?.seconds

  // Millisecond precision is not required in this domain, so we store only seconds.
  @TypeConverter fun durationToSeconds(duration: Duration?) = duration?.inWholeSeconds
}
