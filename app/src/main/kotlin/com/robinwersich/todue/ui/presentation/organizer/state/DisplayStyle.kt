package com.robinwersich.todue.ui.presentation.organizer.state

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

/** Describes how a time block should be displayed in the organizer. */
enum class TimeBlockStyle {
  /** Reduced content is shown, block is displayed as a tray for sorting tasks to child blocks. */
  TRAY,
  /** The content of the block is shown in full detail. */
  FULLSCREEN,
  /** Only the name of the block is shown. */
  PREVIEW,
}
