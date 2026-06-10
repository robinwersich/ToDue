package com.robinwersich.todue.data.repository

import com.robinwersich.todue.data.database.TaskDao
import com.robinwersich.todue.data.entity.ScheduledWork
import com.robinwersich.todue.data.entity.toEntity
import com.robinwersich.todue.data.entity.toModel
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.duration
import com.robinwersich.todue.domain.repository.TaskRepository
import com.robinwersich.todue.utility.intersection
import com.robinwersich.todue.utility.mapToImmutableList
import com.robinwersich.todue.utility.overlapsWith
import com.robinwersich.todue.utility.sumOf
import java.time.LocalDate
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DatabaseTaskRepository(private val taskDao: TaskDao) : TaskRepository {
  override suspend fun insertTask(task: Task) = taskDao.insert(task.toEntity())

  override suspend fun deleteTask(id: Long) = taskDao.delete(id)

  override suspend fun updateTask(task: Task) = taskDao.update(task.toEntity())

  override suspend fun setTimeBlock(id: Long, timeBlock: TimeBlock) =
    taskDao.setScheduledRange(
      id,
      startDate = timeBlock.start,
      endDateInclusive = timeBlock.endInclusive,
    )

  override suspend fun setDueDate(id: Long, date: LocalDate) = taskDao.setDueDate(id, date)

  override suspend fun setText(id: Long, text: String) = taskDao.setText(id, text)

  override suspend fun setDoneDate(id: Long, date: LocalDate?) = taskDao.setDoneDate(id, date)

  override fun getTaskBlockMainDataFlows(
    timelineBlocks: Collection<TimelineBlock>
  ): Flow<Map<TimelineBlock, TaskBlock>> {
    val taskBlockFlows = timelineBlocks.map { timelineBlock ->
      taskDao
        .getTasksFlow(
          timelineId = timelineBlock.timelineId,
          start = timelineBlock.section.start,
          endInclusive = timelineBlock.section.endInclusive,
        )
        .map { tasks ->
          TaskBlock.main(
            timelineBlock = timelineBlock,
            tasks = tasks.mapToImmutableList { it.toModel(timelineBlock) },
          )
        }
    }
    if (taskBlockFlows.isEmpty()) return flowOf(emptyMap())
    return combine(taskBlockFlows) { taskBlocks -> taskBlocks.associateBy { it.timelineBlock } }
  }

  override fun getTaskBlockPreviewDataFlows(
    timelineBlocks: Collection<TimelineBlock>
  ): Flow<Map<TimelineBlock, TaskBlock>> {
    val timelineId = timelineBlocks.maxOfOrNull { it.timelineId }
    if (timelineId == null) return flowOf(emptyMap())
    return taskDao
      .getScheduledWork(
        timelineId = timelineId,
        start = timelineBlocks.minOf { it.section.start },
        endInclusive = timelineBlocks.maxOf { it.section.endInclusive },
      )
      .map { scheduledWork ->
        timelineBlocks.associateWith { timelineBlock ->
          TaskBlock.preview(
            timelineBlock = timelineBlock,
            totalEstimatedDuration = scheduledWork.sumOf { it.portionIn(timelineBlock) },
          )
        }
      }
  }
}

private fun ScheduledWork.portionIn(timelineBlock: TimelineBlock): Duration {
  if (!scheduledTimelineSection.overlapsWith(timelineBlock.section)) return Duration.ZERO
  val overlapWithBlock = (scheduledTimelineSection intersection timelineBlock.section).duration
  return estimatedDuration * (overlapWithBlock / scheduledTimelineSection.duration)
}
