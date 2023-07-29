package com.robinwersich.todue.ui.components

sealed class TaskEvent {
  object Add : TaskEvent()
  data class Remove(val id: Long) : TaskEvent()
  data class Expand(val id: Long) : TaskEvent()
  object Collapse : TaskEvent()
  data class SetText(val id: Long, val text: String) : TaskEvent()
  data class SetDone(val id: Long, val done: Boolean) : TaskEvent()
}
