package com.robinwersich.todue.ui.presentation.organizer.snapping

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPositionInLayout
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Enables a pager-like flinging but allows scrolling inside pages larger than the view port.
 *
 * @param lazyListState The [LazyListState] with information about the current state of the list.
 * @param decayAnimationSpec The [DecayAnimationSpec] used for flings.
 * @param minOverFlingVelocity The remaining velocity needed to snap to the next page after flinging
 *   to the end of an item.
 * @param smallItemSnapPosition The position for placing items not filling the whole view port.
 */
@OptIn(ExperimentalFoundationApi::class)
class ScrollableItemSnapLayoutInfoProvider(
  private val lazyListState: LazyListState,
  private val decayAnimationSpec: DecayAnimationSpec<Float>,
  private val minOverFlingVelocity: Dp = MinOverFlingVelocity,
  private val smallItemSnapPosition: SnapPositionInLayout = SnapPositionInLayout.CenterToCenter,
) : SnapLayoutInfoProvider {
  private val layoutInfo: LazyListLayoutInfo
    get() = lazyListState.layoutInfo

  /** Flings until the end of the current item. */
  override fun Density.calculateApproachOffset(initialVelocity: Float): Float {
    val currentOffset = 0f
    val targetOffset = decayAnimationSpec.calculateTargetValue(currentOffset, initialVelocity)
    val currentRange =
      calculateSnapOffsetRanges().minByOrNull { abs(it.snapOffset) } ?: SnapRange(0f, 0f)
    return if (currentOffset in currentRange) targetOffset.coerceIn(currentRange) else 0f
  }

  /** See [calculateFinalSnapOffset]. */
  override fun Density.calculateSnappingOffset(currentVelocity: Float): Float =
    calculateFinalSnapOffset(currentVelocity, calculateSnapOffsetRanges())

  private fun Density.calculateSnapOffsetRanges(): List<SnapRange> {
    val paddedViewPortSize =
      with(layoutInfo) {
        (if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width) -
          (beforeContentPadding + afterContentPadding)
      }

    return layoutInfo.visibleItemsInfo.map { item ->
      calculateItemSnapRange(
        paddedViewPortSize = paddedViewPortSize,
        itemSize = item.size,
        itemOffset = item.offset,
        itemIndex = item.index,
        smallItemSnapPosition = smallItemSnapPosition
      )
    }
  }

  private fun Density.calculateItemSnapRange(
    paddedViewPortSize: Int,
    itemSize: Int,
    itemOffset: Int,
    itemIndex: Int,
    smallItemSnapPosition: SnapPositionInLayout
  ): SnapRange {
    return if (itemSize < paddedViewPortSize) {
      val desiredOffset =
        with(smallItemSnapPosition) { position(paddedViewPortSize, itemSize, itemIndex) }
      val snapOffset = (itemOffset - desiredOffset).toFloat()
      SnapRange(snapOffset, snapOffset)
    } else {
      SnapRange(itemOffset.toFloat(), (itemSize + itemOffset - paddedViewPortSize).toFloat())
    }
  }

  /**
   * Snap to the nearest offset in scroll direction. If we already are in a settled position and
   * [velocity] is still higher than [minOverFlingVelocity], snap to the next [SnapRange] in scroll
   * direction.
   *
   * @returns The chosen offset for snapping.
   */
  private fun Density.calculateFinalSnapOffset(
    velocity: Float,
    snapRanges: List<SnapRange>
  ): Float {
    val velocityThreshold = minOverFlingVelocity.toPx().absoluteValue
    val snapOffsets = snapRanges.map(SnapRange::snapOffset)

    var snapOffset =
      if (velocity > velocityThreshold) {
        snapOffsets.filter { it > 0 }.minOrNull()
      } else if (velocity < -velocityThreshold) {
        snapOffsets.filter { it < 0 }.maxOrNull()
      } else null

    snapOffset =
      snapOffset
        ?: when (velocity.sign) {
          0f -> snapOffsets.minByOrNull { it.absoluteValue }
          1f -> snapOffsets.filter { it >= 0 }.minOrNull()
          -1f -> snapOffsets.filter { it <= 0 }.maxOrNull()
          else -> null
        }
    return snapOffset ?: 0f
  }

  /** Snap step size of zero to always allow decay animation. */
  override fun Density.calculateSnapStepSize(): Float = 0f
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberScrollableItemSnapFlingBehavior(
  lazyListState: LazyListState,
  lowVelocityAnimationSpec: AnimationSpec<Float> = tween(easing = LinearEasing),
  highVelocityAnimationSpec: DecayAnimationSpec<Float> = rememberSplineBasedDecay(),
  snapAnimationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
  smallItemSnapPosition: SnapPositionInLayout = SnapPositionInLayout.CenterToCenter,
  minFlingVelocity: Dp = MinFlingVelocity,
  minOverFlingVelocity: Dp = MinOverFlingVelocity,
): SnapFlingBehavior {
  val density = LocalDensity.current
  return remember(
    lazyListState,
    lowVelocityAnimationSpec,
    highVelocityAnimationSpec,
    snapAnimationSpec,
    smallItemSnapPosition,
    minFlingVelocity,
    minOverFlingVelocity,
    density
  ) {
    val snapLayoutInfoProvider =
      ScrollableItemSnapLayoutInfoProvider(
        lazyListState = lazyListState,
        decayAnimationSpec = highVelocityAnimationSpec,
        minOverFlingVelocity = minOverFlingVelocity,
        smallItemSnapPosition = smallItemSnapPosition
      )
    SnapFlingBehavior(
      snapLayoutInfoProvider = snapLayoutInfoProvider,
      lowVelocityAnimationSpec = lowVelocityAnimationSpec,
      highVelocityAnimationSpec = highVelocityAnimationSpec,
      snapAnimationSpec = snapAnimationSpec,
      density = density,
      shortSnapVelocityThreshold = minFlingVelocity
    )
  }
}

internal val MinFlingVelocity = 400.dp
internal val MinOverFlingVelocity = 6000.dp
