package com.robinwersich.todue.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
@Composable
fun <T> CachedUpdate(
  value: T,
  onValueChanged: (T) -> Unit,
  debounceMillis: Long = 1000,
  emitUpdates: Boolean = true,
  content: @Composable (MutableState<T>) -> Unit,
) {
  val cached = remember { mutableStateOf(value) }
  cached.value = value
  if (emitUpdates) {
    LaunchedEffect(onValueChanged, debounceMillis) {
      snapshotFlow { cached.value }.debounce(debounceMillis).onEach(onValueChanged).collect()
    }
  }
  content(cached)
}
