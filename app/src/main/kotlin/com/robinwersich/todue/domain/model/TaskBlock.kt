package com.robinwersich.todue.domain.model

data class TaskBlock(
    val timeBlock: TimeUnitInstance<*>,
    val tasks: List<Task>,
)
