package com.robinwersich.todue.ui.composeextensions

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class PaddedRoundedCornerShape(private val cornerRadius: Dp, private val padding: Dp) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val paddingPx = with(density) { padding.toPx() }
    val radius = with(density) { CornerRadius(cornerRadius.toPx()) }

    return Outline.Rounded(
      RoundRect(
        left = paddingPx,
        top = paddingPx,
        right = size.width - paddingPx,
        bottom = size.height - paddingPx,
        cornerRadius = radius,
      )
    )
  }
}
