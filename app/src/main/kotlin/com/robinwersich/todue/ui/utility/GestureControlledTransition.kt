package com.robinwersich.todue.ui.utility

import androidx.collection.FloatList
import androidx.collection.MutableFloatList
import androidx.collection.mutableFloatListOf
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import kotlin.math.abs
import kotlin.math.nextDown
import kotlin.math.nextUp

/**
 * Returns a smoothly interpolated [Float] derived from the current [AnchoredDraggableState] value.
 * This composable function avoids calling [targetValueByAnchor] for every offset change.
 *
 * @param targetValueByAnchor A function that returns a [Float] for a given anchor.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <S> AnchoredDraggableState<S>.animateFloat(targetValueByAnchor: (anchor: S) -> Float) =
  animateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByAnchor = targetValueByAnchor,
  )

/**
 * Returns a smoothly interpolated value derived from the current [AnchoredDraggableState] value.
 * This composable function avoids calling [targetValueByAnchor] for every offset change.
 *
 * @param interpolateValue A function that interpolates between two values of the target type.
 * @param targetValueByAnchor A function that returns the target value for a given anchor.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <S, T> AnchoredDraggableState<S>.animateValue(
  interpolateValue: (start: T, end: T, progress: Float) -> T,
  targetValueByAnchor: (anchor: S) -> T,
): T {
  val (prevAnchor, nextAnchor) =
    remember { derivedStateOf(structuralEqualityPolicy()) { getAdjacentToOffsetAnchors() } }.value
  val prevValue = remember(prevAnchor, targetValueByAnchor) { targetValueByAnchor(prevAnchor) }
  val nextValue = remember(nextAnchor, targetValueByAnchor) { targetValueByAnchor(nextAnchor) }
  return interpolateValue(prevValue, nextValue, progress(prevAnchor, nextAnchor))
}

/**
 * Returns a smoothly interpolated [Float] derived from the current [AnchoredDraggableState] value.
 *
 * @param targetValueByAnchor A function that returns a [Float] for a given anchor.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <S> AnchoredDraggableState<S>.interpolateFloat(targetValueByAnchor: (anchor: S) -> Float) =
  interpolateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByAnchor = targetValueByAnchor,
  )

/**
 * Returns a smoothly interpolated value derived from the current [AnchoredDraggableState] value.
 *
 * @param interpolateValue A function that interpolates between two values of the target type.
 * @param targetValueByAnchor A function that returns the target value for a given anchor.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <S, T> AnchoredDraggableState<S>.interpolateValue(
  interpolateValue: (start: T, end: T, progress: Float) -> T,
  targetValueByAnchor: (anchor: S) -> T,
): T {
  val (prevAnchor, nextAnchor) = getAdjacentToOffsetAnchors()
  val prevValue = targetValueByAnchor(prevAnchor)
  val nextValue = targetValueByAnchor(nextAnchor)
  return interpolateValue(prevValue, nextValue, progress(prevAnchor, nextAnchor))
}

/**
 * Returns the two anchors adjacent to the current [offset][AnchoredDraggableState.offset]. If the
 * draggable is settled or is not initialized, the same anchor is returned twice.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <S> AnchoredDraggableState<S>.getAdjacentToOffsetAnchors(): Pair<S, S> {
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
 * Returns the closest anchor with a smaller offset and the closest anchor with a larger offset than
 * the current [offset][AnchoredDraggableState.offset]. If the draggable is settled or is not
 * initialized, the same anchor is returned twice.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <S> AnchoredDraggableState<S>.getAdjacentToCurrentAnchors(): Pair<S, S> {
  val prevAnchor = anchors.previousAnchor(currentValue) ?: currentValue
  val nextAnchor = anchors.nextAnchor(currentValue) ?: currentValue
  return Pair(prevAnchor, nextAnchor)
}

/** Returns the anchor before (in terms of offset) the given [anchor] or null if there is none */
@OptIn(ExperimentalFoundationApi::class)
fun <T> DraggableAnchors<T>.previousAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  return closestAnchor(anchorOffset.nextDown(), searchUpwards = false)
}

/** Returns the anchor after (in terms of offset) the given [anchor]. */
@OptIn(ExperimentalFoundationApi::class)
fun <T> DraggableAnchors<T>.nextAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  return closestAnchor(anchorOffset.nextUp(), searchUpwards = true)
}

/** Distance between [current anchor][AnchoredDraggableState.currentValue] and current offset. */
@OptIn(ExperimentalFoundationApi::class)
val <S> AnchoredDraggableState<S>.offsetToCurrent
  get() = offset - anchors.positionOf(currentValue)

/** Returns whether the [AnchoredDraggableState] is currently settled at an anchor. */
@OptIn(ExperimentalFoundationApi::class)
val <S> AnchoredDraggableState<S>.isSettled: Boolean
  get() = anchors.positionOf(settledValue) == offset

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
