package com.robinwersich.todue.domain.model

data class TimelineSection<out TSection : DateRange>(
    val timeline: Timeline,
    val section: TSection,
)

typealias TimelineRange = TimelineSection<DateRange>
typealias TimelineBlock = TimelineSection<TimeBlock>
