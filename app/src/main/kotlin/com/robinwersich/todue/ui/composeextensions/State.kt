package com.robinwersich.todue.ui.composeextensions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlin.reflect.KProperty1

fun <T, V> State<T>.delegate(property: KProperty1<T, V>) =
  object : State<V> {
    override val value: V
      get() = property.get(this@delegate.value)
  }

class MutablePeekableState<T>(initialValue: T) : MutableState<T> {
  private var _value = initialValue
  private val _state = mutableStateOf(initialValue)

  override var value: T
    get() = _state.value
    set(value) {
      _value = value
      _state.value = value
    }

  /** Read the state without causing a subscription to state changes. */
  fun peek(): T = _value

  override fun component1() = this.value

  override fun component2() = { value: T -> this.value = value }
}

fun <T> mutablePeekableStateOf(initialValue: T) = MutablePeekableState(initialValue)
