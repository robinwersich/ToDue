package com.robinwersich.todue.domain.repository

import com.robinwersich.todue.domain.model.Timeline
import kotlinx.coroutines.flow.Flow

interface TimeBlockRepository {
  fun getTimelines(): Flow<List<Timeline>>
}
