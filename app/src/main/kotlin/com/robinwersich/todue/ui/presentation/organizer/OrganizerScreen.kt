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
import java.time.LocalDate
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
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

private val previewTimelines =
  listOf(
    Timeline(0, TimeUnit.DAY),
    Timeline(1, TimeUnit.WEEK),
    Timeline(2, TimeUnit.MONTH),
  )

private val previewDate = LocalDate.of(2026, 7, 25)

private fun sampleTaskBlock(timelineBlock: TimelineBlock) =
  TaskBlock(
    timelineBlock,
    tasks =
      listOf(
        Task(
          id = 1,
          text = "Buy groceries",
          scheduledBlock = timelineBlock,
          dueDate = timelineBlock.section.endInclusive,
        ),
        Task(
          id = 2,
          text = "Clean house",
          scheduledBlock = timelineBlock,
          dueDate = timelineBlock.section.endInclusive,
        ),
      ),
  )

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenPreview() {
  ToDueTheme { OrganizerScreen(NavigationState(), { TaskBlock(it) }) }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenSplitViewPreview() {
  val navigationState =
    NavigationState(
      timelines = previewTimelines,
      initialTimeline = previewTimelines[1],
      initialShowChild = true,
      initialDate = previewDate,
    )
  ToDueTheme {
    OrganizerScreen(navigationState = navigationState, getTaskBlock = ::sampleTaskBlock)
  }
}

@Preview(showSystemUi = true)
@Composable
private fun OrganizerScreenMonthPreview() {
  val navigationState =
    NavigationState(
      timelines = previewTimelines,
      initialTimeline = previewTimelines[2],
      initialDate = previewDate,
      initialShowChild = true,
    )
  ToDueTheme {
    OrganizerScreen(navigationState = navigationState, getTaskBlock = ::sampleTaskBlock)
  }
}
