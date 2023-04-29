package de.robinwersich.todue.ui.components

import java.time.LocalDate

data class TaskUiData(
  val id: Int,
  val text: String,
  val dueDate: LocalDate? = null,
  val done: Boolean = false,
)
