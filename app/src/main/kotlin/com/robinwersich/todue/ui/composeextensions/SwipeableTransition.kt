package com.robinwersich.todue.ui.composeextensions

import androidx.annotation.FloatRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import com.robinwersich.todue.utility.isSame
import com.robinwersich.todue.utility.map

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

  /** Specifies if the transition is currently animating or not. */
  val isSettled: Boolean
    get() = transitionStates().let { (prev, next) -> prev == next }

  /** Returns if the current transition state is in any of the given [stateRelations]. */
  fun isState(vararg stateRelations: SwipeableStateRelation<T>): Boolean {
    val transition = transitionStates()
    return stateRelations.any { relation -> transition in relation }
  }

  /**
   * Returns a smoothly interpolated value derived from the current
   * [SwipeableTransition.transitionStates] and [SwipeableTransition.progress].
   *
   * @param interpolateValue A function that interpolates between two values of the target type.
   * @param targetValueByState A function that returns the target value for a given state.
   */
  fun <V> interpolateValue(
    interpolateValue: (start: V, end: V, progress: Float) -> V,
    targetValueByState: (state: T) -> V,
  ): V {
    val (prevState, nextState) = transitionStates()
    val prevValue = targetValueByState(prevState)
    val nextValue = targetValueByState(nextState)
    return if (prevValue == nextValue) prevValue
    else interpolateValue(prevValue, nextValue, progress())
  }

  /**
   * Returns a [SwipeableValue] with a smoothly interpolated value derived from the current
   * [SwipeableTransition.transitionStates] and [SwipeableTransition.progress].
   *
   * @param interpolateValue A function that interpolates between two values of the target type.
   * @param targetValueByState A function that returns the target value for a given state.
   * @param aggregate If [targetValueByState] is a many-to-one mapping.
   */
  @Composable
  fun <V> interpolatedValue(
    interpolateValue: (start: V, end: V, progress: Float) -> V,
    targetValueByState: (state: T) -> V,
    aggregate: Boolean = false,
  ): SwipeableValue<V> =
    remember(this, interpolateValue, targetValueByState) {
      SwipeableValue(this, interpolateValue, targetValueByState, aggregate)
    }

  /**
   * Creates a derived [SwipeableTransition] using the provided [transform].
   *
   * @param aggregate If [transform] is a many-to-one mapping
   * @param transform A function mapping states of this transition to new ones
   */
  fun <S> derive(aggregate: Boolean = false, transform: (T) -> S): SwipeableTransition<S> {
    val getStates =
      if (aggregate) {
        val states =
          derivedStateOf(pairReferentialEqualityPolicy()) { transitionStates().map(transform) }
        ({ states.value })
      } else ({ transitionStates().map(transform) })
    val getProgress =
      if (aggregate) {
        val progressState = derivedStateOf { if (getStates().isSame) 0f else progress() }
        ({ progressState.value })
      } else ({ progress() })
    return SwipeableTransition(transitionStates = getStates, progress = getProgress)
  }

  /**
   * Returns a derived [SwipeableTransition] using the provided [stateTransform]
   *
   * @see derive()
   */
  @Composable
  fun <S> derived(aggregate: Boolean = false, stateTransform: (T) -> S) =
    remember(this) { derive(aggregate, stateTransform) }

  /**
   * Returns a derived [State] with discrete values derived from the continuous state of this
   * transition.
   *
   * @param stateEnds A function returning until which progress to the next state this state is
   *   considered the current one. For example, a transition from `A` to `B` with a progress of
   *   `0.4` towards `B` and a `stateEnd` of `0.3` for `A` would consider `B` the current state.
   * @param stateTransform An optional function mapping the current state to a different one.
   */
  @Composable
  fun <S> derivedState(stateEnds: (T) -> Float = { 0.5f }, stateTransform: (T) -> S) =
    remember(this, stateTransform, stateEnds) {
      derivedStateOf {
        val (prevState, nextState) = transitionStates()
        val progress = progress()
        if (progress <= stateEnds(prevState)) stateTransform(prevState)
        else stateTransform(nextState)
      }
    }

  /**
   * Returns a [State] holding current, discrete value of this transition.
   *
   * @param stateEnds A function returning until which progress to the next state this state is
   *   considered the current one. For example, a transition from `A` to `B` with a progress of
   *   `0.4` towards `B` and a `stateEnd` of `0.3` for `A` would consider `B` the current state.
   */
  @Composable
  fun currentState(stateEnds: (T) -> Float = { 0.5f }) =
    remember(this, stateEnds) {
      derivedStateOf {
        val (prevState, nextState) = transitionStates()
        if (progress() <= stateEnds(prevState)) prevState else nextState
      }
    }
}

