package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Duration
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import com.robinwersich.todue.R
import com.robinwersich.todue.ui.presentation.organizer.formatting.formatDuration
import com.robinwersich.todue.ui.theme.ToDueTheme

private const val TWO_PI = 2f * PI.toFloat()

private val DIAL_SIZE = 220.dp
private val TRACK_WIDTH = 24.dp
private val HANDLE_RADIUS = 8.dp

private class DurationPickerState(
  initialDuration: Duration,
  val maxRotations: Int = 4,
  val snapIntervalMinutes: Int = 5,
) {
  val maxMinutes: Long = maxRotations * 60L
  val minAngle: Float = (snapIntervalMinutes / 60f) * TWO_PI
  val maxAngle: Float = maxRotations * TWO_PI

  var rawAngle: Float by mutableFloatStateOf(durationToAngle(initialDuration))
  var lastHapticSnapMinutes: Int by mutableIntStateOf(snapToMinutes(rawAngle))

  val snappedDuration: Duration
    get() {
      val snapped = snapToMinutes(rawAngle).coerceIn(snapIntervalMinutes, maxMinutes.toInt())
      return Duration.ofMinutes(snapped.toLong())
    }

  suspend fun animateToDuration(duration: Duration) {
    val targetAngle = durationToAngle(duration)
    Animatable(rawAngle).animateTo(targetAngle, tween(300)) { rawAngle = value }
    lastHapticSnapMinutes = snapToMinutes(targetAngle)
  }

  fun durationToAngle(duration: Duration): Float {
    val minutes = duration.toMinutes().coerceIn(0, maxMinutes)
    return (minutes / 60f) * TWO_PI
  }

  fun snapToMinutes(angle: Float): Int {
    val totalMinutes = angle / TWO_PI * 60f
    return ((totalMinutes / snapIntervalMinutes).roundToInt() * snapIntervalMinutes)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationPickerDialog(
  initialDuration: Duration,
  presets: ImmutableList<Duration>,
  modifier: Modifier = Modifier,
  onConfirm: (Duration) -> Unit = {},
  onCancel: () -> Unit = {},
) {
  val state = remember { DurationPickerState(initialDuration) }

  BasicAlertDialog(onDismissRequest = onCancel) {
    DurationPickerContent(
      state = state,
      presets = presets,
      onConfirm = { onConfirm(state.snappedDuration) },
      onCancel = onCancel,
      modifier = modifier,
    )
  }
}

@Composable
private fun DurationPickerContent(
  state: DurationPickerState,
  presets: ImmutableList<Duration>,
  onConfirm: () -> Unit,
  onCancel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()

  Surface(
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(24.dp)) {
      Text(
        text = stringResource(R.string.duration_title).uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.duration_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(24.dp))
      DurationDial(state = state, modifier = Modifier.align(Alignment.CenterHorizontally))
      Spacer(Modifier.height(20.dp))
      PresetChipsRow(
        presets = presets,
        onSelect = { scope.launch { state.animateToDuration(it) } },
      )
      Spacer(Modifier.height(20.dp))
      HorizontalDivider()
      Spacer(Modifier.height(16.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        Spacer(Modifier.size(8.dp))
        Button(onClick = onConfirm) { Text(stringResource(R.string.ok)) }
      }
    }
  }
}

@Composable
private fun DurationDial(state: DurationPickerState, modifier: Modifier = Modifier) {
  Box(contentAlignment = Alignment.Center, modifier = modifier.size(DIAL_SIZE)) {
    DurationDialCanvas(state = state, modifier = Modifier.matchParentSize())
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = formatDuration(state.snappedDuration),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
      Text(
        text = stringResource(R.string.drag_to_set),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun DurationDialCanvas(state: DurationPickerState, modifier: Modifier = Modifier) {
  val colorScheme = MaterialTheme.colorScheme
  val trackColor = colorScheme.surfaceContainerLow
  val gradientStart = colorScheme.primaryContainer
  val gradientEnd = colorScheme.primary
  val handleColor = colorScheme.surface

  val hapticFeedback = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  Canvas(
    modifier =
      modifier.pointerInput(Unit) {
        val center = Offset(size.width / 2f, size.height / 2f)

        awaitEachGesture {
          val down = awaitPointerEvent()
          val downChange = down.changes.firstOrNull() ?: return@awaitEachGesture
          downChange.consume()

          val initialTouchAngle = angleFromCenter(downChange.position, center)
          var revolutions =
            ((state.rawAngle - initialTouchAngle + PI.toFloat()) / TWO_PI)
              .toInt()
              .coerceIn(0, state.maxRotations)
          val targetRawAngle =
            (revolutions * TWO_PI + initialTouchAngle).coerceIn(state.minAngle, state.maxAngle)
          var prevTouchAngle = initialTouchAngle
          var isDrag = false

          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: break
            if (!change.pressed) {
              change.consume()
              val snapTarget =
                if (isDrag) {
                  state.snappedDuration
                } else {
                  val snappedMinutes =
                    state
                      .snapToMinutes(targetRawAngle)
                      .coerceIn(state.snapIntervalMinutes, state.maxMinutes.toInt())
                  Duration.ofMinutes(snappedMinutes.toLong())
                }
              scope.launch { state.animateToDuration(snapTarget) }
              break
            }
            change.consume()

            if (!isDrag) {
              isDrag = true
              state.rawAngle = targetRawAngle
            }

            val touchAngle = angleFromCenter(change.position, center)
            val delta = touchAngle - prevTouchAngle
            val normalizedDelta = normalizeAngleDelta(delta)

            if (normalizedDelta > 0 && delta < -PI.toFloat()) revolutions++
            else if (normalizedDelta < 0 && delta > PI.toFloat()) revolutions--
            revolutions = revolutions.coerceIn(0, state.maxRotations)

            val newRawAngle =
              (revolutions * TWO_PI + touchAngle).coerceIn(state.minAngle, state.maxAngle)
            state.rawAngle = newRawAngle
            prevTouchAngle = touchAngle

            val currentSnapMinutes = state.snapToMinutes(newRawAngle)
            if (currentSnapMinutes != state.lastHapticSnapMinutes) {
              state.lastHapticSnapMinutes = currentSnapMinutes
              hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
          }
        }
      }
  ) {
    val strokeWidth = TRACK_WIDTH.toPx()
    val handleRadius = HANDLE_RADIUS.toPx()
    val radius = (size.minDimension - strokeWidth) / 2f
    val arcTopLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2, radius * 2)

    // track
    drawCircle(color = trackColor, radius = radius, style = Stroke(width = strokeWidth))

    val sweepDegrees = state.rawAngle / PI.toFloat() * 180f
    val gradientMaxAngle = state.maxAngle + TWO_PI
    val arcStartColor = lerp(gradientStart, gradientEnd, state.rawAngle / gradientMaxAngle)
    val arcEndColor = lerp(gradientStart, gradientEnd, (state.rawAngle + TWO_PI) / gradientMaxAngle)

    // arc and handle
    rotate(sweepDegrees - 90f) {
      drawArc(
        brush = Brush.sweepGradient(0f to arcStartColor, 1f to arcEndColor, center = center),
        startAngle = -sweepDegrees,
        sweepAngle = sweepDegrees,
        useCenter = false,
        topLeft = arcTopLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )
      val handlePos = Offset(center.x + radius, center.y)
      // draw circle to cover brush edge
      drawCircle(color = arcEndColor, radius = strokeWidth / 2, center = handlePos)
      drawCircle(color = handleColor, radius = handleRadius, center = handlePos)
    }
  }
}

private fun angleFromCenter(position: Offset, center: Offset): Float {
  val dx = position.x - center.x
  val dy = position.y - center.y
  return (atan2(dx, -dy) + TWO_PI) % TWO_PI
}

private fun normalizeAngleDelta(delta: Float): Float {
  val pi = PI.toFloat()
  return ((delta + pi) % TWO_PI + TWO_PI) % TWO_PI - pi
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChipsRow(
  presets: ImmutableList<Duration>,
  onSelect: (Duration) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
  ) {
    for (preset in presets) {
      SuggestionChip(
        onClick = { onSelect(preset) },
        label = { Text(formatDuration(preset)) },
      )
    }
  }
}

private val PREVIEW_PRESETS =
  persistentListOf(
    Duration.ofMinutes(15),
    Duration.ofMinutes(30),
    Duration.ofMinutes(45),
    Duration.ofHours(1),
  )

@Preview
@Composable
private fun DurationPickerContentPreview() {
  ToDueTheme {
    val state = remember { DurationPickerState(Duration.ofMinutes(45)) }
    DurationPickerContent(
      state = state,
      presets = PREVIEW_PRESETS,
      onConfirm = {},
      onCancel = {},
    )
  }
}
