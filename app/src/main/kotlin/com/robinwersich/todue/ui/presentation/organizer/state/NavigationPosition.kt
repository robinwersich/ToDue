package com.robinwersich.todue.ui.presentation.organizer.state

import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Timeline
import java.time.LocalDate

/** Describes which timeline(s) should currently be visible. */
data class TimelineNavigationPosition(
  /** The visible "main" timeline of this position. */
  val timeline: Timeline,
  /** The adjacent timeline with a smaller time unit, if it should be shown. */
  val child: Timeline? = null,
) {
  val showChild: Boolean
    get() = child != null

  /** The visible timelines, starting with the child timeline (if visible). */
  val visibleTimelines: Sequence<Timeline>
    get() = sequence {
      if (child != null) yield(child)
      yield(timeline)
    }

  override fun toString() = visibleTimelines.joinToString(prefix = "[", postfix = "]")
}

/** Describes the current navigation position in the organizer. */
data class NavigationPosition(
  /** Which timeline(s) should be visible. */
  val timelineNavPos: TimelineNavigationPosition,
  /** The date (and resulting time block) that should be visible. */
  val date: LocalDate,
  /** The date range that will be visible in this navigation position. */
  val dateRange: DateRange,
  /** The time block that is focussed in this navigation position. */
  val timeBlock: TimeBlock,
) {

  override fun toString() = "NavigationPosition(timeline=$timelineNavPos, date=$date)"
}
