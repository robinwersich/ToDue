package com.robinwersich.todue.ui.presentation.organizer.state

import com.robinwersich.todue.domain.model.Timeline

/** Describes how a [Timeline] is presented in the organizer. */
enum class TimelinePresentation {
  /** The timeline is occupies the full width of the screen. */
  FULLSCREEN,
  /** The timeline is displayed in a split view, as the parent timeline. */
  PARENT,
  /** The timeline is displayed in a split view, as the child timeline. */
  CHILD,
  /** The timeline is not visible, but it was / will become a child next. */
  HIDDEN_CHILD,
  /** The timeline is not visible, but was / will become a parent next. */
  HIDDEN_PARENT,
}
