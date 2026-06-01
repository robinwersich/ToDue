package com.robinwersich.todue.ui.composeextensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

fun Modifier.drawShape(
  shape: Shape,
  backgroundColor: () -> Color,
  borderColor: () -> Color,
  borderWidth: () -> Dp,
): Modifier = drawBehind {
  val bgColor = backgroundColor()
  val bdColor = borderColor()
  val bdWidth = borderWidth().toPx()
  when (val outline = shape.createOutline(size, layoutDirection, this)) {
    is Outline.Rectangle -> drawRectangleShape(outline, bgColor, bdColor, bdWidth)
    is Outline.Rounded -> drawRoundedShape(outline, bgColor, bdColor, bdWidth)
    is Outline.Generic -> drawGenericShape(outline, bgColor, bdColor, bdWidth)
  }
}

private fun DrawScope.drawRectangleShape(
  outline: Outline.Rectangle,
  backgroundColor: Color,
  borderColor: Color,
  borderWidth: Float,
) {
  val rect = outline.rect
  val topLeft = Offset(rect.left, rect.top)
  val outlineSize = Size(rect.width, rect.height)
  drawRect(color = backgroundColor, topLeft = topLeft, size = outlineSize)
  if (borderWidth > 0f && borderColor.alpha > 0f) {
    drawRect(color = borderColor, topLeft = topLeft, size = outlineSize, style = Stroke(borderWidth))
  }
}

private fun DrawScope.drawRoundedShape(
  outline: Outline.Rounded,
  backgroundColor: Color,
  borderColor: Color,
  borderWidth: Float,
) {
  val roundRect = outline.roundRect
  val topLeft = Offset(roundRect.left, roundRect.top)
  val outlineSize = Size(roundRect.width, roundRect.height)
  drawRoundRect(
    color = backgroundColor,
    topLeft = topLeft,
    size = outlineSize,
    cornerRadius = roundRect.topLeftCornerRadius,
  )
  if (borderWidth > 0f && borderColor.alpha > 0f) {
    drawRoundRect(
      color = borderColor,
      topLeft = topLeft,
      size = outlineSize,
      cornerRadius = roundRect.topLeftCornerRadius,
      style = Stroke(borderWidth),
    )
  }
}

private fun DrawScope.drawGenericShape(
  outline: Outline.Generic,
  backgroundColor: Color,
  borderColor: Color,
  borderWidth: Float,
) {
  drawPath(path = outline.path, color = backgroundColor)
  if (borderWidth > 0f && borderColor.alpha > 0f) {
    drawPath(path = outline.path, color = borderColor, style = Stroke(borderWidth))
  }
}
