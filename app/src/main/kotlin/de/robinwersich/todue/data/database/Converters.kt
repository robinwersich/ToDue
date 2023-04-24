package de.robinwersich.todue.data.database

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
  @TypeConverter
  fun timestampToLocalDate(timestamp: Long?) = timestamp?.let { LocalDate.ofEpochDay(it) }

  @TypeConverter fun localDateToTimestamp(localDate: LocalDate?) = localDate?.toEpochDay()
}
