package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.UsbMicStatus
import com.example.audio.UsbMicrophoneManager
import com.example.data.KaraokeRepository
import com.example.data.SampleKaraokeRepository
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong
import com.example.model.MixerSettings
import com.example.model.VocalPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KaraokeUiState(
  val selectedCategory: KaraokeCategory = KaraokeCategory.HOME,
  val searchQuery: String = "",
  val isSearching: Boolean = false,
  val searchResults: List<KaraokeSong> = SampleKaraokeRepository.sampleSongs,
  val isYouTubeApiConfigured: Boolean = false,
  val allSongs: List<KaraokeSong> = SampleKaraokeRepository.sampleSongs,
  val favoriteSongs: List<KaraokeSong> = emptyList(),
  val historySongs: List<KaraokeSong> = emptyList(),
  val queueSongs: List<KaraokeSong> = emptyList(),
  val activeStageSong: KaraokeSong? = null,
  val isPlaying: Boolean = true,
  val mixerSettings: MixerSettings = MixerSettings(),
  val usbMicStatus: UsbMicStatus = UsbMicStatus(),
  val statusBannerMessage: String? = null,
) {
  val favoriteIds: Set<String>
    get() = favoriteSongs.map { it.id }.toSet()

  val featuredSong: KaraokeSong
    get() = allSongs.firstOrNull { it.isFeatured } ?: allSongs.first()

  val indonesiaSongs: List<KaraokeSong>
    get() = allSongs.filter { it.category == KaraokeCategory.INDONESIA }

  val baratSongs: List<KaraokeSong>
    get() = allSongs.filter { it.category == KaraokeCategory.BARAT }

  val dangdutSongs: List<KaraokeSong>
    get() = allSongs.filter { it.category == KaraokeCategory.DANGDUT }

  val popularSongs: List<KaraokeSong>
    get() = allSongs.filter { it.isPopular }

  val activePreset: VocalPreset
    get() =
      SampleKaraokeRepository.vocalPresets.getOrElse(mixerSettings.selectedPresetNumber) {
        SampleKaraokeRepository.vocalPresets.first()
      }
}

class KaraokeViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = KaraokeRepository(application)
  private val usbMicManager = UsbMicrophoneManager(application)

  private val _uiState =
    MutableStateFlow(
      KaraokeUiState(
        isYouTubeApiConfigured = repository.isYouTubeApiConfigured(),
        usbMicStatus = usbMicManager.status.value,
      )
    )
  val uiState: StateFlow<KaraokeUiState> = _uiState.asStateFlow()

  private var searchJob: Job? = null

  init {
    viewModelScope.launch {
      usbMicManager.status.collect { micStatus ->
        _uiState.update { it.copy(usbMicStatus = micStatus) }
      }
    }

    viewModelScope.launch {
      repository.seedInitialDataIfNeeded()
      combine(
          repository.favoriteSongsFlow,
          repository.historySongsFlow,
          repository.queueSongsFlow,
          repository.mixerSettingsFlow,
        ) { favs, history, queue, mixer ->
          Quadruple(favs, history, queue, mixer)
        }
        .collect { (favs, history, queue, mixer) ->
          usbMicManager.updateMixerSettings(mixer)
          _uiState.update { state ->
            state.copy(
              favoriteSongs = favs,
              historySongs = history,
              queueSongs = queue,
              mixerSettings = mixer,
              isYouTubeApiConfigured =
                repository.isYouTubeApiConfigured(mixer.customYoutubeApiKey),
            )
          }
        }
    }
  }

  fun selectCategory(category: KaraokeCategory) {
    _uiState.update { it.copy(selectedCategory = category, statusBannerMessage = null) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update {
      it.copy(
        searchQuery = query,
        selectedCategory =
          if (query.isNotBlank()) KaraokeCategory.SEARCH else it.selectedCategory,
      )
    }

    searchJob?.cancel()
    searchJob =
      viewModelScope.launch {
        if (query.isBlank()) {
          _uiState.update {
            it.copy(isSearching = false, searchResults = SampleKaraokeRepository.sampleSongs)
          }
          return@launch
        }
        delay(450L) // Debounce YouTube Data API v3 calls
        _uiState.update { it.copy(isSearching = true) }
        val customKey = _uiState.value.mixerSettings.customYoutubeApiKey
        val result = repository.searchKaraoke(query, KaraokeCategory.SEARCH, customKey)
        result.fold(
          onSuccess = { songs ->
            _uiState.update { it.copy(isSearching = false, searchResults = songs) }
          },
          onFailure = {
            _uiState.update {
              it.copy(
                isSearching = false,
                statusBannerMessage = "Gagal memuat pencarian YouTube. Menampilkan katalog lokal.",
              )
            }
          },
        )
      }
  }

  fun playSong(song: KaraokeSong) {
    _uiState.update {
      it.copy(activeStageSong = song, isPlaying = true, statusBannerMessage = null)
    }
    viewModelScope.launch { repository.recordHistory(song) }
  }

  fun closeStage() {
    _uiState.update { it.copy(activeStageSong = null) }
  }

  fun setPlayingState(playing: Boolean) {
    _uiState.update { it.copy(isPlaying = playing) }
  }

  fun togglePlayPause() {
    _uiState.update { it.copy(isPlaying = !it.isPlaying) }
  }

  fun toggleFavorite(song: KaraokeSong) {
    val isCurrentlyFav = _uiState.value.favoriteIds.contains(song.id)
    viewModelScope.launch {
      repository.toggleFavorite(song, isCurrentlyFav)
      val msg =
        if (!isCurrentlyFav) "Ditambahkan ke Lagu Favorit" else "Dihapus dari Lagu Favorit"
      _uiState.update { it.copy(statusBannerMessage = msg) }
    }
  }

  fun addToQueue(song: KaraokeSong) {
    val alreadyInQueue = _uiState.value.queueSongs.any { it.id == song.id }
    if (alreadyInQueue) {
      _uiState.update { it.copy(statusBannerMessage = "Lagu sudah ada di Antrean") }
      return
    }
    viewModelScope.launch {
      repository.addToQueue(song)
      _uiState.update { it.copy(statusBannerMessage = "Ditambahkan ke Antrean Lagu") }
    }
  }

  fun removeFromQueue(songId: String) {
    viewModelScope.launch {
      repository.removeFromQueue(songId)
      _uiState.update { it.copy(statusBannerMessage = "Dihapus dari Antrean Lagu") }
    }
  }

  fun playNextInQueue() {
    val nextSong = _uiState.value.queueSongs.firstOrNull() ?: return
    viewModelScope.launch {
      repository.removeFromQueue(nextSong.id)
      repository.recordHistory(nextSong)
      _uiState.update { it.copy(activeStageSong = nextSong, isPlaying = true) }
    }
  }

  fun selectVocalPreset(number: Int) {
    if (number in 0..9) {
      viewModelScope.launch {
        repository.updateMixerSettings { it.copy(selectedPresetNumber = number) }
      }
    }
  }

  fun adjustMicVolume(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings { current ->
        val nextVol = (current.micVolume + delta).coerceIn(0, 100)
        current.copy(
          micVolume = nextVol,
          isMicMuted = if (nextVol > 0) false else current.isMicMuted,
        )
      }
    }
  }

  fun toggleMicMute() {
    viewModelScope.launch {
      repository.updateMixerSettings { it.copy(isMicMuted = !it.isMicMuted) }
    }
  }

  fun adjustEchoLevel(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(echoLevel = (it.echoLevel + delta).coerceIn(0, 100))
      }
    }
  }

  fun adjustReverbLevel(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(reverbLevel = (it.reverbLevel + delta).coerceIn(0, 100))
      }
    }
  }

  fun adjustBassLevel(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(bassLevel = (it.bassLevel + delta).coerceIn(0, 100))
      }
    }
  }

  fun adjustTrebleLevel(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(trebleLevel = (it.trebleLevel + delta).coerceIn(0, 100))
      }
    }
  }

  fun adjustPitchShift(delta: Int) {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(pitchShift = (it.pitchShift + delta).coerceIn(-6, 6))
      }
    }
  }

  fun toggleMicMonitor() {
    viewModelScope.launch {
      repository.updateMixerSettings {
        it.copy(isMicMonitorEnabled = !it.isMicMonitorEnabled)
      }
    }
  }

  fun updateYoutubeApiKey(apiKey: String) {
    val cleaned = apiKey.trim()
    viewModelScope.launch {
      repository.updateMixerSettings { it.copy(customYoutubeApiKey = cleaned) }
      val msg =
        if (cleaned.isNotEmpty()) "YouTube API Key disimpan & diaktifkan"
        else "YouTube API Key dihapus"
      _uiState.update { it.copy(statusBannerMessage = msg) }
    }
  }

  fun onAudioPermissionResult(granted: Boolean) {
    usbMicManager.refreshDeviceStatus()
    if (granted) {
      usbMicManager.startAudioEngine()
    }
  }

  fun requestUsbPermission() {
    usbMicManager.requestUsbPermissionIfNeeded()
  }

  fun showMessage(msg: String) {
    _uiState.update { it.copy(statusBannerMessage = msg) }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusBannerMessage = null) }
  }

  override fun onCleared() {
    super.onCleared()
    usbMicManager.release()
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
