package com.robinwersich.todue.ui.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy

/**
 * This fulfills a similar role as [Transition][androidx.compose.animation.core.Transition], but
 * instead of allowing only state changes, triggering fire-once animations, it allows to provide
 * animation progress as an input value, so that it can be controlled by swipeable composables, such
 * as [anchoredDraggable][androidx.compose.foundation.gestures.anchoredDraggable] or
 * [Pager][androidx.compose.foundation.pager.Pager].
 *
 * @param T The type of the state that is transitioned between.
 * @param transitionState A function returning the current [SwipeableTransitionState].
 */
class SwipeableTransition<T>(private val transitionState: () -> SwipeableTransitionState<T>) {
  /**
   * Returns a smoothly interpolated [Float] derived from the current [transitionState].
   *
   * @param targetValueByState A function that returns a [Float] for a given state.
   */
  fun interpolateFloat(targetValueByState: (state: T) -> Float) =
    interpolateValue(
      interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
      targetValueByState = targetValueByState,
    )

  /**
   * Returns a smoothly interpolated value derived from the current [transitionState].
   *
   * @param interpolateValue A function that interpolates between two values of the target type.
   * @param targetValueByState A function that returns the target value for a given state.
   */
  fun <V> interpolateValue(
    interpolateValue: (start: V, end: V, progress: Float) -> V,
    targetValueByState: (state: T) -> V,
  ): V {
    val state = transitionState()
    val prevValue = targetValueByState(state.prev)
    val nextValue = targetValueByState(state.next)
    return interpolateValue(prevValue, nextValue, state.progress)
  }

  /**
   * Creates a derived [SwipeableTransition] that transforms the state using the provided function.
   * This is useful for better readability and reduces the number of recompositions. In composable
   * functions, [rememberDerived] should be used instead, to ensure that the same transition is used
   * across recompositions.
   */
  fun <S> derived(stateTransform: (T) -> S) = SwipeableTransition {
    derivedStateOf(structuralEqualityPolicy()) {
        val state = transitionState()
        SwipeableTransitionState(
          prev = stateTransform(state.prev),
          next = stateTransform(state.next),
          progress = if (state.prev == state.next) 0f else state.progress,
        )
      }
      .value
  }

  /** @see derived() */
  @Composable
  fun <S> rememberDerived(stateTransform: (T) -> S) = remember(this) { derived(stateTransform) }
}

/**
 * Describes the state of a transition between two values. This includes the two values between
 * which the transition is happening, as well as the progress. If the transition is settled, [prev]
 * and [next] should be equal and [progress] should be 0 or 1.
 */
data class SwipeableTransitionState<T>(val prev: T, val next: T, val progress: Float)
