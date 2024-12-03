package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
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
fun Modifier.scaleFromSize(size: Density.() -> IntSize) = layout { measurable, constraints ->
  val (measureWidth, measureHeight) = size()
  val placeable = measurable.measure(Constraints.fixed(measureWidth, measureHeight))
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
 * Composes the content with the given padded [size] and scales it to fit in the padded parent
 * constraints. This way, the padding isn't scaled.
 */
@Stable
fun Modifier.scaleFromSize(padding: PaddingValues, size: Density.() -> IntSize) =
  padding(padding).scaleFromSize {
    val (width, height) = size()
    val verticalPadding = padding.calculateTopPadding() + padding.calculateBottomPadding()
    // layout direction doesn't matter since we add up left and right padding
    val horizontalPadding =
      padding.calculateLeftPadding(LayoutDirection.Ltr) +
        padding.calculateRightPadding(LayoutDirection.Ltr)
    IntSize(
      (width - horizontalPadding.roundToPx()).coerceAtLeast(0),
      (height - verticalPadding.roundToPx()).coerceAtLeast(0),
    )
  }

/** Restricts the maximum width of the content to the minimum width. */
@Stable
fun Modifier.fillMinWidth() = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints.copy(maxWidth = constraints.minWidth))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Restricts the maximum height of the content to the minimum height. */
@Stable
fun Modifier.fillMinHeight() = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints.copy(maxHeight = constraints.minHeight))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Restricts the maximum size of the content to the minimum size. */
@Stable
fun Modifier.fillMinSize() = layout { measurable, constraints ->
  val placeable =
    measurable.measure(
      constraints.copy(maxWidth = constraints.minWidth, maxHeight = constraints.minHeight)
    )
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Measure content with max width and wrap it to min width. */
@Stable
fun Modifier.wrapToMinWidth(alignment: Alignment.Horizontal = Alignment.CenterHorizontally) =
  layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.maxWidth))
    layout(width = constraints.minWidth, height = placeable.height) {
      placeable.place(
        IntOffset(alignment.align(placeable.width, constraints.minWidth, layoutDirection), 0)
      )
    }
  }

/** Measure content with max height and wrap it to min height. */
@Stable
fun Modifier.wrapToMinHeight(alignment: Alignment.Vertical = Alignment.CenterVertically) =
  layout { measurable, constraints ->
    val placeable = measurable.measure(constraints.copy(minHeight = constraints.maxHeight))
    layout(width = placeable.width, height = constraints.minHeight) {
      placeable.place(IntOffset(0, alignment.align(placeable.height, constraints.minHeight)))
    }
  }

/** Measure content with max size and wrap it to min size. */
@Stable
fun Modifier.wrapToMinSize(alignment: Alignment = Alignment.Center) =
  layout { measurable, constraints ->
    val placeable =
      measurable.measure(
        constraints.copy(minWidth = constraints.maxWidth, minHeight = constraints.maxHeight)
      )
    layout(width = constraints.minWidth, height = constraints.minHeight) {
      placeable.place(
        alignment.align(
          IntSize(placeable.width, placeable.height),
          IntSize(constraints.minWidth, constraints.minHeight),
          layoutDirection,
        )
      )
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

/**
 * Occupy all available space and place the content inside this space with the given relative
 * [offset] and [size], but measure it with the given [measureSize], scaling it to the final size.
 */
@Stable
fun Modifier.placeRelativeScaling(offset: () -> Offset, size: () -> Size, measureSize: () -> Size) =
  layout { measurable, constraints ->
    val (measureWidth, measureHeight) = measureSize()
    val measureWidthPx = (measureWidth * constraints.maxWidth).roundToInt()
    val measureHeightPx = (measureHeight * constraints.maxHeight).roundToInt()
    val placeable = measurable.measure(Constraints.fixed(measureWidthPx, measureHeightPx))

    layout(constraints.maxWidth, constraints.maxHeight) {
      val (offsetX, offsetY) = offset()
      val offsetXPx = (offsetX * constraints.maxWidth).roundToInt()
      val offsetYPx = (offsetY * constraints.maxHeight).roundToInt()
      placeable.placeWithLayer(offsetXPx, offsetYPx) {
        transformOrigin = TransformOrigin(0f, 0f)
        val (placeWidth, placeHeight) = size()
        scaleX = placeWidth / measureWidth
        scaleY = placeHeight / measureHeight
      }
    }
  }

/**
 * Occupy all available space and place the content inside this space with the given relative
 * [offset] and [size], but measure it with the given [measureSize], scaling it to the final size.
 * Adjusts the content size and [measureSize] with the given [padding].
 */
@Stable
fun Modifier.placeRelativeScaling(
  offset: () -> Offset,
  size: () -> Size,
  measureSize: () -> Size,
  padding: PaddingValues,
) = layout { measurable, constraints ->
  val leftPad = padding.calculateLeftPadding(layoutDirection).toPx()
  val rightPad = padding.calculateRightPadding(layoutDirection).toPx()
  val topPad = padding.calculateTopPadding().toPx()
  val bottomPad = padding.calculateBottomPadding().toPx()

  val (measureWidth, measureHeight) = measureSize()
  val measureWidthPx = measureWidth * constraints.maxWidth - leftPad - rightPad
  val measureHeightPx = measureHeight * constraints.maxHeight - topPad - bottomPad
  val placeable =
    measurable.measure(Constraints.fixed(measureWidthPx.roundToInt(), measureHeightPx.roundToInt()))

  layout(constraints.maxWidth, constraints.maxHeight) {
    val (offsetX, offsetY) = offset()
    val offsetXPx = offsetX * constraints.maxWidth + leftPad
    val offsetYPx = offsetY * constraints.maxHeight + topPad
    placeable.placeWithLayer(offsetXPx.roundToInt(), offsetYPx.roundToInt()) {
      transformOrigin = TransformOrigin(0f, 0f)
      val (placeWidth, placeHeight) = size()
      val placeWidthPx = placeWidth * constraints.maxWidth - leftPad - rightPad
      val placeHeightPx = placeHeight * constraints.maxHeight - topPad - bottomPad
      scaleX = placeWidthPx / measureWidthPx
      scaleY = placeHeightPx / measureHeightPx
    }
  }
}
