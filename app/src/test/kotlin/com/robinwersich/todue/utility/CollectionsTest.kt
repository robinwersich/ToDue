package com.robinwersich.todue.utility

import kotlinx.collections.immutable.ImmutableList
import org.junit.Assert.*
import org.junit.Test

class CollectionsTest {
  @Test
  fun `toImmutableList creates list with same entries`() {
    val expectedList = listOf(1 to "one", 2 to "two", 3 to "three")
    val map = expectedList.toMap()
    val list: ImmutableList<Pair<Int, String>> = map.toImmutableList()
    assertEquals(expectedList, list)
  }

  @Test
  fun `mapToImmutableList with identity creates list with same entries`() {
    val expectedList = listOf(1, 2, 3)
    val list: ImmutableList<Int> = expectedList.mapToImmutableList { it }
    assertEquals(expectedList, list)
  }
}
