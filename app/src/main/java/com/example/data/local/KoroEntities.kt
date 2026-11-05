package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.R
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
  @PrimaryKey val id: String,
  val title: String,
  val artist: String,
  val categoryName: String,
  val duration: String,
  val thumbnailRes: Int,
  val thumbnailUrl: String?,
  val youtubeVideoId: String,
  val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "history_songs")
data class HistorySongEntity(
  @PrimaryKey val id: String,
  val title: String,
  val artist: String,
  val categoryName: String,
  val duration: String,
  val thumbnailRes: Int,
  val thumbnailUrl: String?,
  val youtubeVideoId: String,
  val playedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "queue_songs")
data class QueueSongEntity(
  @PrimaryKey val id: String,
  val title: String,
  val artist: String,
  val categoryName: String,
  val duration: String,
  val thumbnailRes: Int,
  val thumbnailUrl: String?,
  val youtubeVideoId: String,
  val position: Int,
  val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "youtube_cache")
data class YouTubeCacheEntity(
  @PrimaryKey val cacheKey: String,
  val responseJson: String,
  val cachedAtMillis: Long = System.currentTimeMillis(),
)

fun KaraokeSong.toFavoriteEntity(): FavoriteSongEntity =
  FavoriteSongEntity(
    id = id,
    title = title,
    artist = artist,
    categoryName = category.name,
    duration = duration,
    thumbnailRes = thumbnailRes,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
  )

fun FavoriteSongEntity.toKaraokeSong(): KaraokeSong =
  KaraokeSong(
    id = id,
    title = title,
    artist = artist,
    category = runCatching { KaraokeCategory.valueOf(categoryName) }.getOrDefault(KaraokeCategory.POPULER),
    duration = duration,
    thumbnailRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_karaoke_1793869687632,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
  )

fun KaraokeSong.toHistoryEntity(): HistorySongEntity =
  HistorySongEntity(
    id = id,
    title = title,
    artist = artist,
    categoryName = category.name,
    duration = duration,
    thumbnailRes = thumbnailRes,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
  )

fun HistorySongEntity.toKaraokeSong(): KaraokeSong =
  KaraokeSong(
    id = id,
    title = title,
    artist = artist,
    category = runCatching { KaraokeCategory.valueOf(categoryName) }.getOrDefault(KaraokeCategory.POPULER),
    duration = duration,
    thumbnailRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_karaoke_1793869687632,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
  )

fun KaraokeSong.toQueueEntity(position: Int): QueueSongEntity =
  QueueSongEntity(
    id = id,
    title = title,
    artist = artist,
    categoryName = category.name,
    duration = duration,
    thumbnailRes = thumbnailRes,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
    position = position,
  )

fun QueueSongEntity.toKaraokeSong(): KaraokeSong =
  KaraokeSong(
    id = id,
    title = title,
    artist = artist,
    category = runCatching { KaraokeCategory.valueOf(categoryName) }.getOrDefault(KaraokeCategory.POPULER),
    duration = duration,
    thumbnailRes = if (thumbnailRes != 0) thumbnailRes else R.drawable.img_hero_karaoke_1793869687632,
    thumbnailUrl = thumbnailUrl,
    youtubeVideoId = youtubeVideoId,
  )
