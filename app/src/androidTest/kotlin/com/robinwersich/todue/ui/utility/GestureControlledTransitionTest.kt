package com.robinwersich.todue.ui.utility

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

enum class TestAnchor(val value: Float) {
  START(0f),
  MIDDLE(1f),
  END(2f),
}

@OptIn(ExperimentalFoundationApi::class)
class GestureControlledTransitionTest {
  private lateinit var state: AnchoredDraggableState<TestAnchor>
  private val anchors = MyDraggableAnchors {
    TestAnchor.START at 0f
    TestAnchor.MIDDLE at 50f
    TestAnchor.END at 100f
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Before
  fun setUp() {
    state =
      AnchoredDraggableState(
        initialValue = TestAnchor.MIDDLE,
        positionalThreshold = { 0f },
        velocityThreshold = { 0f },
        snapAnimationSpec = tween(),
        decayAnimationSpec = exponentialDecay(),
      )
  }

  @Test
  fun interpolateFloat_ReturnsInitialAnchorValue_ForUninitializedAnchors() {
    assertThat(state.interpolateFloat(TestAnchor::value)).isEqualTo(1f)
  }

  @Test
  fun interpolateFloat_ReturnsSettledValue_ForInitializeState() {
    state.updateAnchors(anchors)
    assertThat(state.interpolateFloat(TestAnchor::value)).isEqualTo(1f)
  }

  @Test
  fun interpolateFloat_ReturnsInterpolatedValue_ForInitializedUnsettledState() {
    state.updateAnchors(anchors)
    state.dispatchRawDelta(25f)
    assertThat(state.interpolateFloat(TestAnchor::value)).isEqualTo(1.5f)
  }

  @Test
  fun getAdjacentToOffsetAnchors_ReturnsInitialAnchor_ForUninitializedAnchors() {
    assertThat(state.getAdjacentToOffsetAnchors()).isEqualTo(TestAnchor.MIDDLE to TestAnchor.MIDDLE)
  }

  @Test
  fun getAdjacentToOffsetAnchors_ReturnsSettledAnchor_ForInitializedAnchors() {
    state.updateAnchors(anchors)
    assertThat(state.getAdjacentToOffsetAnchors()).isEqualTo(TestAnchor.MIDDLE to TestAnchor.MIDDLE)
  }

  @Test
  fun getAdjacentToOffsetAnchors_ReturnsAdjacentAnchors_ForUnsettledState() {
    state.updateAnchors(anchors)
    state.dispatchRawDelta(25f)
    assertThat(state.getAdjacentToOffsetAnchors()).isEqualTo(TestAnchor.MIDDLE to TestAnchor.END)
  }

  @Test
  fun getAdjacentToCurrentAnchors_ReturnsInitialAnchor_ForUninitializedAnchors() {
    assertThat(state.getAdjacentToCurrentAnchors())
      .isEqualTo(TestAnchor.MIDDLE to TestAnchor.MIDDLE)
  }

  @Test
  fun getAdjacentToCurrentAnchors_ReturnsAdjacentAnchors_ForInitializedAnchors() {
    state.updateAnchors(anchors)
    assertThat(state.getAdjacentToCurrentAnchors()).isEqualTo(TestAnchor.START to TestAnchor.END)
  }

  @Test
  fun getAdjacentToCurrentAnchors_ReturnsAdjacentAnchors_ForUnsettledState() {
    state.updateAnchors(anchors)
    runBlocking { state.anchoredDrag { dragTo(70f) } }
    assertThat(state.getAdjacentToCurrentAnchors()).isEqualTo(TestAnchor.START to TestAnchor.END)
    runBlocking { state.anchoredDrag { dragTo(80f) } }
    assertThat(state.getAdjacentToCurrentAnchors()).isEqualTo(TestAnchor.MIDDLE to TestAnchor.END)
  }

  @Test
  fun previousAnchor_OfMiddle_ReturnsStart() {
    assertThat(anchors.previousAnchor(TestAnchor.MIDDLE)).isEqualTo(TestAnchor.START)
  }

  @Test
  fun previousAnchor_OfStart_ReturnsNull() {
    assertThat(anchors.previousAnchor(TestAnchor.START)).isNull()
  }

  @Test
  fun nextAnchor_OfStart_ReturnsMiddle() {
    assertThat(anchors.nextAnchor(TestAnchor.START)).isEqualTo(TestAnchor.MIDDLE)
  }

  @Test
  fun nextAnchor_OfEnd_ReturnsNull() {
    assertThat(anchors.nextAnchor(TestAnchor.END)).isNull()
  }

  @Test
  fun offsetToCurrent_ReturnsZeroForUninitializedState() {
    assertThat(state.offsetToCurrent).isEqualTo(0f)
  }

  @Test
  fun offsetToCurrent_ReturnsZeroForInitializedState() {
    state.updateAnchors(anchors)
    assertThat(state.offsetToCurrent).isEqualTo(0f)
  }

  @Test
  fun offsetToCurrent_ReturnsCorrectOffset_ForUnsettledState() {
    state.updateAnchors(anchors)
    runBlocking { state.anchoredDrag { dragTo(70f) } }
    assertThat(state.offsetToCurrent).isEqualTo(20f)
    runBlocking { state.anchoredDrag { dragTo(80f) } }
    assertThat(state.offsetToCurrent).isEqualTo(-20f)
  }

  @Test
  fun isSettled_ReturnsTrue_ForUninitializedState() {
    assertThat(state.isSettled).isTrue()
  }

  @Test
  fun isSettled_ReturnsTrue_ForInitializedState() {
    state.updateAnchors(anchors)
    assertThat(state.isSettled).isTrue()
  }

  @Test
  fun isSettled_ReturnsTrue_AfterSettling() {
    state.updateAnchors(anchors)
    runBlocking { state.anchoredDrag { dragTo(100f) } }
    assertThat(state.isSettled).isTrue()
  }

  @Test
  fun isSettled_ReturnsFalse_ForUnsettledState() {
    state.updateAnchors(anchors)
    runBlocking { state.anchoredDrag { dragTo(70f) } }
    assertThat(state.isSettled).isFalse()
  }
}
