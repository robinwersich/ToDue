package com.robinwersich.todue.utility

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CollectionsTest {
  @Test
  fun `toImmutableList creates list with same entries`() {
    val entryList = listOf(1 to "one", 2 to "two", 3 to "three")
    assertThat(entryList.toMap().toImmutableList()).containsExactlyElementsIn(entryList)
  }

  @Test
  fun `mapToImmutableList maps values correctly`() {
    val elementList = listOf(1, 2, 3)
    assertThat(elementList.mapToImmutableList { it * 2 }).containsExactly(2, 4, 6).inOrder()
  }
}
