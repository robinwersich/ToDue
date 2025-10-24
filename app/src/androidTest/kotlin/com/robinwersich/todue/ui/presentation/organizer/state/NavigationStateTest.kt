package com.robinwersich.todue.ui.presentation.organizer.state

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
import org.junit.Test

class NavigationStateTest {
  private fun navigationState(
    timelines: Collection<Timeline> =
      listOf(TimeUnit.DAY, TimeUnit.WEEK, TimeUnit.MONTH).map { Timeline(it) },
    initialTimeline: Timeline = timelines.first(),
    initialDate: LocalDate = LocalDate.now(),
  ) =
    NavigationState(
        timelines = timelines,
        initialTimeline = initialTimeline,
        initialDate = initialDate,
      )
      .also { it.updateViewportSize(IntSize(100, 100)) }

  @Test
  fun initially_activeTimelineBlocksAreCorrect() {
    val initialWeek = Week()
    val state =
      navigationState(initialTimeline = Timeline(TimeUnit.WEEK), initialDate = initialWeek.start)
    assertThat(state.activeTimelineBlocks)
      .containsExactly(Timeline(TimeUnit.WEEK) to persistentListOf(initialWeek))
  }

  @Test
  fun whenScrollingTimelines_activeTimelineBlocksAreCorrect() {
    val initialWeek = Week()
    val state =
      navigationState(initialTimeline = Timeline(TimeUnit.WEEK), initialDate = initialWeek.start)
    runBlocking { with(state.timelineDraggableState) { anchoredDrag { dragTo(offset - 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(
        Timeline(TimeUnit.DAY) to initialWeek.days.map(::Day).toImmutableList(),
        Timeline(TimeUnit.WEEK) to persistentListOf(initialWeek),
      )
  }

  @Test
  fun whenScrollingDates_activeTimelineBlocksAreCorrect() {
    val initialWeek = Week()
    val state =
      navigationState(initialTimeline = Timeline(TimeUnit.WEEK), initialDate = initialWeek.start)
    runBlocking { with(state.dateDraggableState) { anchoredDrag { dragTo(offset + 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(Timeline(TimeUnit.WEEK) to persistentListOf(initialWeek, initialWeek + 1))
  }

  @Test
  fun givenAdditionalMargin_additionalTimelineBlocksAreShown() {
    val state =
      navigationState(initialDate = LocalDate.of(2020, 1, 15)).also {
        it.updateViewportSize(IntSize(100, 100), 0.1f, 0.2f)
      }
    runBlocking {
      state.timelineDraggableState.snapTo(
        TimelineNavPosition(timeline = Timeline(TimeUnit.MONTH), child = Timeline(TimeUnit.WEEK))
      )
    }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(
        Timeline(TimeUnit.WEEK) to (Week(2019, 52)..Week(2020, 6)).toImmutableList(),
        Timeline(TimeUnit.MONTH) to (Month(2019, 12)..Month(2020, 2)).toImmutableList(),
      )
  }

  @Test
  fun whenSettingNewTimelinesIncludingCurrentlyShown_activeTimelineBlocksDontChange() {
    val state = navigationState()
    runBlocking {
      state.timelineDraggableState.snapTo(
        TimelineNavPosition(timeline = Timeline(TimeUnit.MONTH), child = Timeline(TimeUnit.WEEK))
      )
    }
    val activeTimelineBlocksBefore = state.activeTimelineBlocks
    state.setTimelines(listOf(Timeline(TimeUnit.WEEK), Timeline(TimeUnit.MONTH)))
    val activeTimelineBlocksAfter = state.activeTimelineBlocks
    assertThat(activeTimelineBlocksAfter).isEqualTo(activeTimelineBlocksBefore)
  }

  @Test
  fun whenSettingNewTimelinesNotIncludingCurrentlyShown_activeTimelineBlocksSnapToAdapt() {
    val initialDate = LocalDate.of(2020, 1, 1)
    val state =
      navigationState(initialTimeline = Timeline(TimeUnit.WEEK), initialDate = initialDate)
    state.setTimelines(listOf(Timeline(TimeUnit.DAY)))
    assertThat(state.activeTimelineBlocks)
      .containsExactly(Timeline(TimeUnit.DAY) to persistentListOf(Day(initialDate)))
  }
}
