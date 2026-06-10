package com.robinwersich.todue.ui.presentation.organizer

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import com.robinwersich.todue.R
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TaskBlockMainData
import com.robinwersich.todue.domain.model.TaskBlockPreviewData
import com.robinwersich.todue.domain.model.TimeUnit
import com.robinwersich.todue.domain.model.Timeline
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.ui.presentation.organizer.components.OrganizerNavigation
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockContent
import com.robinwersich.todue.ui.presentation.organizer.components.TaskBlockFillOverlay
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
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
        },
        expandedHeight = 36.dp,
        colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
    },
    floatingActionButton = {
      AnimatedVisibility(!navigationState.isSplitView, enter = scaleIn(), exit = scaleOut()) {
        FloatingActionButton(
          onClick = { onEvent(OrganizerEvent.AddTask(navigationState.currentTimelineBlock)) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
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
        Box(Modifier.padding(padding), propagateMinConstraints = true) {
          TaskBlockFillOverlay(taskBlock = getTaskBlock(timelineBlock))
          TaskBlockLabel(
            timeBlock = timelineBlock.section,
            formatter = formatter,
          )
        }
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

private val previewDate = LocalDate.now()

private fun sampleTaskBlock(timelineBlock: TimelineBlock): TaskBlock {
  return TaskBlock(
    timelineBlock,
    mainData =
      TaskBlockMainData(
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
          )
      ),
    previewData = TaskBlockPreviewData(totalEstimatedDuration = Random.nextInt(4).hours),
  )
}

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

@Preview(showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
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
