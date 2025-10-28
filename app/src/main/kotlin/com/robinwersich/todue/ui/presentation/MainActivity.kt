package com.robinwersich.todue.ui.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robinwersich.todue.ui.presentation.organizer.OrganizerScreen
import com.robinwersich.todue.ui.presentation.organizer.OrganizerViewModel
import com.robinwersich.todue.ui.theme.ToDueTheme
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ToDueTheme {
        val viewModel: OrganizerViewModel = viewModel(factory = OrganizerViewModel.Factory)
        OrganizerScreen(
          navigationState = viewModel.navigationState,
          onEvent = viewModel::handleEvent,
        )
      }
    }
  }
}
