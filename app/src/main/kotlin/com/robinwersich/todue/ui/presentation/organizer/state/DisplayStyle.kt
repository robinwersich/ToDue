package com.robinwersich.todue.ui.presentation.organizer.state

import com.robinwersich.todue.domain.model.Timeline

/** Describes the semantic position of a timeline in the organizer. */
enum class TimelineStyle {
  /** The timeline is not visible, but was / will become a parent next. */
  HIDDEN_PARENT,
  /** The timeline is displayed in a split view, as the parent timeline. */
  PARENT,
  /** The timeline is occupies the full width of the screen. */
  FULLSCREEN,
  /** The timeline is displayed in a split view, as the child timeline. */
  CHILD,
  /** The timeline is not visible, but it was / will become a child next. */
  HIDDEN_CHILD,
}

/**
 * Returns the correct [TimelineStyle] of a [Timeline] based on a [TimelineNavPosition].
 *
 * @param timeline The [Timeline] for which the style should be determined.
 * @param navPos The [TimelineNavPosition] based on which the style should be determined.
 */
fun timelineStyle(timeline: Timeline, navPos: TimelineNavPosition) =
  when {
    timeline > navPos.timeline -> TimelineStyle.HIDDEN_PARENT
    timeline == navPos.timeline && navPos.showChild -> TimelineStyle.PARENT
    timeline == navPos.timeline && !navPos.showChild -> TimelineStyle.FULLSCREEN
    timeline == navPos.child -> TimelineStyle.CHILD
    timeline < (navPos.child ?: navPos.timeline) -> TimelineStyle.HIDDEN_CHILD
    else -> error("Unhandled TimelineStyle case. Timeline: $timeline, NavPos: $navPos.")
  }
