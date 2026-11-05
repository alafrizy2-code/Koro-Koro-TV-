package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class KaraokeCategory(val label: String, val routeId: String) {
  SEARCH("Pencarian", "search"),
  HOME("Beranda", "home"),
  INDONESIA("Karaoke Indonesia", "indonesia"),
  BARAT("Karaoke Barat", "barat"),
  DANGDUT("Karaoke Dangdut", "dangdut"),
  POPULER("Lagu Populer", "populer"),
  FAVORIT("Lagu Favorit", "favorit"),
  RIWAYAT("Riwayat Pemutaran", "riwayat"),
  ANTREAN("Antrean Lagu", "antrean"),
}

data class KaraokeSong(
  val id: String,
  val title: String,
  val artist: String,
  val category: KaraokeCategory,
  val duration: String,
  @DrawableRes val thumbnailRes: Int = R.drawable.img_hero_karaoke_1793869687632,
  val thumbnailUrl: String? = null,
  val youtubeVideoId: String,
  val isFeatured: Boolean = false,
  val isPopular: Boolean = false,
  val sampleLyrics: List<String> = emptyList(),
)

data class VocalPreset(val number: Int, val name: String, val subtitle: String)

data class MixerSettings(
  val selectedPresetNumber: Int = 0,
  val micVolume: Int = 80,
  val isMicMuted: Boolean = false,
  val echoLevel: Int = 35,
  val reverbLevel: Int = 40,
  val bassLevel: Int = 50,
  val trebleLevel: Int = 55,
  val pitchShift: Int = 0, // -6 to +6 semitones
  val isMicMonitorEnabled: Boolean = false,
  val customYoutubeApiKey: String = "",
)
