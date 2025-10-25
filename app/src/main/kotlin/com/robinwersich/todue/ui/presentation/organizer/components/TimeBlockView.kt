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
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Week
import com.robinwersich.todue.ui.presentation.organizer.formatting.TimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.state.FocusLevel
import com.robinwersich.todue.ui.presentation.organizer.state.TaskViewState
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList
import kotlinx.collections.immutable.ImmutableList

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
  tasks: ImmutableList<TaskViewState>,
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      formatter.format(timeBlock, useNarrowFormatting = false),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(8.dp),
    )
    tasks.forEach { TaskView(it) }
  }
}

@Preview(showBackground = true)
@Composable
fun ExpandedTimeBlockViewPreview() {
  val tasks =
    listOf("Task 1", "Task 2", "Task 3").mapIndexedToImmutableList { id, text ->
      TaskViewState(id = id.toLong(), text = text, focusLevel = FocusLevel.NEUTRAL)
    }
  ToDueTheme {
    TaskBlockContent(tasks, Week(), rememberTimeBlockFormatter(), modifier = Modifier.fillMaxSize())
  }
}
