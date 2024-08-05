package com.robinwersich.todue.ui.utility

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec

/** Stops the animation instantly. */
private object InstantStopDecaySpec : FloatDecayAnimationSpec {
  override val absVelocityThreshold: Float = 0.1f

  override fun getValueFromNanos(
    playTimeNanos: Long,
    initialValue: Float,
    initialVelocity: Float,
  ): Float {
    return initialValue
  }

  override fun getVelocityFromNanos(
    playTimeNanos: Long,
    initialValue: Float,
    initialVelocity: Float,
  ): Float {
    return 0f
  }

  override fun getDurationNanos(initialValue: Float, initialVelocity: Float): Long {
    return 0L
  }

  override fun getTargetValue(initialValue: Float, initialVelocity: Float): Float {
    return initialValue
  }
}

/** [DecayAnimationSpec] stopping the animation instantly. Useful to force snap animation. */
fun <T> instantStop(): DecayAnimationSpec<T> = InstantStopDecaySpec.generateDecayAnimationSpec()
