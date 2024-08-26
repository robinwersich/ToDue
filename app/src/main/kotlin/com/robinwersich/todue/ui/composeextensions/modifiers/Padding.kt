package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

/**
 * Same functionality as [Modifier.padding][androidx.compose.foundation.layout.padding], but allows
 * negative padding values, making the content appear smaller than it is.
 */
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
fun Modifier.signedPadding(vertical: Dp = 0.dp, horizontal: Dp = 0.dp) =
  signedPadding(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

/** @see signedPadding */
fun Modifier.signedPadding(all: Dp) = signedPadding(all, all, all, all)

/** Lambda version of [Modifier.padding][androidx.compose.foundation.layout.padding] */
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
