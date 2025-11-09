package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.robinwersich.todue.domain.model.Day
import com.robinwersich.todue.domain.model.Task
import com.robinwersich.todue.domain.model.TaskBlock
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.TimelineBlock
import com.robinwersich.todue.domain.model.Week
import com.robinwersich.todue.ui.presentation.organizer.OrganizerEvent
import com.robinwersich.todue.ui.presentation.organizer.formatting.TimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@Composable
fun TaskBlockLabel(
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
) {
  Box(modifier, contentAlignment = Alignment.Center) {
    Text(formatter.format(timeBlock, useNarrowFormatting = true), textAlign = TextAlign.Center)
  }
}

@Composable
fun TaskBlockContent(
  taskBlock: TaskBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
  onEvent: (OrganizerEvent) -> Unit = {},
) {
  Column(modifier) {
    Text(
      formatter.format(taskBlock.timeBlock, useNarrowFormatting = false),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(8.dp),
    )
    TaskList(taskBlock.tasks, onEvent = onEvent, modifier = Modifier.fillMaxSize())
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
      TaskBlock(TimelineBlock(0, Week()), tasks),
      rememberTimeBlockFormatter(),
      modifier = Modifier.fillMaxSize(),
    )
  }
}
