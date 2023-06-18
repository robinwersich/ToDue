package de.robinwersich.todue.ui.components

sealed class TaskEvent {
  object Add : TaskEvent()
  data class Remove(val id: Int) : TaskEvent()
  data class Expand(val id: Int) : TaskEvent()
  object Collapse : TaskEvent()
  data class SetText(val id: Int, val text: String) : TaskEvent()
  data class SetDone(val id: Int, val done: Boolean) : TaskEvent()
}
