package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import kotlin.math.roundToInt

/**
 * Same functionality as [Modifier.padding][androidx.compose.foundation.layout.padding], but allows
 * negative padding values, making the content appear smaller than it is.
 */
@Stable
fun Modifier.signedPadding(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp) =
  layout { measurable, constraints ->
    val startPx = start.roundToPx()
    val endPx = end.roundToPx()
    val topPx = top.roundToPx()
    val bottomPx = bottom.roundToPx()
    val horizontalPadding = startPx + endPx
    val verticalPadding = topPx + bottomPx
    val newConstraints = constraints.offset(-horizontalPadding, -verticalPadding)
    val placeable = measurable.measure(newConstraints)

    val width = constraints.constrainWidth(placeable.width + horizontalPadding)
    val height = constraints.constrainHeight(placeable.height + verticalPadding)
    layout(width, height) { placeable.placeRelative(startPx, topPx) }
  }

/** @see signedPadding */
@Stable
fun Modifier.signedPadding(vertical: Dp = 0.dp, horizontal: Dp = 0.dp) =
  signedPadding(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

/** @see signedPadding */
@Stable fun Modifier.signedPadding(all: Dp) = signedPadding(all, all, all, all)

/** Lambda version of [Modifier.padding][androidx.compose.foundation.layout.padding] */
@Stable
fun Modifier.padding(paddingValues: () -> PaddingValues) = layout { measurable, constraints ->
  val padding = paddingValues()
  val startPx = padding.calculateLeftPadding(layoutDirection).roundToPx()
  val endPx = padding.calculateRightPadding(layoutDirection).roundToPx()
  val topPx = padding.calculateTopPadding().roundToPx()
  val bottomPx = padding.calculateBottomPadding().roundToPx()
  val horizontalPadding = startPx + endPx
  val verticalPadding = topPx + bottomPx
  val placeable = measurable.measure(constraints.offset(-horizontalPadding, -verticalPadding))

  val width = constraints.constrainWidth(placeable.width + horizontalPadding)
  val height = constraints.constrainHeight(placeable.height + verticalPadding)
  layout(width, height) { placeable.place(startPx, topPx) }
}

/** Sets the size of the content to the given [IntSize]. */
@Stable
fun Modifier.size(size: Density.() -> IntSize) = layout { measurable, constraints ->
  val targetConstraints = size().let { Constraints.fixed(it.width, it.height) }
  val placeable = measurable.measure(constraints.constrain(targetConstraints))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Composes the content with the given [size] and scales it to fit in the parent constraints. */
@Stable
fun Modifier.scaleFromSize(size: Density.() -> IntSize?) = layout { measurable, constraints ->
  val placeable =
    measurable.measure(
      size()?.let { (measureWidth, measureHeight) ->
        Constraints.fixed(measureWidth, measureHeight)
      } ?: constraints
    )
  layout(constraints.maxWidth, constraints.maxHeight) {
    placeable.placeWithLayer(0, 0) {
      transformOrigin = TransformOrigin(0f, 0f)
      if (placeable.width != constraints.maxWidth) {
        scaleX = constraints.maxWidth.toFloat() / placeable.width.toFloat()
      }
      if (placeable.height != constraints.maxHeight) {
        scaleY = constraints.maxHeight.toFloat() / placeable.height.toFloat()
      }
    }
  }
}

/**
 * Occupy all available space and place the content inside this space with the given relative
 * [offset] and [size].
 */
@Stable
fun Modifier.placeRelative(offset: () -> Offset, size: () -> Size) =
  layout { measurable, constraints ->
    val (width, height) = size()
    val widthPx = (width * constraints.maxWidth).roundToInt()
    val heightPx = (height * constraints.maxHeight).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(widthPx, heightPx))

    layout(constraints.maxWidth, constraints.maxHeight) {
      val (offsetX, offsetY) = offset()
      val offsetXPx = (offsetX * constraints.maxWidth).roundToInt()
      val offsetYPx = (offsetY * constraints.maxHeight).roundToInt()
      placeable.place(offsetXPx, offsetYPx)
    }
  }
