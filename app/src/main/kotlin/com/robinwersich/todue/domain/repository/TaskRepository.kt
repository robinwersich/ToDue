package com.robinwersich.todue.domain.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.TimelineSection

interface TaskRepository {
  suspend fun insertTask(task: Task): Long

  suspend fun deleteTask(id: Long)

  suspend fun setText(id: Long, text: String)

  suspend fun setTimeBlock(id: Long, timeBlock: TimeBlock)

  suspend fun setDueDate(id: Long, date: LocalDate)

  suspend fun setDoneDate(id: Long, date: LocalDate?)

  fun getTaskBlockFlow(timelineBlock: TimelineBlock): Flow<TaskBlock>

  suspend fun getTasks(timelineSection: TimelineSection<*>): List<Task>

  suspend fun getTaskBlock(timelineBlock: TimelineBlock): TaskBlock

  suspend fun getTaskBlocks(timelineBlocks: Collection<TimelineBlock>): List<TaskBlock>

  suspend fun getTaskBlocksMap(
    timelineBlocks: Collection<TimelineBlock>
  ): Map<TimelineBlock, TaskBlock>
}
