package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrain

/** Sets the size of the content to the given [IntSize]. */
fun Modifier.size(size: Density.() -> IntSize) = layout { measurable, constraints ->
  val targetConstraints = size().let { Constraints.fixed(it.width, it.height) }
  val placeable = measurable.measure(constraints.constrain(targetConstraints))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Composes the content with the given [size] and scales it to fit in the parent constraints. */
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
fun Modifier.fillMinWidth() = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints.copy(maxWidth = constraints.minWidth))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Restricts the maximum height of the content to the minimum height. */
fun Modifier.fillMinHeight() = layout { measurable, constraints ->
  val placeable = measurable.measure(constraints.copy(maxHeight = constraints.minHeight))
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Restricts the maximum size of the content to the minimum size. */
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
