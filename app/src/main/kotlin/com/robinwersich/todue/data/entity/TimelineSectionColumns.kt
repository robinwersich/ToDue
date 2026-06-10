package com.robinwersich.todue.data.entity

import androidx.room.ColumnInfo
import java.time.LocalDate
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.TimelineSection

data class TimelineSectionColumns(
  @ColumnInfo("timeline_id") val timelineId: Long,
  @ColumnInfo("start") override val start: LocalDate,
  @ColumnInfo("end_inclusive") override val endInclusive: LocalDate,
) : ClosedRange<LocalDate>

fun TimelineSectionColumns.toModel() = TimelineBlock(timelineId, Day(start)..Day(endInclusive))

fun TimelineSection<*>.toEntity() =
  TimelineSectionColumns(timelineId, section.start, section.endInclusive)
