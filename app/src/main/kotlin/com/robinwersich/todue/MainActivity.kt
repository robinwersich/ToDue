package com.robinwersich.todue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.robinwersich.todue.ui.screens.main.MainScreen
import com.robinwersich.todue.ui.screens.main.MainScreenViewModel
import com.robinwersich.todue.ui.theme.ToDueTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ToDueTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          val viewModel: MainScreenViewModel = viewModel(factory = MainScreenViewModel.Factory)
          val state by viewModel.viewState.collectAsState()
          MainScreen(state, viewModel::handleEvent)
        }
      }
    }
  }
}
