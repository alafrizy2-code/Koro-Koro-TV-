package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong
import com.example.ui.components.HeroKaraokeBanner
import com.example.ui.components.KaraokeCarouselRow
import com.example.ui.components.KaraokeVideoCard
import com.example.ui.components.TvNavigationRail
import com.example.ui.components.TvTopHeader
import com.example.ui.components.tvFocusable
import com.example.ui.theme.KoroBackground
import com.example.ui.theme.KoroCardCharcoal
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroDivider
import com.example.ui.theme.KoroEmerald
import com.example.ui.theme.KoroGold
import com.example.ui.theme.KoroSurface
import com.example.ui.theme.KoroTextMuted
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary
import com.example.viewmodel.KaraokeUiState
import com.example.viewmodel.KaraokeViewModel
import kotlinx.coroutines.delay

@Composable
fun KoroKoroHomeScreen(viewModel: KaraokeViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showApiKeyDialog by remember { mutableStateOf(false) }

  val audioPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      viewModel.onAudioPermissionResult(granted)
    }

  val voiceSearchLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val matches =
          result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val spokenText = matches?.firstOrNull().orEmpty()
        if (spokenText.isNotBlank()) {
          viewModel.updateSearchQuery(spokenText)
        }
      }
    }

  val launchVoiceSearch = {
    val intent =
      Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
          RecognizerIntent.EXTRA_LANGUAGE_MODEL,
          RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Sebutkan judul lagu atau penyanyi...")
      }
    try {
      voiceSearchLauncher.launch(intent)
    } catch (_: ActivityNotFoundException) {
      viewModel.selectCategory(KaraokeCategory.SEARCH)
      viewModel.showMessage("Layanan pencarian suara tidak tersedia di perangkat ini")
    }
  }

  LaunchedEffect(Unit) {
    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
  }

  // Auto-dismiss status banner after 2.5 seconds
  LaunchedEffect(uiState.statusBannerMessage) {
    if (uiState.statusBannerMessage != null) {
      delay(2500L)
      viewModel.clearStatusMessage()
    }
  }

  // When on a sub-category, Back returns to Beranda (HOME)
  BackHandler(
    enabled =
      !showApiKeyDialog &&
        uiState.activeStageSong == null &&
        uiState.selectedCategory != KaraokeCategory.HOME
  ) {
    viewModel.selectCategory(KaraokeCategory.HOME)
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(colors = listOf(KoroBackground, KoroSurface, KoroBackground))
        )
        .safeDrawingPadding()
  ) {
    Row(modifier = Modifier.fillMaxSize()) {
      // Left YouTube TV-Style Navigation Rail
      TvNavigationRail(
        selectedCategory = uiState.selectedCategory,
        queueCount = uiState.queueSongs.size,
        favoriteCount = uiState.favoriteSongs.size,
        onSelectCategory = viewModel::selectCategory,
      )

      // Main 16:9 Content Area
      Column(modifier = Modifier.weight(1f).fillMaxSize()) {
        TvTopHeader(
          searchQuery = uiState.searchQuery,
          isApiConfigured = uiState.isYouTubeApiConfigured,
          isUsbMicConnected = uiState.usbMicStatus.isUsbMicConnected,
          onSearchQueryChange = viewModel::updateSearchQuery,
          onVoiceSearchClick = launchVoiceSearch,
          onApiSettingsClick = { showApiKeyDialog = true },
        )

        if (uiState.selectedCategory == KaraokeCategory.HOME) {
          HomeCarouselsContent(
            uiState = uiState,
            onPlaySong = viewModel::playSong,
            onAddToQueue = viewModel::addToQueue,
            onToggleFavorite = viewModel::toggleFavorite,
          )
        } else {
          CategoryGridContent(
            uiState = uiState,
            onPlaySong = viewModel::playSong,
          )
        }
      }
    }

    // Floating Status Toast Pill for TV Feedback
    AnimatedVisibility(
      visible = uiState.statusBannerMessage != null,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.align(Alignment.BottomEnd).padding(28.dp),
    ) {
      Box(
        modifier =
          Modifier.clip(RoundedCornerShape(14.dp))
            .background(KoroCardElevated)
            .border(1.5.dp, KoroGold, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        Text(
          text = uiState.statusBannerMessage.orEmpty(),
          style = MaterialTheme.typography.labelLarge,
          color = KoroTextPrimary,
        )
      }
    }

    // Fullscreen YouTube Karaoke Player Overlay (Tahap 2)
    val stageSong = uiState.activeStageSong
    if (stageSong != null) {
      KaraokeStageOverlay(
        song = stageSong,
        isPlaying = uiState.isPlaying,
        isFavorite = uiState.favoriteIds.contains(stageSong.id),
        queueSongs = uiState.queueSongs,
        selectedPreset = uiState.activePreset,
        mixerSettings = uiState.mixerSettings,
        usbMicStatus = uiState.usbMicStatus,
        onPlayingChanged = viewModel::setPlayingState,
        onNextInQueue = viewModel::playNextInQueue,
        onPlayQueuedSong = { queued ->
          viewModel.removeFromQueue(queued.id)
          viewModel.playSong(queued)
        },
        onRemoveFromQueue = viewModel::removeFromQueue,
        onToggleFavorite = { viewModel.toggleFavorite(stageSong) },
        onAddToQueue = { viewModel.addToQueue(stageSong) },
        onSelectPreset = viewModel::selectVocalPreset,
        onAdjustMicVolume = viewModel::adjustMicVolume,
        onToggleMicMute = viewModel::toggleMicMute,
        onAdjustEcho = viewModel::adjustEchoLevel,
        onAdjustReverb = viewModel::adjustReverbLevel,
        onAdjustBass = viewModel::adjustBassLevel,
        onAdjustTreble = viewModel::adjustTrebleLevel,
        onAdjustPitch = viewModel::adjustPitchShift,
        onToggleMicMonitor = viewModel::toggleMicMonitor,
        onRequestUsbPermission = viewModel::requestUsbPermission,
        onRequestAudioPermission = {
          audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onClose = viewModel::closeStage,
      )
    }

    if (showApiKeyDialog) {
      YouTubeApiKeyDialog(
        currentKey = uiState.mixerSettings.customYoutubeApiKey,
        onSaveKey = { newKey ->
          viewModel.updateYoutubeApiKey(newKey)
          showApiKeyDialog = false
        },
        onDismiss = { showApiKeyDialog = false },
      )
    }
  }
}

@Composable
private fun YouTubeApiKeyDialog(
  currentKey: String,
  onSaveKey: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  BackHandler(onBack = onDismiss)
  var keyInput by remember(currentKey) { mutableStateOf(currentKey) }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(KoroBackground.copy(alpha = 0.85f))
        .padding(32.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.width(540.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(KoroSurface)
          .border(1.5.dp, KoroDivider, RoundedCornerShape(20.dp))
          .padding(28.dp)
    ) {
      Text(
        text = "Pengaturan YouTube Data API v3",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = KoroTextPrimary,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text =
          "Tempelkan YouTube Data API v3 Key Anda di bawah ini untuk mengaktifkan pencarian lagu karaoke langsung dari YouTube.",
        style = MaterialTheme.typography.bodyMedium,
        color = KoroTextSecondary,
      )

      Spacer(modifier = Modifier.height(18.dp))

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KoroCardCharcoal)
            .border(1.5.dp, KoroGold.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        if (keyInput.isEmpty()) {
          Text(
            text = "AIzaSy...",
            style = MaterialTheme.typography.bodyLarge,
            color = KoroTextMuted,
          )
        }
        BasicTextField(
          value = keyInput,
          onValueChange = { keyInput = it },
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge.copy(color = KoroTextPrimary),
          cursorBrush = SolidColor(KoroCrimson),
          modifier = Modifier.fillMaxWidth().testTag("api_key_input"),
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (currentKey.isNotEmpty()) {
          Box(
            modifier =
              Modifier.height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KoroCardElevated)
                .tvFocusable(onClick = { onSaveKey("") }, shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "Hapus",
              style = MaterialTheme.typography.labelLarge,
              color = KoroCrimson,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
        }

        Box(
          modifier =
            Modifier.height(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(KoroCardElevated)
              .tvFocusable(onClick = onDismiss, shape = RoundedCornerShape(12.dp))
              .padding(horizontal = 18.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Batal",
            style = MaterialTheme.typography.labelLarge,
            color = KoroTextSecondary,
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
          modifier =
            Modifier.height(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(KoroCrimson)
              .tvFocusable(
                onClick = { onSaveKey(keyInput) },
                shape = RoundedCornerShape(12.dp),
              )
              .testTag("save_api_key_button")
              .padding(horizontal = 22.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "Simpan & Aktifkan",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        }
      }
    }
  }
}

@Composable
private fun HomeCarouselsContent(
  uiState: KaraokeUiState,
  onPlaySong: (KaraokeSong) -> Unit,
  onAddToQueue: (KaraokeSong) -> Unit,
  onToggleFavorite: (KaraokeSong) -> Unit,
) {
  val featured = uiState.featuredSong

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 36.dp),
    verticalArrangement = Arrangement.spacedBy(26.dp),
  ) {
    // 1. Video Karaoke Unggulan (Hero Banner)
    item(key = "hero_banner") {
      HeroKaraokeBanner(
        song = featured,
        isFavorite = uiState.favoriteIds.contains(featured.id),
        onPlayClick = { onPlaySong(featured) },
        onAddToQueueClick = { onAddToQueue(featured) },
        onToggleFavoriteClick = { onToggleFavorite(featured) },
      )
    }

    // 2. Karaoke Indonesia
    item(key = "row_indonesia") {
      KaraokeCarouselRow(
        title = "Karaoke Indonesia",
        songs = uiState.indonesiaSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "indo",
      )
    }

    // 3. Karaoke Barat
    item(key = "row_barat") {
      KaraokeCarouselRow(
        title = "Karaoke Barat",
        songs = uiState.baratSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "barat",
      )
    }

    // 4. Karaoke Dangdut
    item(key = "row_dangdut") {
      KaraokeCarouselRow(
        title = "Karaoke Dangdut",
        songs = uiState.dangdutSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "dangdut",
      )
    }

    // 5. Lagu Populer
    item(key = "row_populer") {
      KaraokeCarouselRow(
        title = "Lagu Populer",
        songs = uiState.popularSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "populer",
      )
    }

    // 6. Lagu Favorit
    item(key = "row_favorit") {
      KaraokeCarouselRow(
        title = "Lagu Favorit",
        songs = uiState.favoriteSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "favorit",
        emptyMessage = "Belum ada lagu favorit. Tekan Favorit pada lagu untuk menyimpannya.",
      )
    }

    // 7. Riwayat Pemutaran
    item(key = "row_riwayat") {
      KaraokeCarouselRow(
        title = "Riwayat Pemutaran",
        songs = uiState.historySongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "riwayat",
        emptyMessage = "Riwayat pemutaran masih kosong.",
      )
    }

    // 8. Antrean Lagu
    item(key = "row_antrean") {
      KaraokeCarouselRow(
        title = "Antrean Lagu",
        songs = uiState.queueSongs,
        favoriteIds = uiState.favoriteIds,
        onSongClick = onPlaySong,
        tagPrefix = "antrean",
        emptyMessage = "Antrean lagu kosong. Tambahkan lagu menggunakan tombol + Antrean.",
      )
    }
  }
}

@Composable
private fun CategoryGridContent(
  uiState: KaraokeUiState,
  onPlaySong: (KaraokeSong) -> Unit,
) {
  val songs =
    when (uiState.selectedCategory) {
      KaraokeCategory.SEARCH -> uiState.searchResults
      KaraokeCategory.INDONESIA -> uiState.indonesiaSongs
      KaraokeCategory.BARAT -> uiState.baratSongs
      KaraokeCategory.DANGDUT -> uiState.dangdutSongs
      KaraokeCategory.POPULER -> uiState.popularSongs
      KaraokeCategory.FAVORIT -> uiState.favoriteSongs
      KaraokeCategory.RIWAYAT -> uiState.historySongs
      KaraokeCategory.ANTREAN -> uiState.queueSongs
      KaraokeCategory.HOME -> uiState.allSongs
    }

  Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text =
            if (uiState.selectedCategory == KaraokeCategory.SEARCH && uiState.searchQuery.isNotBlank()) {
              "Hasil Pencarian: \"${uiState.searchQuery}\""
            } else {
              uiState.selectedCategory.label
            },
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = KoroTextPrimary,
        )
        Text(
          text = "${songs.size} video karaoke tersedia • Tekan OK pada remote untuk memutar",
          style = MaterialTheme.typography.bodyMedium,
          color = KoroTextSecondary,
        )
      }

      if (uiState.selectedCategory == KaraokeCategory.SEARCH) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (uiState.isSearching) {
            CircularProgressIndicator(
              color = KoroCrimson,
              strokeWidth = 2.5.dp,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Box(
            modifier =
              Modifier.clip(RoundedCornerShape(10.dp))
                .background(KoroCardElevated)
                .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text =
                if (uiState.isYouTubeApiConfigured) "YouTube Data API v3 • Cache Aktif"
                else "Mode Katalog Lokal",
              style = MaterialTheme.typography.labelSmall,
              color = if (uiState.isYouTubeApiConfigured) KoroEmerald else KoroGold,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (songs.isEmpty() && !uiState.isSearching) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(KoroCardElevated.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Tidak ada lagu yang ditemukan.",
          style = MaterialTheme.typography.titleMedium,
          color = KoroTextSecondary,
        )
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(items = songs, key = { "grid_${it.id}" }) { song ->
          KaraokeVideoCard(
            song = song,
            isFavorite = uiState.favoriteIds.contains(song.id),
            onClick = { onPlaySong(song) },
            tagPrefix = "grid",
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
