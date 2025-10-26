package com.robinwersich.todue.data.repository

import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.robinwersich.todue.data.database.TaskDao
import com.robinwersich.todue.data.entity.TaskEntity
import com.robinwersich.todue.data.entity.toEntity
import com.robinwersich.todue.data.entity.toModel
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.TimelineSection
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
      .map { TaskBlock(timelineBlock, it.mapToImmutableList(TaskEntity::toModel)) }

  override suspend fun getTasks(timelineSection: TimelineSection<*>): ImmutableList<Task> =
    taskDao
      .getTasks(
        timelineId = timelineSection.timelineId,
        start = timelineSection.section.start,
        endInclusive = timelineSection.section.endInclusive,
      )
      .mapToImmutableList(TaskEntity::toModel)
}
