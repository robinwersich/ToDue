package com.robinwersich.todue.ui.composeextensions

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlin.math.nextDown
import kotlin.math.nextUp

@Composable
fun <T> AnchoredDraggableState<T>.rememberSwipeableTransition() =
  remember(this) { toSwipeableTransition() }

/**
 * Creates a [SwipeableTransition] controlled by this [AnchoredDraggableState]. When used in a
 * composable, [rememberSwipeableTransition] should be used instead to ensure that the same
 * transition is used across recompositions.
 */
fun <T> AnchoredDraggableState<T>.toSwipeableTransition(): SwipeableTransition<T> {
  val transitionStates by
    derivedStateOf(pairReferentialEqualityPolicy()) { getAdjacentToOffsetAnchors() }
  return SwipeableTransition(
    transitionStates = { transitionStates },
    progress = {
      val (prevAnchor, nextAnchor) = transitionStates
      progress(prevAnchor, nextAnchor)
    },
  )
}

/**
 * Returns the two anchors adjacent to the current [offset][AnchoredDraggableState.offset]. If the
 * draggable is settled or is not initialized, the same anchor is returned twice.
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToOffsetAnchors(): Pair<T, T> {
  if (offset.isNaN() || anchors.size == 0 || anchors.positionOf(settledValue) == offset) {
    return Pair(settledValue, settledValue)
  } else {
    // closest anchors cannot be null as anchors are not empty
    val prevAnchor = anchors.closestAnchor(offset, searchUpwards = false)!!
    val nextAnchor = anchors.closestAnchor(offset, searchUpwards = true)!!
    return Pair(prevAnchor, nextAnchor)
  }
}

/**
 * Returns the adjacent anchors to the current [current value][AnchoredDraggableState.currentValue].
 *
 * @see getAdjacentAnchors
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToCurrentAnchors() = getAdjacentAnchors(currentValue)

/**
 * Returns the adjacent anchors to the current [settled value][AnchoredDraggableState.settledValue].
 *
 * @see getAdjacentAnchors
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToSettledAnchors() = getAdjacentAnchors(settledValue)

/**
 * Returns the closest anchor with a smaller offset and the closest anchor with a larger offset than
 * the given [anchor]. If there is no such, the given [anchor] will be returned for the
 * corresponding bound. Consequently, for uninitialized anchors, the given [anchor] will be returned
 * twice.
 */
fun <T> AnchoredDraggableState<T>.getAdjacentAnchors(anchor: T): Pair<T, T> {
  val prevAnchor = anchors.previousAnchor(anchor) ?: anchor
  val nextAnchor = anchors.nextAnchor(anchor) ?: anchor
  return Pair(prevAnchor, nextAnchor)
}

/** Returns the anchor before (in terms of offset) the given [anchor] or null if there is none */
fun <T> DraggableAnchors<T>.previousAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  // closestAnchor(searchUpwards = false) may return larger anchor if there are no smaller anchors
  return closestAnchor(anchorOffset.nextDown(), searchUpwards = false)?.takeIf {
    positionOf(it) < anchorOffset
  }
}

/** Returns the anchor after (in terms of offset) the given [anchor]. */
fun <T> DraggableAnchors<T>.nextAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  // closestAnchor(searchUpwards = true) may return smaller anchor if there are no larger anchors
  return closestAnchor(anchorOffset.nextUp(), searchUpwards = true)?.takeIf {
    positionOf(it) > anchorOffset
  }
}

/**
 * Distance between [current anchor][AnchoredDraggableState.currentValue] and current offset. If the
 * anchors are not initialized yet, this will always return 0.
 */
val <T> AnchoredDraggableState<T>.offsetToCurrent: Float
  get() = if (offset.isNaN()) 0f else offset - anchors.positionOf(currentValue)

/**
 * Returns whether the [AnchoredDraggableState] is currently settled at an anchor. If the anchors
 * are not initialized yet, this will always return true.
 */
val <T> AnchoredDraggableState<T>.isSettled: Boolean
  get() = offset.isNaN() || anchors.positionOf(settledValue) == offset

/**
 * Workaround for applying overscroll in the original direction if reverseDirection is true on the
 * `anchoredDraggable` modifier.
 */
fun OverscrollEffect.reversed() =
  object : OverscrollEffect by this {
    override fun applyToScroll(
      delta: Offset,
      source: NestedScrollSource,
      performScroll: (Offset) -> Offset,
    ) = this@reversed.applyToScroll(-delta, source, { -performScroll(-it) })

    override suspend fun applyToFling(
      velocity: Velocity,
      performFling: suspend (Velocity) -> Velocity,
    ) = this@reversed.applyToFling(-velocity, { -performFling(-it) })
  }
