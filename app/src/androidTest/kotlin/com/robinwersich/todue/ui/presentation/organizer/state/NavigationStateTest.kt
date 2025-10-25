package com.robinwersich.todue.ui.presentation.organizer.state

import androidx.compose.foundation.gestures.snapTo
import androidx.compose.ui.unit.IntSize
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Month
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.Week

private val defaultTimelines =
  persistentListOf(
    Timeline(0, TimeUnit.DAY),
    Timeline(1, TimeUnit.WEEK),
    Timeline(2, TimeUnit.MONTH),
  )

class NavigationStateTest {
  private fun navigationState(
    timelines: Collection<Timeline> = defaultTimelines,
    initialTimeline: Timeline = defaultTimelines.first(),
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
      navigationState(initialTimeline = defaultTimelines[1], initialDate = initialWeek.start)
    assertThat(state.activeTimelineBlocks).containsExactly(TimelineBlock(1, initialWeek))
  }

  @Test
  fun whenScrollingTimelines_activeTimelineBlocksAreCorrect() {
    val initialWeek = Week()
    val state =
      navigationState(initialTimeline = defaultTimelines[1], initialDate = initialWeek.start)
    runBlocking { with(state.timelineDraggableState) { anchoredDrag { dragTo(offset - 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactlyElementsIn(
        buildList {
          initialWeek.days.forEach { add(TimelineBlock(0, Day(it))) }
          add(TimelineBlock(1, initialWeek))
        }
      )
  }

  @Test
  fun whenScrollingDates_activeTimelineBlocksAreCorrect() {
    val initialWeek = Week()
    val state =
      navigationState(initialTimeline = defaultTimelines[1], initialDate = initialWeek.start)
    runBlocking { with(state.dateDraggableState) { anchoredDrag { dragTo(offset + 10f) } } }
    assertThat(state.activeTimelineBlocks)
      .containsExactly(TimelineBlock(1, initialWeek), TimelineBlock(1, initialWeek + 1))
  }

  @Test
  fun givenAdditionalMargin_additionalTimelineBlocksAreShown() {
    val state =
      navigationState(initialDate = LocalDate.of(2020, 1, 15)).also {
        it.updateViewportSize(IntSize(100, 100), 0.1f, 0.2f)
      }
    runBlocking {
      state.timelineDraggableState.snapTo(
        TimelineNavPosition(timeline = defaultTimelines[2], child = defaultTimelines[1])
      )
    }
    assertThat(state.activeTimelineBlocks)
      .containsExactlyElementsIn(
        buildList {
          (Week(2019, 52)..Week(2020, 6)).forEach { add(TimelineBlock(1, it)) }
          (Month(2019, 12)..Month(2020, 2)).forEach { add(TimelineBlock(2, it)) }
        }
      )
  }

  @Test
  fun whenSettingNewTimelinesIncludingCurrentlyShown_activeTimelineBlocksDontChange() {
    val state = navigationState()
    runBlocking {
      state.timelineDraggableState.snapTo(
        TimelineNavPosition(timeline = defaultTimelines[2], child = defaultTimelines[1])
      )
    }
    val activeTimelineBlocksBefore = state.activeTimelineBlocks
    state.setTimelines(listOf(defaultTimelines[1], defaultTimelines[2]))
    val activeTimelineBlocksAfter = state.activeTimelineBlocks
    assertThat(activeTimelineBlocksAfter).isEqualTo(activeTimelineBlocksBefore)
  }

  @Test
  fun whenSettingNewTimelinesNotIncludingCurrentlyShown_activeTimelineBlocksSnapToAdapt() {
    val initialDate = LocalDate.of(2020, 1, 1)
    val state = navigationState(initialTimeline = defaultTimelines[1], initialDate = initialDate)
    state.setTimelines(listOf(defaultTimelines[0]))
    assertThat(state.activeTimelineBlocks).containsExactly(TimelineBlock(0, Day(initialDate)))
  }
}
