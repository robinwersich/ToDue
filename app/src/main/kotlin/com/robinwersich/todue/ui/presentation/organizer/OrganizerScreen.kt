package com.robinwersich.todue.ui.presentation.organizer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockContent
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockLabel
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.state.NavigationState
import com.robinwersich.todue.ui.theme.ToDueTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerScreen(
  navigationState: NavigationState,
  getTaskBlock: (TimelineBlock) -> TaskBlock,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    floatingActionButton = {
      AnimatedVisibility(!navigationState.isSplitView, enter = scaleIn(), exit = scaleOut()) {
        FloatingActionButton(
          onClick = { onEvent(OrganizerEvent.AddTask(navigationState.currentTimelineBlock)) }
        ) {
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
      taskBlockContent = { timelineBlock, mode, padding ->
        TaskBlockContent(
          taskBlock = getTaskBlock(timelineBlock),
          mode = mode,
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
  ToDueTheme { OrganizerScreen(NavigationState(), { TaskBlock(it) }) }
}
