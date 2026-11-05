package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.MixerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mixerDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "koro_mixer_prefs")

class MixerPreferencesDataStore(private val context: Context) {

  private object Keys {
    val PRESET_NUMBER = intPreferencesKey("preset_number")
    val MIC_VOLUME = intPreferencesKey("mic_volume")
    val MIC_MUTED = booleanPreferencesKey("mic_muted")
    val ECHO_LEVEL = intPreferencesKey("echo_level")
    val REVERB_LEVEL = intPreferencesKey("reverb_level")
    val BASS_LEVEL = intPreferencesKey("bass_level")
    val TREBLE_LEVEL = intPreferencesKey("treble_level")
    val PITCH_SHIFT = intPreferencesKey("pitch_shift")
    val MIC_MONITOR = booleanPreferencesKey("mic_monitor")
    val CUSTOM_YOUTUBE_API_KEY = stringPreferencesKey("custom_youtube_api_key")
  }

  val mixerSettingsFlow: Flow<MixerSettings> =
    context.mixerDataStore.data.map { prefs ->
      MixerSettings(
        selectedPresetNumber = prefs[Keys.PRESET_NUMBER] ?: 0,
        micVolume = prefs[Keys.MIC_VOLUME] ?: 80,
        isMicMuted = prefs[Keys.MIC_MUTED] ?: false,
        echoLevel = prefs[Keys.ECHO_LEVEL] ?: 35,
        reverbLevel = prefs[Keys.REVERB_LEVEL] ?: 40,
        bassLevel = prefs[Keys.BASS_LEVEL] ?: 50,
        trebleLevel = prefs[Keys.TREBLE_LEVEL] ?: 55,
        pitchShift = prefs[Keys.PITCH_SHIFT] ?: 0,
        isMicMonitorEnabled = prefs[Keys.MIC_MONITOR] ?: false,
        customYoutubeApiKey = prefs[Keys.CUSTOM_YOUTUBE_API_KEY] ?: "",
      )
    }

  suspend fun updateSettings(transform: (MixerSettings) -> MixerSettings) {
    context.mixerDataStore.edit { prefs ->
      val current =
        MixerSettings(
          selectedPresetNumber = prefs[Keys.PRESET_NUMBER] ?: 0,
          micVolume = prefs[Keys.MIC_VOLUME] ?: 80,
          isMicMuted = prefs[Keys.MIC_MUTED] ?: false,
          echoLevel = prefs[Keys.ECHO_LEVEL] ?: 35,
          reverbLevel = prefs[Keys.REVERB_LEVEL] ?: 40,
          bassLevel = prefs[Keys.BASS_LEVEL] ?: 50,
          trebleLevel = prefs[Keys.TREBLE_LEVEL] ?: 55,
          pitchShift = prefs[Keys.PITCH_SHIFT] ?: 0,
          isMicMonitorEnabled = prefs[Keys.MIC_MONITOR] ?: false,
          customYoutubeApiKey = prefs[Keys.CUSTOM_YOUTUBE_API_KEY] ?: "",
        )
      val updated = transform(current)
      prefs[Keys.PRESET_NUMBER] = updated.selectedPresetNumber
      prefs[Keys.MIC_VOLUME] = updated.micVolume
      prefs[Keys.MIC_MUTED] = updated.isMicMuted
      prefs[Keys.ECHO_LEVEL] = updated.echoLevel
      prefs[Keys.REVERB_LEVEL] = updated.reverbLevel
      prefs[Keys.BASS_LEVEL] = updated.bassLevel
      prefs[Keys.TREBLE_LEVEL] = updated.trebleLevel
      prefs[Keys.PITCH_SHIFT] = updated.pitchShift
      prefs[Keys.MIC_MONITOR] = updated.isMicMonitorEnabled
      prefs[Keys.CUSTOM_YOUTUBE_API_KEY] = updated.customYoutubeApiKey
    }
  }
}
