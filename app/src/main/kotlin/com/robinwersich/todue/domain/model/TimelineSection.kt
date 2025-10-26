package com.robinwersich.todue.domain.model

/** Defines a time range in a certain timeline, e.g. to determine when a [Task] is scheduled. */
data class TimelineSection<out TSection : DateRange>(val timelineId: Long, val section: TSection)

typealias TimelineRange = TimelineSection<DateRange>

typealias TimelineBlock = TimelineSection<TimeBlock>
