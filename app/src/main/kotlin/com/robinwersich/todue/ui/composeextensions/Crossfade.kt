package com.robinwersich.todue.ui.composeextensions

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.robinwersich.todue.utility.forEach

@Composable
fun <T> SwipeableTransition<T>.Crossfade(
  modifier: Modifier = Modifier,
  contentKey: (T) -> Any? = { it },
  content: @Composable (targetState: T) -> Unit,
) {
  val states = transitionStates()
  Box(modifier, propagateMinConstraints = true) {
    if (contentKey(states.first) == contentKey(states.second)) {
      content(states.first)
    } else {
      states.forEach { state ->
        Box(
          Modifier.graphicsLayer {
            alpha = interpolateValue(::lerp) { it: T -> if (it == state) 1f else 0f }
          },
          propagateMinConstraints = true,
        ) {
          content(state)
        }
      }
    }
  }
}
