package com.robinwersich.todue.ui.composeextensions

import androidx.collection.FloatList
import androidx.collection.MutableFloatList
import androidx.collection.mutableFloatListOf
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.abs
import kotlin.math.nextDown
import kotlin.math.nextUp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AnchoredDraggableState<T>.rememberSwipeableTransition() =
  remember(this) { toSwipeableTransition() }

/**
 * Creates a [SwipeableTransition] controlled by this [AnchoredDraggableState]. When used in a
 * composable, [rememberSwipeableTransition] should be used instead to ensure that the same
 * transition is used across recompositions.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T> AnchoredDraggableState<T>.toSwipeableTransition(): SwipeableTransition<T> {
  val transitionStates by derivedStateOf { getAdjacentToOffsetAnchors() }
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
@OptIn(ExperimentalFoundationApi::class)
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
@OptIn(ExperimentalFoundationApi::class)
fun <T> AnchoredDraggableState<T>.getAdjacentToCurrentAnchors() = getAdjacentAnchors(currentValue)

/**
 * Returns the adjacent anchors to the current [settled value][AnchoredDraggableState.settledValue].
 *
 * @see getAdjacentAnchors
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T> AnchoredDraggableState<T>.getAdjacentToSettledAnchors() = getAdjacentAnchors(settledValue)

/**
 * Returns the closest anchor with a smaller offset and the closest anchor with a larger offset than
 * the given [anchor]. If there is no such, the given [anchor] will be returned for the
 * corresponding bound. Consequently, for uninitialized anchors, the given [anchor] will be returned
 * twice.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <T> AnchoredDraggableState<T>.getAdjacentAnchors(anchor: T): Pair<T, T> {
  val prevAnchor = anchors.previousAnchor(anchor) ?: anchor
  val nextAnchor = anchors.nextAnchor(anchor) ?: anchor
  return Pair(prevAnchor, nextAnchor)
}

/** Returns the anchor before (in terms of offset) the given [anchor] or null if there is none */
@OptIn(ExperimentalFoundationApi::class)
fun <T> DraggableAnchors<T>.previousAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  // closestAnchor(searchUpwards = false) may return larger anchor if there are no smaller anchors
  return closestAnchor(anchorOffset.nextDown(), searchUpwards = false)?.takeIf {
    positionOf(it) < anchorOffset
  }
}

/** Returns the anchor after (in terms of offset) the given [anchor]. */
@OptIn(ExperimentalFoundationApi::class)
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
@OptIn(ExperimentalFoundationApi::class)
val <T> AnchoredDraggableState<T>.offsetToCurrent: Float
  get() = if (offset.isNaN()) 0f else offset - anchors.positionOf(currentValue)

/**
 * Returns whether the [AnchoredDraggableState] is currently settled at an anchor. If the anchors
 * are not initialized yet, this will always return true.
 */
@OptIn(ExperimentalFoundationApi::class)
val <T> AnchoredDraggableState<T>.isSettled: Boolean
  get() = offset.isNaN() || anchors.positionOf(settledValue) == offset

// --- Fixed Version of DraggableAnchors ---

class MyDraggableAnchorsConfig<T> {
  internal val keys = mutableListOf<T>()
  internal val values = mutableFloatListOf()

  infix fun T.at(position: Float) {
    keys.add(this)
    values.add(position)
  }
}

/** Create a new [MyDraggableAnchors] instance using a builder function. */
@OptIn(ExperimentalFoundationApi::class)
fun <T : Any> MyDraggableAnchors(
  builder: MyDraggableAnchorsConfig<T>.() -> Unit
): DraggableAnchors<T> {
  val config = MyDraggableAnchorsConfig<T>().apply(builder)
  val sortedIndices = config.values.indices.sortedBy { config.values[it] }
  return MyDraggableAnchors(
    keys = sortedIndices.map { config.keys[it] },
    values =
      MutableFloatList(initialCapacity = config.values.size).apply {
        sortedIndices.forEach { add(config.values[it]) }
      },
  )
}

@OptIn(ExperimentalFoundationApi::class)
private class MyDraggableAnchors<T>(private val keys: List<T>, private val values: FloatList) :
  DraggableAnchors<T> {
  override fun positionOf(value: T): Float {
    val index = keys.indexOf(value)
    return if (index == -1) Float.NaN else values[index]
  }

  override fun hasAnchorFor(value: T) = keys.contains(value)

  override fun closestAnchor(position: Float): T? {
    var minAnchor: T? = null
    var minDistance = Float.POSITIVE_INFINITY
    keys.forEachIndexed { index, anchor ->
      val anchorPosition = values[index]
      val distance = abs(position - anchorPosition)
      if (distance <= minDistance) {
        minAnchor = anchor
        minDistance = distance
      }
    }
    return minAnchor
  }

  override fun closestAnchor(position: Float, searchUpwards: Boolean): T? {
    var minAnchor: T? = null
    var minDistance = Float.POSITIVE_INFINITY
    keys.forEachIndexed { index, anchor ->
      val anchorPosition = values[index]
      val delta = if (searchUpwards) anchorPosition - position else position - anchorPosition
      val distance = if (delta < 0) Float.POSITIVE_INFINITY else delta
      if (distance <= minDistance) {
        minAnchor = anchor
        minDistance = distance
      }
    }
    return minAnchor
  }

  override fun minAnchor() = if (values.isEmpty()) Float.NEGATIVE_INFINITY else values.first()

  override fun maxAnchor() = if (values.isEmpty()) Float.POSITIVE_INFINITY else values.last()

  override val size: Int
    get() = keys.size

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is MyDraggableAnchors<*>) return false

    return keys == other.keys && values == other.values
  }

  override fun hashCode() = 31 * keys.hashCode() + values.hashCode()

  override fun toString() =
    "MyDraggableAnchors(${keys.mapIndexed { index, key -> "$key at ${values[index]}" }})"

  override fun forEach(block: (anchor: T, position: Float) -> Unit) {
    keys.forEachIndexed { index, key -> block(key, values[index]) }
  }
}
