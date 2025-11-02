package com.robinwersich.todue.ui.composeextensions

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Velocity
import kotlin.math.nextDown
import kotlin.math.nextUp

@Composable
fun <T> AnchoredDraggableState<T>.rememberSwipeableTransition() =
  remember(this) { toSwipeableTransition() }

/**
 * Creates a [SwipeableTransition] controlled by this [AnchoredDraggableState]. When used in a
 * composable, [rememberSwipeableTransition] should be used instead to ensure that the same
 * transition is used across recompositions.
 */
fun <T> AnchoredDraggableState<T>.toSwipeableTransition(): SwipeableTransition<T> {
  val transitionStates by
    derivedStateOf(pairReferentialEqualityPolicy()) { getAdjacentToOffsetAnchors() }
  return SwipeableTransition(
    transitionStates = { transitionStates },
    progress = {
      val (prevAnchor, nextAnchor) = transitionStates
      progress(prevAnchor, nextAnchor)
    },
  )
}

/**
 * Returns the two anchors adjacent to the current [offset][AnchoredDraggableState.offset]. If the
 * draggable is settled or is not initialized, the same anchor is returned twice. The returned
 * anchors are sorted by their position.
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToOffsetAnchors(): Pair<T, T> {
  if (offset.isNaN() || anchors.size == 0 || anchors.positionOf(settledValue) == offset) {
    return Pair(settledValue, settledValue)
  } else {
    // closest anchors cannot be null as anchors are not empty
    val prevAnchor = anchors.closestAnchor(offset, searchUpwards = false)!!
    val nextAnchor = anchors.closestAnchor(offset, searchUpwards = true)!!
    return Pair(prevAnchor, nextAnchor)
  }
}

/**
 * Returns the adjacent anchors to the current [current value][AnchoredDraggableState.currentValue].
 *
 * @see getAdjacentAnchors
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToCurrentAnchors() = getAdjacentAnchors(currentValue)

/**
 * Returns the adjacent anchors to the current [settled value][AnchoredDraggableState.settledValue].
 *
 * @see getAdjacentAnchors
 */
fun <T> AnchoredDraggableState<T>.getAdjacentToSettledAnchors() = getAdjacentAnchors(settledValue)

/**
 * Returns the closest anchor with a smaller offset and the closest anchor with a larger offset than
 * the given [anchor]. If there is no such, the given [anchor] will be returned for the
 * corresponding bound. Consequently, for uninitialized anchors, the given [anchor] will be returned
 * twice.
 */
fun <T> AnchoredDraggableState<T>.getAdjacentAnchors(anchor: T): Pair<T, T> {
  val prevAnchor = anchors.previousAnchor(anchor) ?: anchor
  val nextAnchor = anchors.nextAnchor(anchor) ?: anchor
  return Pair(prevAnchor, nextAnchor)
}

/** Returns the anchor before (in terms of offset) the given [anchor] or null if there is none */
fun <T> DraggableAnchors<T>.previousAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  // closestAnchor(searchUpwards = false) may return larger anchor if there are no smaller anchors
  return closestAnchor(anchorOffset.nextDown(), searchUpwards = false)?.takeIf {
    positionOf(it) < anchorOffset
  }
}

/** Returns the anchor after (in terms of offset) the given [anchor]. */
fun <T> DraggableAnchors<T>.nextAnchor(anchor: T): T? {
  val anchorOffset = positionOf(anchor)
  if (anchorOffset.isNaN()) return null
  // closestAnchor(searchUpwards = true) may return smaller anchor if there are no larger anchors
  return closestAnchor(anchorOffset.nextUp(), searchUpwards = true)?.takeIf {
    positionOf(it) > anchorOffset
  }
}

/**
 * Distance between [current anchor][AnchoredDraggableState.currentValue] and current offset. If the
 * anchors are not initialized yet, this will always return 0.
 */
val AnchoredDraggableState<*>.offsetToCurrent: Float
  get() = if (offset.isNaN()) 0f else offset - anchors.positionOf(currentValue)

/**
 * Distance between [settled anchor][AnchoredDraggableState.settledValue] and current offset. If the
 * anchors are not initialized yet, this will always return 0.
 */
val AnchoredDraggableState<*>.offsetToSettled: Float
  get() = if (offset.isNaN()) 0f else offset - anchors.positionOf(settledValue)

/**
 * Returns whether the [AnchoredDraggableState] is currently settled at an anchor. If the anchors
 * are not initialized yet, this will always return true.
 */
val AnchoredDraggableState<*>.isSettled: Boolean
  get() = offset.isNaN() || anchors.positionOf(settledValue) == offset

/**
 * Workaround for applying overscroll in the original direction if reverseDirection is true on the
 * `anchoredDraggable` modifier.
 */
fun OverscrollEffect.reversed() =
  object : OverscrollEffect by this {
    override fun applyToScroll(
      delta: Offset,
      source: NestedScrollSource,
      performScroll: (Offset) -> Offset,
    ) = this@reversed.applyToScroll(-delta, source, { -performScroll(-it) })

    override suspend fun applyToFling(
      velocity: Velocity,
      performFling: suspend (Velocity) -> Velocity,
    ) = this@reversed.applyToFling(-velocity, { -performFling(-it) })
  }

