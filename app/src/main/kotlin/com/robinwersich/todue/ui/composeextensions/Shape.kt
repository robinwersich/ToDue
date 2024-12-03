package com.robinwersich.todue.ui.composeextensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class PaddedRoundedCornerShape(
  private val cornerRadius: Dp,
  private val paddingValues: PaddingValues,
) : Shape {
  constructor(cornerRadius: Dp, padding: Dp) : this(cornerRadius, PaddingValues(padding))

  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    with(density) {
      val paddingTop = paddingValues.calculateTopPadding().toPx()
      val paddingBottom = paddingValues.calculateBottomPadding().toPx()
      val paddingStart = paddingValues.calculateStartPadding(layoutDirection).toPx()
      val paddingEnd = paddingValues.calculateEndPadding(layoutDirection).toPx()
      val paddingLeft = if (layoutDirection == LayoutDirection.Ltr) paddingStart else paddingEnd
      val paddingRight = if (layoutDirection == LayoutDirection.Ltr) paddingEnd else paddingStart
      val radius = CornerRadius(cornerRadius.toPx())

      return Outline.Rounded(
        RoundRect(
          left = paddingLeft,
          top = paddingTop,
          right = size.width - paddingRight,
          bottom = size.height - paddingBottom,
          cornerRadius = radius,
        )
      )
    }
  }
}
