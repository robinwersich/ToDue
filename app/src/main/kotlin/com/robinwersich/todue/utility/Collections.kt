package com.robinwersich.todue.utility

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

fun <T> buildImmutableList(builderAction: PersistentList.Builder<T>.() -> Unit): ImmutableList<T> =
  persistentListOf<T>().builder().apply(builderAction).build()

/** Converts a map to an [ImmutableList] of [Pair]s. */
fun <K, V> Map<K, V>.toImmutableList(): ImmutableList<Pair<K, V>> =
  buildImmutableList { this@toImmutableList.forEach { add(it.toPair()) } }

/** Maps the elements of this iterable to a new [ImmutableList]. */
fun <T, R> Iterable<T>.mapToImmutableList(transform: (T) -> R): ImmutableList<R> =
  mapTo(persistentListOf<R>().builder(), transform).build()

/** Maps the elements of this iterable to a new [ImmutableList], providing indices. */
fun <T, R> Iterable<T>.mapIndexedToImmutableList(transform: (Int, T) -> R): ImmutableList<R> =
  mapIndexedTo(persistentListOf<R>().builder(), transform).build()

/** Returns if the pair contains the [element]. */
operator fun <T> Pair<T, T>.contains(element: T): Boolean = first == element || second == element

/** Returns if any of the given [elements] is contained in the pair. */
fun <T> Pair<T, T>.containsAny(vararg elements: T): Boolean = elements.any { it in this }

/** Returns an element of the pair that matches the given [predicate] or null. */
inline fun <T> Pair<T, T>.find(predicate: (T) -> Boolean): T? =
  when {
    predicate(first) -> first
    predicate(second) -> second
    else -> null
  }

/** Transforms the content of a pair. */
inline fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> =
  transform(first) to transform(second)

/** If the two elements of the pair are equal to each other. */
val <T> Pair<T, T>.areSame
  get() = first == second

/** Performs the given [action] on each element of the pair. */
inline fun <T> Pair<T, T>.forEach(action: (T) -> Unit) {
  action(first)
  action(second)
}
