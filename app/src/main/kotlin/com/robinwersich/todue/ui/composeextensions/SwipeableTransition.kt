package com.robinwersich.todue.ui.composeextensions

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import com.robinwersich.todue.utility.map

/**
 * This fulfills a similar role as [Transition][androidx.compose.animation.core.Transition], but
 * instead of allowing only state changes, triggering fire-once animations, it allows to provide
 * animation progress as an input value, so that it can be controlled by swipeable composables, such
 * as [anchoredDraggable][androidx.compose.foundation.gestures.anchoredDraggable] or
 * [Pager][androidx.compose.foundation.pager.Pager].
 *
 * @param transitionStates A function that returns the two states between which is transitioned. If
 *   the transition is currently settled, the two states should be equal.
 * @param progress A function that returns the progress of the transition from the first to the
 *   second state.
 */
class SwipeableTransition<T>(
  val transitionStates: () -> Pair<T, T>,
  @FloatRange(from = 0.0, to = 1.0) val progress: () -> Float,
) {
  /** Convenience constructor for previews. */
  constructor(state: T) : this({ state to state }, { 0f })

  /**
   * Creates a derived [SwipeableTransition] that transforms the state using the provided function.
   *
   * @param stateTransform A function mapping the original states to the derived states.
   */
  fun <S> derived(stateTransform: (T) -> S): SwipeableTransition<S> {
    val derivedStateTransition by derivedStateOf { transitionStates().map(stateTransform) }
    val derivedProgress by derivedStateOf {
      if (derivedStateTransition.first == derivedStateTransition.second) 0f else progress()
    }
    return SwipeableTransition(
      transitionStates = { derivedStateTransition },
      progress = { derivedProgress },
    )
  }

  /** @see derived() */
  @Composable
  fun <S> rememberDerived(stateTransform: (T) -> S) = remember(this) { derived(stateTransform) }
}

/**
 * Returns a smoothly interpolated value derived from the current
 * [SwipeableTransition.transitionStates] and [SwipeableTransition.progress].
 *
 * @param interpolateValue A function that interpolates between two values of the target type.
 * @param targetValueByState A function that returns the target value for a given state.
 */
fun <T, V> SwipeableTransition<T>.interpolateValue(
  interpolateValue: (start: V, end: V, progress: Float) -> V,
  targetValueByState: (state: T) -> V,
): V {
  val (prevState, nextState) = transitionStates()
  val prevValue = targetValueByState(prevState)
  val nextValue = targetValueByState(nextState)
  // don't read progress if previous and next value are equal to avoid unnecessary recompositions
  if (prevValue == nextValue) return prevValue
  return interpolateValue(prevValue, nextValue, progress())
}

/**
 * Returns a smoothly interpolated value derived from the current
 * [SwipeableTransition.transitionStates] and [SwipeableTransition.progress].
 *
 * @param interpolateValue A function that interpolates between two values of the target type.
 * @param targetValueByState A function that returns the target value for a given state.
 */
@Composable
fun <T, V> SwipeableTransition<T>.animateValue(
  interpolateValue: (start: V, end: V, progress: Float) -> V,
  targetValueByState: @Composable (state: T) -> V,
): V {
  val (prevState, nextState) = transitionStates()
  val prevValue = targetValueByState(prevState)
  val nextValue = targetValueByState(nextState)
  // don't read progress if previous and next value are equal to avoid unnecessary recompositions
  if (prevValue == nextValue) return prevValue
  return interpolateValue(prevValue, nextValue, progress())
}

fun <T> SwipeableTransition<T>.interpolateFloat(targetValueByState: (state: T) -> Float) =
  interpolateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByState = targetValueByState,
  )

@Composable
fun <T> SwipeableTransition<T>.animateFloat(targetValueByState: @Composable (state: T) -> Float) =
  animateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByState = targetValueByState,
  )

@Composable
fun <T> SwipeableTransition<T>.animateColor(targetValueByState: @Composable (state: T) -> Color) =
  animateValue(interpolateValue = ::lerp, targetValueByState = targetValueByState)

@Composable
fun <T> SwipeableTransition<T>.animateDp(targetValueByState: @Composable (state: T) -> Dp) =
  animateValue(interpolateValue = ::lerp, targetValueByState = targetValueByState)
