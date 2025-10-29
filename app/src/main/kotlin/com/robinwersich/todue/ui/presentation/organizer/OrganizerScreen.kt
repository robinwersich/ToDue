package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockContent
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockLabel
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.presentation.organizer.state.TaskBlockViewState
import com.robinwersich.todue.ui.theme.ToDueTheme

@Composable
fun OrganizerScreen(
  navigationState: NavigationState,
  getTaskBlockViewState: (TimelineBlock) -> TaskBlockViewState,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      val color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
      Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars).background(color))
    },
    floatingActionButton = {
      AnimatedVisibility(!navigationState.isSplitView, enter = scaleIn(), exit = scaleOut()) {
        FloatingActionButton(onClick = { onEvent(AddTask(navigationState.currentTimelineBlock)) }) {
          Icon(painter = painterResource(R.drawable.add), contentDescription = null)
        }
      }
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
          viewState = getTaskBlockViewState(timelineBlock),
          formatter = formatter,
          onEvent = onEvent,
          modifier = Modifier.padding(padding),
        )
      },
    )
  }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(NavigationState(), { TaskBlockViewState(it) }) }
}
