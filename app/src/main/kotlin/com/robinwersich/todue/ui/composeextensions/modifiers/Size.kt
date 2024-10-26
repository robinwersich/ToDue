package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

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
