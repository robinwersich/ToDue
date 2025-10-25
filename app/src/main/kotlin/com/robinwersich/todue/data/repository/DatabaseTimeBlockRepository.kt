package com.robinwersich.todue.data.repository

import com.robinwersich.todue.data.database.TimelineDao
import com.robinwersich.todue.data.entity.TimelineEntity
import com.robinwersich.todue.data.entity.toModel
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.repository.TimeBlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatabaseTimeBlockRepository(private val timelineDao: TimelineDao) : TimeBlockRepository {
  override fun getTimelines(): Flow<List<Timeline>> =
    timelineDao.getTimelines().map { it.map(TimelineEntity::toModel) }
}
