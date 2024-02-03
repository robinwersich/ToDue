package com.robinwersich.todue.ui.utility

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <S> AnchoredDraggableState<S>.animateFloat(
  targetValueByAnchor: (anchor: S) -> Float
): State<Float> =
  animateValue(
    interpolateValue = { start, end, progress -> start * (1 - progress) + end * progress },
    targetValueByAnchor = targetValueByAnchor
  )

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <S, T> AnchoredDraggableState<S>.animateValue(
  interpolateValue: (start: T, end: T, progress: Float) -> T,
  targetValueByAnchor: (anchor: S) -> T
): State<T> {
  return remember(interpolateValue, targetValueByAnchor) {
    derivedStateOf { getInterpolatedValue(interpolateValue, targetValueByAnchor) }
  }
}

/**
 * Returns a smoothly interpolated value derived from the current [AnchoredDraggableState] value.
 *
 * @param interpolateValue A function that interpolates between two values of the target type.
 * @param targetValueByAnchor A function that returns the target value for a given anchor.
 */
@OptIn(ExperimentalFoundationApi::class)
fun <S, T> AnchoredDraggableState<S>.getInterpolatedValue(
  interpolateValue: (start: T, end: T, progress: Float) -> T,
  targetValueByAnchor: (anchor: S) -> T
): T {
  val currentAnchors = anchors
  if (
    offset.isNaN() || currentAnchors.size == 0 || currentAnchors.positionOf(currentValue) == offset
  ) {
    return targetValueByAnchor(currentValue)
  }

  val prevAnchor = currentAnchors.closestAnchor(offset, searchUpwards = false)!!
  val nextAnchor = currentAnchors.closestAnchor(offset, searchUpwards = true)!!

  val prevAnchorPosition = currentAnchors.positionOf(prevAnchor)
  val nextAnchorPosition = currentAnchors.positionOf(nextAnchor)
  val prevValue = targetValueByAnchor(prevAnchor)
  val nextValue = targetValueByAnchor(nextAnchor)

  return if (prevValue == nextValue) {
    prevValue
  } else {
    interpolateValue(
      prevValue,
      nextValue,
      (offset - prevAnchorPosition) / (nextAnchorPosition - prevAnchorPosition)
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
val <S> AnchoredDraggableState<S>.isSettled: Boolean
  get() = anchors.positionOf(currentValue) == offset
