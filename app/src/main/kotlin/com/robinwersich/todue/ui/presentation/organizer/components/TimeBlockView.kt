package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.robinwersich.todue.domain.model.TimeBlock
import com.robinwersich.todue.domain.model.Week
import com.robinwersich.todue.ui.presentation.organizer.FocusLevel
import com.robinwersich.todue.ui.presentation.organizer.TaskViewState
import com.robinwersich.todue.ui.presentation.organizer.formatting.TimeBlockFormatter
import com.robinwersich.todue.ui.presentation.organizer.formatting.rememberTimeBlockFormatter
import com.robinwersich.todue.ui.theme.ToDueTheme
import com.robinwersich.todue.utility.mapIndexedToImmutableList

@Composable
fun TimeBlockView(
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  @FloatRange(from = 0.0, to = 1.0) expandProgress: () -> Float,
  modifier: Modifier = Modifier,
) {
  val surfaceColor =
    lerp(MaterialTheme.colorScheme.surfaceContainer, Color.Transparent, expandProgress())
  val cornerRadius = lerp(24.dp, 0.dp, expandProgress())
  Surface(
    shape = RoundedCornerShape(cornerRadius),
    color = surfaceColor,
    modifier = modifier.padding(4.dp),
  ) {
    val clampedProgress = expandProgress().coerceIn(0f, 1f)
    if (clampedProgress < 1f) {
      CollapsedTimeBlockContent(
        timeBlock = timeBlock,
        formatter = formatter,
        modifier = Modifier.graphicsLayer { alpha = 1 - clampedProgress },
      )
    }
    if (clampedProgress > 0f) {
      ExpandedTimeBlockView(
        timeBlock = timeBlock,
        formatter = formatter,
        modifier = Modifier.graphicsLayer { alpha = clampedProgress },
      )
    }
  }
}

@Composable
private fun CollapsedTimeBlockContent(
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
) {
  Box(modifier.padding(4.dp)) {
    Text(
      formatter.format(timeBlock, useNarrowFormatting = true),
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.Center),
    )
  }
}

@Composable
private fun ExpandedTimeBlockView(
  timeBlock: TimeBlock,
  formatter: TimeBlockFormatter,
  modifier: Modifier = Modifier,
) {
  val tasks =
    listOf("Task 1", "Task 2", "Task 3").mapIndexedToImmutableList { id, text ->
      TaskViewState(
        id = id.toLong(),
        text = text,
        focusLevel = if (id == 1) FocusLevel.FOCUSSED else FocusLevel.BACKGROUND,
      )
    }
  Column(modifier) {
    Text(
      formatter.format(timeBlock),
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(8.dp),
    )
    TaskList(tasks, modifier = modifier)
  }
}

@Preview(showBackground = true)
@Composable
private fun ExpandedTimeBlockViewPreview() {
  ToDueTheme {
    TimeBlockView(Week(), rememberTimeBlockFormatter(), { 1f }, modifier = Modifier.fillMaxSize())
  }
}

@Preview
@Composable
private fun CollapsedTimeBlockViewPreview() {
  ToDueTheme {
    TimeBlockView(Week(), rememberTimeBlockFormatter(), { 0f }, modifier = Modifier.size(150.dp))
  }
}
