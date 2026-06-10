package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.times
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.Week
import com.robinwersich.todue.domain.model.numberOfDays
import com.robinwersich.todue.ui.presentation.organizer.OrganizerEvent
import com.robinwersich.todue.ui.presentation.organizer.formatting.TimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.formatting.TimeBlockLabelComponent
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.theme.BlockLabelSubtitleStyle
import com.robinwersich.todue.ui.theme.BlockLabelTitleStyle
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@Composable
fun TaskBlockLabel(
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
) {
  val components = formatter.formatLabel(timeBlock)
  val subtitleColor = LocalContentColor.current.copy(alpha = 0.65f)

  Box(modifier, contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      for (component in components) {
        when (component) {
          is TimeBlockLabelComponent.Title ->
            Text(component.text, style = BlockLabelTitleStyle, textAlign = TextAlign.Center)
          is TimeBlockLabelComponent.Subtitle ->
            Text(
              component.text,
              style = BlockLabelSubtitleStyle,
              color = subtitleColor,
              textAlign = TextAlign.Center,
            )
          is TimeBlockLabelComponent.Divider ->
            HorizontalDivider(
              Modifier.width(32.dp).padding(vertical = 2.dp),
              thickness = 1.dp,
              color = subtitleColor,
            )
        }
      }
    }
  }
}

@Composable
fun TaskBlockFillOverlay(
  taskBlock: TaskBlock,
  modifier: Modifier = Modifier,
) {
  val estimatedTaskDuration = taskBlock.previewData?.totalEstimatedDuration ?: Duration.ZERO
  val availableDuration = (taskBlock.timeBlock.numberOfDays.toInt() * 8.hours)
  val fillFraction = (estimatedTaskDuration / availableDuration).toFloat()

  Box(modifier) {
    Box(
      Modifier.fillMaxWidth()
        .fillMaxHeight(fillFraction)
        .align(Alignment.BottomCenter)
        .background(LocalContentColor.current.copy(alpha = 0.1f))
    )
  }
}

@Composable
fun TaskBlockContent(
  taskBlock: TaskBlock,
  mode: TaskBlockContentMode,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  val heading = formatter.formatHeading(taskBlock.timeBlock)
  val headingStyle = MaterialTheme.typography.titleLarge

  Column(modifier) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
      Text(heading.title, style = headingStyle)
      heading.subtitle?.let {
        Text(
          heading.subtitle,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    if (taskBlock.mainData != null) {
      TaskList(
        taskBlock.mainData.tasks,
        mode = mode,
        onEvent = onEvent,
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ExpandedTimeBlockViewPreview() {
  val tasks =
    listOf("Task 1", "Task 2", "Task 3").mapIndexedToImmutableList() { id, text ->
      Task(
        id = id.toLong(),
        text = text,
        scheduledBlock = TimelineBlock(0, Day()),
        dueDate = LocalDate.now(),
      )
    }
  ToDueTheme {
    TaskBlockContent(
      TaskBlock.main(TimelineBlock(0, Week()), tasks),
      TaskBlockContentMode.FULLSCREEN,
      rememberTimeBlockFormatter(),
      modifier = Modifier.fillMaxSize(),
    )
  }
}
