package com.robinwersich.todue.ui.presentation.organizer.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.robinwersich.todue.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueDatePicker(
  initialSelection: LocalDate,
  modifier: Modifier = Modifier,
  onConfirm: (LocalDate) -> Unit = {},
  onCancel: () -> Unit = {},
) {
  val today = LocalDate.now()
  val todayMillis = localDateToMillis(today)
  val datePickerState =
    rememberDatePickerState(
      initialSelectedDateMillis = localDateToMillis(initialSelection),
      yearRange = today.year until today.year + 99,
      selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis }
    )
  val confirmEnabled by remember {
    derivedStateOf { datePickerState.selectedDateMillis?.let { it >= todayMillis } ?: false }
  }
  DatePickerDialog(
    onDismissRequest = onCancel,
    confirmButton = {
      TextButton(
        onClick = { onConfirm(millisToLocalDate(datePickerState.selectedDateMillis!!)) },
        enabled = confirmEnabled
      ) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) } },
    modifier = modifier,
  ) {
    DatePicker(
      state = datePickerState,
      title = {
        Text(
          stringResource(R.string.select_due_date),
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
      },
      showModeToggle = false,
    )
  }
}

private fun localDateToMillis(localDate: LocalDate) =
  localDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun millisToLocalDate(millis: Long) =
  Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()
