package com.robinwersich.todue.ui.composeextensions

import androidx.compose.runtime.SnapshotMutationPolicy

/** Considers two pairs equal if their two references are equal. */
private data object PairReferentialEqualityPolicy : SnapshotMutationPolicy<Pair<Any?, Any?>> {
  override fun equivalent(a: Pair<Any?, Any?>, b: Pair<Any?, Any?>) =
    a.first === b.first && a.second === b.second
}

/** A policy that considers two pairs equal if their first and second elements are equal. */
@Suppress("UNCHECKED_CAST")
fun <T> pairReferentialEqualityPolicy() =
  PairReferentialEqualityPolicy as SnapshotMutationPolicy<Pair<T, T>>

/** Considers two triples equal if their references are equal. */
private data object TripleReferentialEqualityPolicy : SnapshotMutationPolicy<Triple<Any?, Any?, Any?>> {
  override fun equivalent(a: Triple<Any?, Any?, Any?>, b: Triple<Any?, Any?, Any?>) =
    a.first === b.first && a.second === b.second && a.third === b.third
}

/** A policy that considers two triples equal if all their elements are equal. */
@Suppress("UNCHECKED_CAST")
fun <T> tripleReferentialEqualityPolicy() =
  TripleReferentialEqualityPolicy as SnapshotMutationPolicy<Triple<T, T, T>>
