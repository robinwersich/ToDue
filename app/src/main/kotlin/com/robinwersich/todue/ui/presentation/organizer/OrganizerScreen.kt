package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TimeBlockView
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.theme.ToDueTheme
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrganizerScreen(state: OrganizerState, onEvent: (OrganizerEvent) -> Unit = {}) {
  val timelines = persistentListOf(
    Timeline(0, TimeUnit.DAY),
    Timeline(1, TimeUnit.WEEK),
    Timeline(2, TimeUnit.MONTH),
  )
  val formatter = rememberTimeBlockFormatter()
  val backgroundColor = MaterialTheme.colorScheme.background
  OrganizerNavigation(
    timelines = timelines,
    modifier = Modifier.background(backgroundColor)
  ) { _, timeBlock ->
    TimeBlockView(timeBlock = timeBlock, formatter = formatter, expandProgress = { 0f })
  }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(OrganizerState()) }
}
