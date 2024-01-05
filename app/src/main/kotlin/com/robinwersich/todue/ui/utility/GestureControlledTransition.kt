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
    derivedStateOf {
      val currentAnchors = anchors
      if (
        offset.isNaN() ||
          currentAnchors.size == 0 ||
          currentAnchors.positionOf(currentValue) == offset
      ) {
        return@derivedStateOf targetValueByAnchor(currentValue)
      }

      val prevAnchor = currentAnchors.closestAnchor(offset, searchUpwards = false)!!
      val nextAnchor = currentAnchors.closestAnchor(offset, searchUpwards = true)!!

      val prevAnchorPosition = currentAnchors.positionOf(prevAnchor)
      val nextAnchorPosition = currentAnchors.positionOf(nextAnchor)
      val prevValue = targetValueByAnchor(prevAnchor)
      val nextValue = targetValueByAnchor(nextAnchor)

      if (prevValue == nextValue) return@derivedStateOf prevValue

      return@derivedStateOf interpolateValue(
        prevValue,
        nextValue,
        (offset - prevAnchorPosition) / (nextAnchorPosition - prevAnchorPosition)
      )
    }
  }
}
