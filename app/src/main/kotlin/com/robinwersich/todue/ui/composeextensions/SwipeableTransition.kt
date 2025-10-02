package com.robinwersich.todue.ui.composeextensions

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import com.robinwersich.todue.utility.areSame
import com.robinwersich.todue.utility.letIf
import com.robinwersich.todue.utility.map
import com.robinwersich.todue.utility.relativeProgress

/**
 * This fulfills a similar role as [Transition][androidx.compose.animation.core.Transition], but
 * instead of allowing only state changes, triggering fire-once animations, it allows to provide
 * animation progress as an input value, so that it can be controlled by swipeable composables, such
 * as [anchoredDraggable][androidx.compose.foundation.gestures.anchoredDraggable] or
 * [Pager][androidx.compose.foundation.pager.Pager].
 *
 * Note that this kind of transition has a "direction", meaning that states have an order and
 * certain states come "before" others. Depending on the underlying swipeable this order is based on
 * layout or swipe direction.
 *
 * @param transitionStates A function that returns the two states between which is transitioned. The
 *   [first][Pair.first] state is considered to come "before" the [second][Pair.second] one. If the
 *   transition is currently settled, the two states should be equal.
 * @param progress A function that returns the progress of the transition from the
 *   [first][Pair.first] to the [second][Pair.second] state.
 */
