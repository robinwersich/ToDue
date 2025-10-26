package com.robinwersich.todue.ui.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import com.robinwersich.todue.ui.presentation.organizer.OrganizerScreen
import com.robinwersich.todue.ui.presentation.organizer.OrganizerViewModel
import com.robinwersich.todue.ui.theme.ToDueTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ToDueTheme {
        val viewModel: OrganizerViewModel = viewModel(factory = OrganizerViewModel.Factory)
        val tasks by viewModel.tasksFlow.collectAsStateWithLifecycle(persistentMapOf())
        OrganizerScreen(
          navigationState = viewModel.navigationState,
          getTasks = { timelineBlock -> tasks.getOrElse(timelineBlock) { persistentListOf() } },
          onEvent = viewModel::handleEvent,
        )
      }
    }
  }
}
