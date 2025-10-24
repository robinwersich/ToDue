package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.theme.ToDueTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun OrganizerScreen(
  state: OrganizerState,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      val color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
      Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(color))
    },
  ) { scaffoldPadding ->
    val timelines = remember {
      persistentListOf(
        Timeline(TimeUnit.DAY),
        Timeline(TimeUnit.WEEK),
        Timeline(TimeUnit.MONTH),
      )
    }
    val navigationState = remember { NavigationState(timelines) }
    val formatter = rememberTimeBlockFormatter()
    OrganizerNavigation(
      navigationState = navigationState,
      contentPadding = scaffoldPadding,
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
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(OrganizerState()) }
}
