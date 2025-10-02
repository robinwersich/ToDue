package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.ui.unit.IntSize
import com.google.common.truth.Truth.assertThat
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.Week
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalFoundationApi::class)
class NavigationStateTest {
  private val timelines =
    listOf(Timeline(0, TimeUnit.DAY), Timeline(1, TimeUnit.WEEK), Timeline(2, TimeUnit.MONTH))
  private val initialTimeline = Timeline(1, TimeUnit.WEEK)
  private val initialDate = LocalDate.of(2020, 1, 15)
  private lateinit var state: NavigationState

  @Before
  fun setUp() {
    state =
      NavigationState(
        timelines = timelines,
        childTimelineSizeRatio = 0.3f,
        positionalThreshold = { 0f },
        velocityThreshold = { 0f },
        snapAnimationSpec = tween(),
        decayAnimationSpec = exponentialDecay(),
        initialTimeline = initialTimeline,
        initialDate = initialDate,
      )
    state.updateViewportSize(IntSize(100, 100))
  }

  @Test
  fun activeTimelineBlocks_AreCorrect_Initially() {
    assertThat(state.activeTimelineBlocks)
      .containsExactly(initialTimeline to persistentListOf(Week(initialDate)))
  }

  @Test
  fun activeTimelineBlocks_AreCorrect_DuringTimelineScroll() {
    runBlocking { with(state.timelineDraggableState) { anchoredDrag { dragTo(offset - 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(
        Timeline(0, TimeUnit.DAY) to Week(initialDate).days.map(::Day).toImmutableList(),
        Timeline(1, TimeUnit.WEEK) to persistentListOf(Week(initialDate)),
      )
  }

  @Test
  fun activeTimelineBlocks_AreCorrect_DuringDateScroll() {
    runBlocking { with(state.dateDraggableState) { anchoredDrag { dragTo(offset + 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(
        Timeline(1, TimeUnit.WEEK) to persistentListOf(Week(initialDate), Week(initialDate) + 1)
      )
  }

  @Test
  fun activeTimelineBlocks_RespectAdditionalMargin() {
    state.updateViewportSize(IntSize(100, 100), 0.1f, 0.2f)
    runBlocking {
      state.timelineDraggableState.snapTo(
        TimelineNavPosition(
          timeline = Timeline(2, TimeUnit.MONTH),
          child = Timeline(1, TimeUnit.WEEK),
        )
      )
    }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(
        Timeline(1, TimeUnit.WEEK) to (Week(2019, 52)..Week(2020, 6)).toImmutableList(),
        Timeline(2, TimeUnit.MONTH) to (Month(2019, 12)..Month(2020, 2)).toImmutableList(),
      )
  }
}