class SwipeableTransition<T>(
  val transitionStates: () -> Pair<T, T>,
  @FloatRange(from = 0.0, to = 1.0) val progress: () -> Float,
) {
  /** Convenience constructor for previews. */
  constructor(state: T) : this({ state to state }, { 0f })

  /**
   * Returns a smoothly interpolated value derived from the current [transitionStates] and
   * [progress].
   *
   * @param lerp A function that interpolates between two values of the target type.
   * @param transform A function that returns the target value for a given state.
   */
  fun <V> interpolateValue(
    lerp: (start: V, end: V, progress: Float) -> V,
    transform: (state: T) -> V,
  ): V {
    val (prevState, nextState) = transitionStates()
    val prevValue = transform(prevState)
    val nextValue = transform(nextState)
    return if (prevValue == nextValue) prevValue else lerp(prevValue, nextValue, progress())
  }

  /**
   * Returns a [State] object with a smoothly interpolated value derived from the current
   * [transitionStates] and [progress].
   *
   * @param lerp A function that interpolates between two values of the target type.
   * @param useState Whether to use [derivedStateOf] to cache the result and reduce recompositions
   *   if [transform] is a many-to-one mapping
   * @param transform A function that returns the target value for a given state.
   */
  @Composable
  fun <V> interpolatedValue(
    lerp: (start: V, end: V, progress: Float) -> V,
    useState: Boolean = false,
    transform: (state: T) -> V,
  ): State<V> =
    remember(this, lerp, useState, transform) {
      object : State<V> {
        private val getValue =
          { interpolateValue(lerp, transform) }.letIf(useState) {
            it.withDerivedState(structuralEqualityPolicy())
          }
        override val value
          get() = getValue()
      }
    }

  /**
   * Returns a smoothly interpolated value derived from the current [transitionStates] and
   * [progress].
   *
   * @param lerp A function that interpolates between two values of the target type.
   * @param padding A function providing padding for each state based on the adjacent state. This
   *   extends the area in which the transition produces a settled value.
   * @param transform A function that returns the target value for a given state.
   */
  fun <V> interpolateValue(
    lerp: (start: V, end: V, progress: Float) -> V,
    padding: (state: T, otherState: T) -> Float,
    transform: (state: T) -> V,
  ): V {
    val (prevState, nextState) = transitionStates()
    val prevValue = transform(prevState)
    val nextValue = transform(nextState)
    if (prevValue == nextValue) return prevValue

    val prevPadding = padding(prevState, nextState)
    val nextPadding = padding(nextState, prevState)
    assert(prevPadding + nextPadding <= 1f) {
      "State paddings of $prevState ($prevPadding) and $nextState ($nextPadding) overlap."
    }
    return lerp(prevValue, nextValue, relativeProgress(prevPadding, 1 - nextPadding, progress()))
  }

  /**
   * Returns a [State] object with a smoothly interpolated value derived from the current
   * [transitionStates] and [progress].
   *
   * @param lerp A function that interpolates between two values of the target type.
   * @param padding A function providing padding for each state based on the adjacent state. This
   *   extends the area in which the transition produces a settled value.
   * @param transform A function that returns the target value for a given state.
   */
  @Composable
  fun <V> interpolatedValue(
    lerp: (start: V, end: V, progress: Float) -> V,
    padding: (state: T, otherState: T) -> Float,
    transform: (state: T) -> V,
  ) =
    remember(this, lerp, padding, transform) {
      object : State<V> {
        private val getValue =
          { interpolateValue(lerp, padding, transform) }.withDerivedState(
            structuralEqualityPolicy()
          )
        override val value
          get() = getValue()
      }
    }

  /**
   * Returns a value derived from the current [transitionStates].
   *
   * @param transform A function that returns the target value for a given state.
   */
  fun <V> deriveValue(transform: (prevState: T, nextState: T) -> V): V {
    val (prevState, nextState) = transitionStates()
    return transform(prevState, nextState)
  }

  /**
   * Returns a [State] object with a value derived from the current transition state.
   *
   * @param useState Whether to use [derivedStateOf] to cache the result and reduce recompositions.
   * @param transform A function that returns the target value for a given state.
   */
  @Composable
  fun <V> derivedValue(useState: Boolean = false, transform: (prevState: T, nextState: T) -> V) =
    remember(this, useState, transform) {
      object : State<V> {
        private val getValue =
          { deriveValue(transform) }.letIf(useState) {
            it.withDerivedState(structuralEqualityPolicy())
          }
        override val value
          get() = getValue()
      }
    }

  /**
   * Returns a [State] object with a value derived from the current transition state (without
   * interpolation).
   *
   * @param threshold Progress at which the value should switch.
   * @param transform A function that returns the target value for a given state.
   */
  @Composable
  fun <V> derivedValue(threshold: Float = 0.5f, transform: (state: T) -> V) =
    remember(this, transform, threshold) {
      object : State<V> {
        private val currentState: State<T> =
          derivedStateOf(structuralEqualityPolicy()) {
            val (prevState, nextState) = transitionStates()
            if (prevState == nextState) prevState
            else if (progress() < threshold) prevState else nextState
          }
        override val value
          get() = transform(currentState.value)
      }
    }

  /**
   * Creates a derived [SwipeableTransition] using the provided state [transform].
   *
   * @param manyToOne If [transform] is a many-to-one mapping. This will will use [derivedStateOf]
   *   to reduce the number of recompositions caused by state and progress changes of the original
   *   transition.
   * @param cacheStates If the results of [transform] should be cached. This defaults to true if
   *   [manyToOne] is set but may want to be used even if [manyToOne] is false in case of an
   *   expensive [transform] function and multiple state reads.
   * @param transform A function mapping states of this transition to new ones.
   */
  fun <S> derive(
    manyToOne: Boolean = false,
    cacheStates: Boolean = manyToOne,
    transform: (T) -> S,
  ): SwipeableTransition<S> {
    val getStates =
      { transitionStates().map(transform) }.letIf(cacheStates) {
        it.withDerivedState(structuralEqualityPolicy())
      }

    val getProgress =
      if (manyToOne) {
        { if (getStates().areSame) 0f else progress() }.withDerivedState()
      } else {
        progress
      }

    return SwipeableTransition(transitionStates = getStates, progress = getProgress)
  }

  /**
   * Returns a derived [SwipeableTransition] using the provided [transform]
   *
   * @see derive()
   */
  @Composable
  fun <S> derived(
    manyToOne: Boolean = false,
    cacheStates: Boolean = manyToOne,
    transform: (T) -> S,
  ) =
    remember(this, manyToOne, cacheStates, transform) { derive(manyToOne, cacheStates, transform) }

  /**
   * Creates a derived [SwipeableTransition] with different states and state [padding].
   *
   * @param manyToOne If [transform] is a many-to-one mapping. This will will use [derivedStateOf]
   *   to reduce the number of recompositions caused by state and progress changes of the original
   *   transition.
   * @param cacheStates If the results of [transform] should be cached. This defaults to true if
   *   [manyToOne] is set but may want to be used even if [manyToOne] is false in case of an
   *   expensive [transform] function and multiple state reads.
   * @param padding A function providing padding for each state based on the adjacent state. This
   *   extends the area in which the transition produces a settled value.
   * @param transform A function mapping states of this transition to new ones.
   */
  fun <S> derive(
    padding: (state: T, otherState: T) -> Float,
    manyToOne: Boolean = false,
    cacheStates: Boolean = manyToOne,
    transform: (T) -> S,
  ): SwipeableTransition<S> {
    val getStates =
      { transitionStates().map(transform) }.letIf(cacheStates) {
        it.withDerivedState(structuralEqualityPolicy())
      }

    val getProgress =
      {
          val (prev, next) = transitionStates()
          relativeProgress(
            start = padding(prev, next),
            end = 1 - padding(next, prev),
            progress = progress(),
          )
        }
        .letIf(manyToOne) { { if (getStates().areSame) 0f else it() } }
        .withDerivedState()

    return SwipeableTransition(transitionStates = getStates, progress = getProgress)
  }

  @Composable
  fun <S> derived(
    padding: (state: T, otherState: T) -> Float,
    manyToOne: Boolean = false,
    cacheStates: Boolean = manyToOne,
    transform: (T) -> S,
  ) =
    remember(this, padding, manyToOne, cacheStates, transform) {
      derive(padding, manyToOne, cacheStates, transform)
    }
}

/** Caches and minimizes the update frequency of a value getter using [derivedStateOf]. */
private fun <T> (() -> T).withDerivedState(policy: SnapshotMutationPolicy<T>) =
  derivedStateOf(policy, this).let { { it.value } }

/** Caches and minimizes the update frequency of a value getter using [derivedStateOf]. */
private fun <T> (() -> T).withDerivedState() = derivedStateOf(this).let { { it.value } }
