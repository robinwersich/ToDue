package com.robinwersich.todue.ui.composeextensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

/**
 * Propagates updates of a child composable at a reduced rate.
 *
 * @param value (Initial) value to be used by the [content].
 * @param onValueChanged Callback for updates to [value].
 * @param debounceTime Time after which value changes are propagated by calling [onValueChanged].
 * @param emitUpdates If true, updates emitted by the [content] are propagated. This should be false
 *   if no updates are expected to save resources.
 * @param content A composable reading and writing to the supplied [MutableState]
 */
@OptIn(FlowPreview::class)
@Composable
fun <T> DebouncedUpdate(
  value: T,
  onValueChanged: (T) -> Unit,
  debounceTime: Duration = 1000.milliseconds,
  emitUpdates: Boolean = true,
  content: @Composable (MutableState<T>) -> Unit,
) {
  val previous = remember { ValueReference(value) }
  SideEffect { previous.value = value }
  val internalState = remember { mutableStateOf(value) }
  if (value != previous.value) internalState.value = value

  if (emitUpdates) {
    LaunchedEffect(onValueChanged, debounceTime) {
      snapshotFlow { internalState.value }.debounce { debounceTime }.collect(onValueChanged)
    }
    DisposableEffect(onValueChanged) { onDispose { onValueChanged(internalState.value) } }
  }

  content(internalState)
}

class ValueReference<T>(var value: T)
