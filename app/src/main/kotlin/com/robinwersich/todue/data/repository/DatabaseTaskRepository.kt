package com.robinwersich.todue.data.repository

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.robinwersich.todue.data.database.TaskDao
import com.robinwersich.todue.data.entity.toEntity
import com.robinwersich.todue.data.entity.toModel
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.utility.mapToImmutableList

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())

  override suspend fun deleteTask(id: Long) = taskDao.delete(id)

  override suspend fun setTimeBlock(id: Long, timeBlock: TimeBlock) =
    taskDao.setScheduledRange(
      id,
      startDate = timeBlock.start,
      endDateInclusive = timeBlock.endInclusive,
    )

  override suspend fun setDueDate(id: Long, date: LocalDate) = taskDao.setDueDate(id, date)

  override suspend fun setText(id: Long, text: String) = taskDao.setText(id, text)

  override suspend fun setDoneDate(id: Long, date: LocalDate?) = taskDao.setDoneDate(id, date)

  override fun getTaskBlockFlow(timelineBlock: TimelineBlock): Flow<TaskBlock> =
    taskDao
      .getTasksFlow(
        timelineId = timelineBlock.timelineId,
        start = timelineBlock.section.start,
        endInclusive = timelineBlock.section.endInclusive,
      )
      .map { tasks ->
        TaskBlock(timelineBlock, tasks.mapToImmutableList { it.toModel(timelineBlock) })
      }

  override suspend fun getTasks(timelineBlock: TimelineBlock): List<Task> =
    taskDao
      .getTasks(
        timelineId = timelineBlock.timelineId,
        start = timelineBlock.section.start,
        endInclusive = timelineBlock.section.endInclusive,
      )
      .map { it.toModel(timelineBlock) }

  override suspend fun getTaskBlock(timelineBlock: TimelineBlock): TaskBlock =
    TaskBlock(timelineBlock, getTasks(timelineBlock))

  override suspend fun getTaskBlocks(timelineBlocks: Collection<TimelineBlock>): List<TaskBlock> =
    timelineBlocks.map { getTaskBlock(it) }

  override suspend fun getTaskBlocksMap(
    timelineBlocks: Collection<TimelineBlock>
  ): Map<TimelineBlock, TaskBlock> = timelineBlocks.associateWith { getTaskBlock(it) }
}
