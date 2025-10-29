package com.robinwersich.todue.utility

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList

inline fun <T> buildImmutableList(
  builderAction: PersistentList.Builder<T>.() -> Unit
): PersistentList<T> = persistentListOf<T>().builder().apply(builderAction).build()

fun <K, V> Map<K, V>.toImmutableList(): PersistentList<Pair<K, V>> = buildImmutableList {
  this@toImmutableList.forEach { add(it.toPair()) }
}

inline fun <T, R> Iterable<T>.mapToImmutableList(transform: (T) -> R): PersistentList<R> =
  mapTo(persistentListOf<R>().builder(), transform).build()

inline fun <T, R> Iterable<T>.mapIndexedToImmutableList(
  transform: (Int, T) -> R
): PersistentList<R> = mapIndexedTo(persistentListOf<R>().builder(), transform).build()

inline fun <T, K> Iterable<T>.groupByToImmutable(
  keySelector: (T) -> K
): PersistentMap<K, PersistentList<T>> =
  this.groupBy(keySelector)
    .mapValuesTo(persistentMapOf<K, PersistentList<T>>().builder()) { it.value.toPersistentList() }
    .build()

operator fun <T> Pair<T, T>.contains(element: T): Boolean = first == element || second == element

fun <T> Pair<T, T>.containsAny(vararg elements: T): Boolean = elements.any { it in this }

inline fun <T> Pair<T, T>.find(predicate: (T) -> Boolean): T? =
  when {
    predicate(first) -> first
    predicate(second) -> second
    else -> null
  }

inline fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> =
  transform(first) to transform(second)

val <T> Pair<T, T>.areSame
  get() = first == second

inline fun <T> Pair<T, T>.forEach(action: (T) -> Unit) {
  action(first)
  action(second)
}

inline fun <T> Pair<T, T>.forEachDistinct(action: (T) -> Unit) {
  action(first)
  if (first != second) action(second)
}
