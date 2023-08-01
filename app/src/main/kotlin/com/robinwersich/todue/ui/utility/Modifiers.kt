package com.robinwersich.todue.ui.utility

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

fun Modifier.signedPadding(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp) =
  this.layout { measurable, constraints ->
    val horizontalPadding = (start + end).roundToPx()
    val verticalPadding = (top + bottom).roundToPx()
    val newConstraints = constraints.offset(-horizontalPadding, -verticalPadding)
    val placeable = measurable.measure(newConstraints)

    layout(placeable.width + horizontalPadding, placeable.height + verticalPadding) {
      placeable.placeRelative(start.roundToPx(), top.roundToPx())
    }
  }

fun Modifier.signedPadding(vertical: Dp = 0.dp, horizontal: Dp = 0.dp) =
  this.signedPadding(start = horizontal, top = vertical, end = horizontal, bottom = vertical)

fun Modifier.signedPadding(all: Dp) = this.signedPadding(all, all, all, all)