/**
 * Modifier to allow an anchoredDraggable to consume deltas from the nested scroll hierarchy.
 *
 * @param state The [AnchoredDraggableState] that should be controlled by the nested scroll.
 * @param orientation The orientation of the drag, matching the one used in `anchoredDraggable`.
 */
fun Modifier.anchoredDraggableWithNestedScroll(
  state: AnchoredDraggableState<*>,
  reverseDirection: Boolean,
  orientation: Orientation,
  enabled: Boolean = true,
  interactionSource: MutableInteractionSource? = null,
  overscrollEffect: OverscrollEffect? = null,
  flingBehavior: FlingBehavior? = null,
): Modifier =
  this.then(
      AnchoredDraggableWithNestedScrollElement(
        state = state,
        reverseDirection = reverseDirection,
        orientation = orientation,
        enabled = enabled,
      )
    )
    .anchoredDraggable(
      state = state,
      reverseDirection = reverseDirection,
      orientation = orientation,
      enabled = enabled,
      interactionSource = interactionSource,
      overscrollEffect = overscrollEffect,
      flingBehavior = flingBehavior,
    )

private data class AnchoredDraggableWithNestedScrollElement<T>(
  val state: AnchoredDraggableState<T>,
  val reverseDirection: Boolean,
  val orientation: Orientation,
  val enabled: Boolean,
) : ModifierNodeElement<AnchoredDraggableWithNestedScrollNode<T>>() {
  override fun create() =
    AnchoredDraggableWithNestedScrollNode(state, reverseDirection, orientation, enabled)

  override fun update(node: AnchoredDraggableWithNestedScrollNode<T>) {
    node.nestedScrollConnection.state = state
    node.nestedScrollConnection.reverseDirection = reverseDirection
    node.nestedScrollConnection.orientation = orientation
    node.nestedScrollConnection.enabled = enabled
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "anchoredDraggableWithNestedScroll"
    properties["state"] = state
    properties["reverseDirection"] = reverseDirection
    properties["orientation"] = orientation
    properties["enabled"] = enabled
  }
}

private class AnchoredDraggableWithNestedScrollNode<T>(
  state: AnchoredDraggableState<T>,
  reverseDirection: Boolean,
  orientation: Orientation,
  enabled: Boolean,
) : DelegatingNode() {
  val nestedScrollConnection =
    AnchoredDraggableNestedScrollConnection(state, reverseDirection, orientation, enabled)

  init {
    delegate(nestedScrollModifierNode(nestedScrollConnection, null))
  }
}

class AnchoredDraggableNestedScrollConnection<T>(
  var state: AnchoredDraggableState<T>,
  var reverseDirection: Boolean,
  var orientation: Orientation,
  var enabled: Boolean,
) : NestedScrollConnection {
  override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
    // draggable should only be moved by direct user interaction
    if (!enabled || source != NestedScrollSource.UserInput) return Offset.Zero
    // if settled, let child scroll first and only consume excess delta
    if (state.isSettled) return Offset.Zero
    // if not settled, scroll back draggable to settled before scrolling child
    val adjustedAvailable = available.toFloat().reverseIfNeeded()
    val (prevAnchor, nextAnchor) = state.getAdjacentToOffsetAnchors()
    val delta =
      adjustedAvailable.coerceIn(
        state.anchors.positionOf(prevAnchor) - state.offset,
        state.anchors.positionOf(nextAnchor) - state.offset,
      )
    return state.dispatchRawDelta(delta).reverseIfNeeded().toOffset()
  }

  override suspend fun onPreFling(available: Velocity): Velocity {
    // if settled, fling child
    if (!enabled || state.isSettled) return Velocity.Zero
    // if not settled, fling draggable to next anchor and don't fling child
    return state.settle(available.toFloat().reverseIfNeeded()).reverseIfNeeded().toVelocity()
  }

  override fun onPostScroll(
    consumed: Offset,
    available: Offset,
    source: NestedScrollSource,
  ): Offset {
    // Draggable should only be moved by direct user interaction
    if (!enabled || source != NestedScrollSource.UserInput) return Offset.Zero
    return state
      .dispatchRawDelta(available.toFloat().reverseIfNeeded())
      .reverseIfNeeded()
      .toOffset()
  }

  override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
    if (!enabled) return Velocity.Zero
    // excess velocity will be ignored if already settled, allowing overscroll effect in child
    return state.settle(available.toFloat().reverseIfNeeded()).reverseIfNeeded().toVelocity()
  }

  private fun Float.reverseIfNeeded() = if (reverseDirection) -this else this

  private fun Float.toOffset() =
    when (orientation) {
      Orientation.Horizontal -> Offset(this, 0f)
      Orientation.Vertical -> Offset(0f, this)
    }

  private fun Offset.toFloat() =
    when (orientation) {
      Orientation.Horizontal -> this.x
      Orientation.Vertical -> this.y
    }

  private fun Float.toVelocity() =
    when (orientation) {
      Orientation.Horizontal -> Velocity(this, 0f)
      Orientation.Vertical -> Velocity(0f, this)
    }

  private fun Velocity.toFloat() =
    when (orientation) {
      Orientation.Horizontal -> this.x
      Orientation.Vertical -> this.y
    }
}
