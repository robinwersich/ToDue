package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import com.robinwersich.todue.domain.model.TimelineRange
import com.robinwersich.todue.domain.model.TimelineSection
import java.time.LocalDate

data class TimelineSectionColumns(
  @ColumnInfo("timeline_id") val timelineId: Long,
  @ColumnInfo("start") val start: LocalDate,
  @ColumnInfo("end_inclusive") val endInclusive: LocalDate,
)

fun TimelineSectionColumns.toModel() = TimelineRange(timelineId, start..endInclusive)

fun TimelineSection<*>.toEntity() =
  TimelineSectionColumns(timelineId, section.start, section.endInclusive)
