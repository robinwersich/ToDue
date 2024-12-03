package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class LayoutTest {
  @get:Rule val rule = createComposeRule()

  @Test
  fun signedPadding_isApplied() {
    rule.setContent { Box(Modifier.size(100.dp).signedPadding(top = (-10).dp).testTag("box")) }
    rule.onNodeWithTag("box").apply {
      assertHeightIsEqualTo(110.dp)
      assertWidthIsEqualTo(100.dp)
      assertTopPositionInRootIsEqualTo((-10).dp)
      assertLeftPositionInRootIsEqualTo(0.dp)
    }
  }

  @Test
  fun dynamicPadding_isApplied() {
    rule.setContent {
      Box(
        Modifier.size(100.dp)
          .padding { PaddingValues(horizontal = 10.dp, vertical = 20.dp) }
          .testTag("box")
      )
    }
    rule.onNodeWithTag("box").apply {
      assertHeightIsEqualTo(60.dp)
      assertWidthIsEqualTo(80.dp)
      assertTopPositionInRootIsEqualTo(20.dp)
      assertLeftPositionInRootIsEqualTo(10.dp)
    }
  }
}
