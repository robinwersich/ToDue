package com.robinwersich.todue

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.robinwersich.todue.data.AppDataContainer

class ToDueApplication : Application() {
  lateinit var container: AppDataContainer

  override fun onCreate() {
    super.onCreate()
    container = AppDataContainer(this)
  }
}

fun CreationExtras.toDueApplication(): ToDueApplication =
  (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ToDueApplication)
