package com.robinwersich.todue.utility

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

/** Converts a map to an [ImmutableList] of [Pairs][Pair]. */
fun <K, V> Map<K, V>.toImmutableList(): ImmutableList<Pair<K, V>> =
  persistentListOf<Pair<K, V>>().mutate { list -> this.forEach { list.add(it.toPair()) } }

/** Maps the elements of this iterable to a new [ImmutableList]. */
fun <T, R> Iterable<T>.mapToImmutableList(transform: (T) -> R): ImmutableList<R> =
  mapTo(persistentListOf<R>().builder(), transform).build()
