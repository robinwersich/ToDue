package com.robinwersich.todue.domain.model

data class TaskBlock(
  val timeBlock: TimeBlock,
  val tasks: List<Task>,
)
