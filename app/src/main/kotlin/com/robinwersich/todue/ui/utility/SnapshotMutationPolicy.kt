package com.robinwersich.todue.ui.utility

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
