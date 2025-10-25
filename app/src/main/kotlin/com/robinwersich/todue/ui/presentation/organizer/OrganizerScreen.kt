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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockContent
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockLabel
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TaskViewState
import com.robinwersich.todue.ui.theme.ToDueTheme

@Composable
fun OrganizerScreen(
  navigationState: NavigationState,
  getTasks: (TimelineBlock) -> ImmutableList<TaskViewState>,
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
    val formatter = rememberTimeBlockFormatter()
    OrganizerNavigation(
      navigationState = navigationState,
      contentPadding = scaffoldPadding,
      taskBlockLabel = { timelineBlock, padding ->
        TaskBlockLabel(
          timeBlock = timelineBlock.section,
          formatter = formatter,
          modifier = Modifier.padding(padding),
        )
      },
      taskBlockContent = { timelineBlock, padding ->
        TaskBlockContent(
          tasks = getTasks(timelineBlock),
          timeBlock = timelineBlock.section,
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
  ToDueTheme { OrganizerScreen(NavigationState(), { persistentListOf() }) }
}