/**
 * Stores the calculation of an interpolated value of a [SwipeableTransition] so it can be retrieved
 * later, deferring state reads.
 */
class SwipeableValue<V>
private constructor(
  private val progress: () -> Float,
  private val valueAtProgress: (() -> Float) -> V,
  aggregate: Boolean = false,
) : State<V> {
  companion object {
    /**
     * Generic factory method for creating a [SwipeableValue].
     *
     * @param transition The [SwipeableTransition] to derive the value from.
     * @param interpolateValue A function that interpolates between two values of the target type.
     * @param targetValueByState A function that returns the target value for a given state.
     * @param aggregate If [targetValueByState] is a many-to-one mapping.
     */
    operator fun <T, V> invoke(
      transition: SwipeableTransition<T>,
      interpolateValue: (start: V, end: V, progress: Float) -> V,
      targetValueByState: (state: T) -> V,
      aggregate: Boolean = false,
    ) =
      SwipeableValue(
        progress = transition.progress,
        valueAtProgress = { progress ->
          val (prev, next) = transition.transitionStates()
          val prevValue = targetValueByState(prev)
          val nextValue = targetValueByState(next)
          if (prevValue == nextValue) prevValue
          else interpolateValue(prevValue, nextValue, progress())
        },
        aggregate = aggregate,
      )
  }

  private val getValue =
    if (aggregate) derivedStateOf { valueAtProgress(progress) }.let { { it.value } }
    else ({ valueAtProgress(progress) })

  override val value
    get() = getValue()

  fun at(progress: Float) = valueAtProgress { progress }

  /** Creates a value derived from this one. */
  fun <U> derive(transform: (V) -> U) =
    SwipeableValue(progress = progress, valueAtProgress = { transform(valueAtProgress(it)) })

  /** Returns a remembered value derived from this one. */
  @Composable fun <U> derived(transform: (V) -> U) = remember(this, transform) { derive(transform) }

  override fun hashCode(): Int {
    var result = progress.hashCode()
    result = 31 * result + valueAtProgress.hashCode()
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SwipeableValue<*>) return false
    return progress == other.progress && valueAtProgress == other.valueAtProgress
  }
}

fun <T> SwipeableTransition<T>.interpolateFloat(targetValueByState: (state: T) -> Float) =
  interpolateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByState = targetValueByState,
  )

@Composable
fun <T> SwipeableTransition<T>.interpolatedFloat(
  aggregate: Boolean = false,
  targetValueByState: (state: T) -> Float,
) =
  interpolatedValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByState = targetValueByState,
    aggregate = aggregate,
  )

fun <T> SwipeableTransition<T>.interpolateColor(targetValueByState: (state: T) -> Color) =
  interpolateValue(interpolateValue = ::lerp, targetValueByState = targetValueByState)

@Composable
fun <T> SwipeableTransition<T>.interpolatedColor(
  aggregate: Boolean = false,
  targetValueByState: (state: T) -> Color,
) = interpolatedValue(interpolateValue = ::lerp, targetValueByState = targetValueByState, aggregate)

fun <T> SwipeableTransition<T>.interpolateDp(targetValueByState: (state: T) -> Dp) =
  interpolateValue(interpolateValue = ::lerp, targetValueByState = targetValueByState)

@Composable
fun <T> SwipeableTransition<T>.interpolatedDp(
  aggregate: Boolean = false,
  targetValueByState: (state: T) -> Dp,
) = interpolatedValue(interpolateValue = ::lerp, targetValueByState = targetValueByState, aggregate)

/** Describes a desired relation between the current transition state and another state. */
sealed interface SwipeableStateRelation<T> {
  /** Returns if the given [transition] has the desired relation. */
  operator fun contains(transition: Pair<T, T>): Boolean
}

/** The transition state should be directly before, after or at the given [state]. */
class Near<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) =
    state == transition.first || state == transition.second
}

/** The transition state should be at the given [state]. */
class Eq<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) =
    state == transition.first && state == transition.second
}

/** The transition state should be at or before the given [state]. */
class Leq<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) = transition.second == state
}

/** The transition state should be at or after the given [state]. */
class Geq<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) = transition.first == state
}

/** The transition state should be directly before but not at the given [state]. */
class Lt<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) =
    state == transition.second && state != transition.first
}

/** The transition state should be directly after but not at the given [state]. */
class Gt<T>(val state: T) : SwipeableStateRelation<T> {
  override fun contains(transition: Pair<T, T>) =
    state == transition.first && state != transition.second
}
