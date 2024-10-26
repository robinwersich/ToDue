package com.robinwersich.todue.ui.presentation.organizer.state

import com.robinwersich.todue.domain.model.DateRange
import com.robinwersich.todue.domain.model.Timeline
import java.time.LocalDate

/** Describes which timeline(s) should currently be visible. */
data class TimelineNavigationPosition(
  /** The visible "main" timeline of this position. */
  val timeline: Timeline,
  /** The adjacent timeline, with a bigger time unit. */
  val parent: Timeline?,
  /** The adjacent timeline, with a smaller time unit. */
  val child: Timeline?,
  /** Whether the child timeline should be visible. */
  val showChild: Boolean,
) {
  init {
    require(!showChild || child != null) {
      "Child timeline must not be null if it should be visible."
    }
  }

  /**
   * Creates a new [TimelineNavigationPosition] based on a list of [Timeline]s.
   *
   * @param sortedTimelines A list of [Timeline]s, sorted by their time unit size.
   * @param index The index of the main timeline of this position in the [sortedTimelines] list.
   * @param showChild Whether the child timeline should be visible.
   */
  constructor(
    sortedTimelines: List<Timeline>,
    index: Int,
    showChild: Boolean = false,
  ) : this(
    timeline = sortedTimelines[index],
    parent = sortedTimelines.elementAtOrNull(index + 1),
    child = sortedTimelines.elementAtOrNull(index - 1),
    showChild = showChild,
  )

  /**
   * Creates a new [TimelineNavigationPosition] based on a list of [Timeline]s and a focussed
   * timeline.
   *
   * @param sortedTimelines A list of [Timeline]s, sorted by their time unit size.
   * @param focussedTimeline The main timeline of this position. If it is not in the list, the first
   *   timeline will be used.
   * @param showChild Whether the child timeline should be visible.
   */
  constructor(
    sortedTimelines: List<Timeline>,
    focussedTimeline: Timeline,
    showChild: Boolean = false,
  ) : this(
    sortedTimelines = sortedTimelines,
    index = sortedTimelines.indexOf(focussedTimeline).coerceAtLeast(0),
    showChild = showChild,
  )

  /** The child timeline, if visible, otherwise null. */
  val visibleChild: Timeline?
    get() = if (showChild) child else null

  /** The visible timelines, starting with the child timeline (if visible). */
  val visibleTimelines: Sequence<Timeline>
    get() = sequence {
      if (showChild && child != null) yield(child)
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
) {
  override fun toString() = "NavigationPosition(timeline=$timelineNavPos, date=$date)"
}
