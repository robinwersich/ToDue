package com.robinwersich.todue.ui.composeextensions

import androidx.compose.runtime.State
import kotlin.reflect.KProperty1

fun <T, V> State<T>.delegate(property: KProperty1<T, V>) = object : State<V> {
  override val value: V
    get() = property.get(this@delegate.value)
}
