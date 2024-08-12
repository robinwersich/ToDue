package com.robinwersich.todue.utility

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/** Converts a map to an [ImmutableList] of [Pair]s. */
fun <K, V> Map<K, V>.toImmutableList(): ImmutableList<Pair<K, V>> =
  persistentListOf<Pair<K, V>>().mutate { list -> this.forEach { list.add(it.toPair()) } }

/** Maps the elements of this iterable to a new [ImmutableList]. */
fun <T, R> Iterable<T>.mapToImmutableList(transform: (T) -> R): ImmutableList<R> =
  mapTo(persistentListOf<R>().builder(), transform).build()

/** Maps the elements of this iterable to a new [ImmutableList], providing indices. */
fun <T, R> Iterable<T>.mapIndexedToImmutableList(transform: (Int, T) -> R): ImmutableList<R> =
  mapIndexedTo(persistentListOf<R>().builder(), transform).build()

/** Returns if the pair contains the [element]. */
operator fun <T> Pair<T, T>.contains(element: T): Boolean = first == element || second == element

/** Transforms the content of a pair. */
fun <T, R> Pair<T, T>.map(transform: (T) -> R): Pair<R, R> = transform(first) to transform(second)
