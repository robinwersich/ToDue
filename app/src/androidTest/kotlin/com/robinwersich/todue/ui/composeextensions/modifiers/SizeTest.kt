package com.robinwersich.todue.ui.composeextensions.modifiers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class SizeTest {
  @get:Rule val rule = createComposeRule()

  @Test
  fun fillMinWidth() {
    rule.setContent { Box(Modifier.widthIn(50.dp, 100.dp).fillMinWidth().testTag("box")) }
    rule.onNodeWithTag("box").apply {
      assertWidthIsEqualTo(50.dp)
      assertLeftPositionInRootIsEqualTo(0.dp)
    }
  }

  @Test
  fun fillMinHeight() {
    rule.setContent { Box(Modifier.heightIn(50.dp, 100.dp).fillMinHeight().testTag("box")) }
    rule.onNodeWithTag("box").apply {
      assertHeightIsEqualTo(50.dp)
      assertTopPositionInRootIsEqualTo(0.dp)
    }
  }

  @Test
  fun fillMinSize() {
    rule.setContent {
      Box(Modifier.sizeIn(50.dp, 50.dp, 100.dp, 100.dp).fillMinSize().testTag("box"))
    }
    rule.onNodeWithTag("box").apply {
      assertWidthIsEqualTo(50.dp)
      assertHeightIsEqualTo(50.dp)
      assertPositionInRootIsEqualTo(0.dp, 0.dp)
    }
  }

  @Test
  fun wrapToMinWidth() {
    rule.setContent {
      Box(Modifier.testTag("outer")) {
        Box(Modifier.widthIn(50.dp, 100.dp).wrapToMinWidth().testTag("inner"))
      }
    }
    rule.onNodeWithTag("outer").apply {
      assertWidthIsEqualTo(50.dp)
      assertLeftPositionInRootIsEqualTo(0.dp)
    }
    rule.onNodeWithTag("inner").apply {
      assertWidthIsEqualTo(100.dp)
      assertLeftPositionInRootIsEqualTo((-25).dp)
    }
  }

  @Test
  fun wrapToMinHeight() {
    rule.setContent {
      Box(Modifier.testTag("outer")) {
        Box(Modifier.heightIn(50.dp, 100.dp).wrapToMinHeight().testTag("inner"))
      }
    }
    rule.onNodeWithTag("outer").apply {
      assertHeightIsEqualTo(50.dp)
      assertTopPositionInRootIsEqualTo(0.dp)
    }
    rule.onNodeWithTag("inner").apply {
      assertHeightIsEqualTo(100.dp)
      assertTopPositionInRootIsEqualTo((-25).dp)
    }
  }

  @Test
  fun wrapToMinSize() {
    rule.setContent {
      Box(Modifier.testTag("outer")) {
        Box(Modifier.sizeIn(50.dp, 50.dp, 100.dp, 100.dp).wrapToMinSize().testTag("inner"))
      }
    }
    rule.onNodeWithTag("outer").apply {
      assertWidthIsEqualTo(50.dp)
      assertHeightIsEqualTo(50.dp)
      assertPositionInRootIsEqualTo(0.dp, 0.dp)
    }
    rule.onNodeWithTag("inner").apply {
      assertWidthIsEqualTo(100.dp)
      assertHeightIsEqualTo(100.dp)
      assertPositionInRootIsEqualTo((-25).dp, (-25).dp)
    }
  }
}
