package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockContent
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockLabel
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.theme.ToDueTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun OrganizerScreen(state: OrganizerState, onEvent: (OrganizerEvent) -> Unit = {}) {
  val timelines = remember {
    persistentListOf(
      Timeline(0, TimeUnit.DAY),
      Timeline(1, TimeUnit.WEEK),
      Timeline(2, TimeUnit.MONTH),
    )
  }
  val formatter = rememberTimeBlockFormatter()
  OrganizerNavigation(
    timelines = timelines,
    taskBlockLabel = { _, timeBlock, padding ->
      TaskBlockLabel(
        timeBlock = timeBlock,
        formatter = formatter,
        modifier = Modifier.padding(padding),
      )
    },
    taskBlockContent = { _, timeBlock, padding ->
      TaskBlockContent(
        timeBlock = timeBlock,
        formatter = formatter,
        modifier = Modifier.padding(padding),
      )
    },
  )
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(OrganizerState()) }
}
