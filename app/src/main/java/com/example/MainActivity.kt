package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.KoroKoroHomeScreen
import com.example.ui.theme.KoroKoroTvTheme
import com.example.viewmodel.KaraokeViewModel

class MainActivity : ComponentActivity() {
  private val karaokeViewModel: KaraokeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      KoroKoroTvTheme {
        KoroKoroHomeScreen(viewModel = karaokeViewModel)
      }
    }
  }
}
