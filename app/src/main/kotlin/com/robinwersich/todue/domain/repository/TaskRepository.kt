package com.robinwersich.todue.domain.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock

interface TaskRepository {
  suspend fun insertTask(task: Task): Long

  suspend fun deleteTask(id: Long)

  suspend fun updateTask(task: Task)

  suspend fun setText(id: Long, text: String)

  suspend fun setTimeBlock(id: Long, timeBlock: TimeBlock)

  suspend fun setDueDate(id: Long, date: LocalDate)

  suspend fun setDoneDate(id: Long, date: LocalDate?)

  fun getTaskBlockMainDataFlows(
    timelineBlocks: Collection<TimelineBlock>
  ): Flow<Map<TimelineBlock, TaskBlock>>

  fun getTaskBlockPreviewDataFlows(
    timelineBlocks: Collection<TimelineBlock>
  ): Flow<Map<TimelineBlock, TaskBlock>>
}
