package com.example.data

import android.content.Context
import android.text.Html
import com.example.BuildConfig
import com.example.R
import com.example.data.local.KoroDatabase
import com.example.data.local.MixerPreferencesDataStore
import com.example.data.local.YouTubeCacheEntity
import com.example.data.local.toFavoriteEntity
import com.example.data.local.toHistoryEntity
import com.example.data.local.toKaraokeSong
import com.example.data.local.toQueueEntity
import com.example.data.remote.CachedYouTubeListDto
import com.example.data.remote.CachedYouTubeSongDto
import com.example.data.remote.YouTubeNetworkModule
import com.example.data.remote.parseIso8601Duration
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong
import com.example.model.MixerSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class KaraokeRepository(context: Context) {

  private val dao = KoroDatabase.getInstance(context).koroDao()
  private val mixerPrefs = MixerPreferencesDataStore(context)
  private val apiService = YouTubeNetworkModule.apiService
  private val cacheAdapter =
    YouTubeNetworkModule.moshi.adapter(CachedYouTubeListDto::class.java)

  companion object {
    private const val CACHE_TTL_MILLIS = 6L * 60L * 60L * 1000L // 6 hours
  }

  fun resolveApiKey(customKey: String = ""): String {
    val trimmedCustom = customKey.trim()
    if (trimmedCustom.isNotEmpty()) return trimmedCustom
    val buildKey = BuildConfig.YOUTUBE_API_KEY.trim()
    return if (buildKey.isNotEmpty() && buildKey != "YOUR_YOUTUBE_DATA_API_V3_KEY") buildKey else ""
  }

  fun isYouTubeApiConfigured(customKey: String = ""): Boolean =
    resolveApiKey(customKey).isNotEmpty()

  val favoriteSongsFlow: Flow<List<KaraokeSong>> =
    dao.getAllFavorites().map { list -> list.map { it.toKaraokeSong() } }

  val historySongsFlow: Flow<List<KaraokeSong>> =
    dao.getAllHistory().map { list -> list.map { it.toKaraokeSong() } }

  val queueSongsFlow: Flow<List<KaraokeSong>> =
    dao.getAllQueue().map { list -> list.map { it.toKaraokeSong() } }

  val mixerSettingsFlow: Flow<MixerSettings> = mixerPrefs.mixerSettingsFlow

  suspend fun seedInitialDataIfNeeded() =
    withContext(Dispatchers.IO) {
      val sampleMap = SampleKaraokeRepository.sampleSongs.associateBy { it.id }
      if (dao.getFavoriteCount() == 0) {
        SampleKaraokeRepository.initialFavorites.forEach { id ->
          sampleMap[id]?.let { dao.insertFavorite(it.toFavoriteEntity()) }
        }
      }
      if (dao.getHistoryCount() == 0) {
        SampleKaraokeRepository.initialHistory.forEach { id ->
          sampleMap[id]?.let { dao.insertHistory(it.toHistoryEntity()) }
        }
      }
      if (dao.getQueueCount() == 0) {
        SampleKaraokeRepository.initialQueue.forEachIndexed { index, id ->
          sampleMap[id]?.let { dao.insertQueueItem(it.toQueueEntity(index + 1)) }
        }
      }
      dao.deleteExpiredCache(System.currentTimeMillis() - CACHE_TTL_MILLIS)
    }

  suspend fun toggleFavorite(song: KaraokeSong, currentlyFavorite: Boolean) =
    withContext(Dispatchers.IO) {
      if (currentlyFavorite) {
        dao.deleteFavoriteById(song.id)
      } else {
        dao.insertFavorite(song.toFavoriteEntity())
      }
    }

  suspend fun recordHistory(song: KaraokeSong) =
    withContext(Dispatchers.IO) { dao.insertHistory(song.toHistoryEntity()) }

  suspend fun addToQueue(song: KaraokeSong) =
    withContext(Dispatchers.IO) {
      val nextPos = dao.getMaxQueuePosition() + 1
      dao.insertQueueItem(song.toQueueEntity(nextPos))
    }

  suspend fun removeFromQueue(songId: String) =
    withContext(Dispatchers.IO) { dao.deleteQueueItemById(songId) }

  suspend fun updateMixerSettings(transform: (MixerSettings) -> MixerSettings) {
    mixerPrefs.updateSettings(transform)
  }

  suspend fun searchKaraoke(
    query: String,
    category: KaraokeCategory = KaraokeCategory.SEARCH,
    customApiKey: String = "",
  ): Result<List<KaraokeSong>> =
    withContext(Dispatchers.IO) {
      val cleanQuery = query.trim()
      if (cleanQuery.isEmpty()) {
        return@withContext Result.success(SampleKaraokeRepository.sampleSongs)
      }

      val cacheKey = "yt_search_${category.name}_${cleanQuery.lowercase()}"
      val now = System.currentTimeMillis()

      // 1. Check Room Cache first
      val cached = dao.getCacheByKey(cacheKey)
      if (cached != null && (now - cached.cachedAtMillis) < CACHE_TTL_MILLIS) {
        val parsed = runCatching { cacheAdapter.fromJson(cached.responseJson) }.getOrNull()
        if (parsed != null && parsed.songs.isNotEmpty()) {
          return@withContext Result.success(parsed.songs.map { it.toDomainSong() })
        }
      }

      // 2. If YouTube Data API v3 key is configured, fetch live results
      val activeApiKey = resolveApiKey(customApiKey)
      if (activeApiKey.isNotEmpty()) {
        try {
          val searchKeyword =
            if (cleanQuery.contains("karaoke", ignoreCase = true)) cleanQuery
            else "$cleanQuery karaoke"

          val searchResp =
            apiService.searchVideos(
              query = searchKeyword,
              apiKey = activeApiKey,
            )

          val videoIds =
            searchResp.items.mapNotNull { it.id.videoId }.distinct().joinToString(",")

          val durationsMap =
            if (videoIds.isNotEmpty()) {
              runCatching {
                  apiService
                    .getVideoDetails(
                      videoIds = videoIds,
                      apiKey = activeApiKey,
                    )
                    .items
                    .associate { it.id to parseIso8601Duration(it.contentDetails?.duration) }
                }
                .getOrDefault(emptyMap())
            } else {
              emptyMap()
            }

          val songs =
            searchResp.items.mapNotNull { item ->
              val vid = item.id.videoId ?: return@mapNotNull null
              val decodedTitle =
                Html.fromHtml(item.snippet.title, Html.FROM_HTML_MODE_LEGACY).toString()
              val decodedChannel =
                Html.fromHtml(item.snippet.channelTitle, Html.FROM_HTML_MODE_LEGACY).toString()
              KaraokeSong(
                id = "yt_$vid",
                title = decodedTitle,
                artist = decodedChannel,
                category = category,
                duration = durationsMap[vid] ?: "04:00",
                thumbnailRes = R.drawable.img_hero_karaoke_1793869687632,
                thumbnailUrl =
                  item.snippet.thumbnails.bestUrl ?: "https://i.ytimg.com/vi/$vid/hqdefault.jpg",
                youtubeVideoId = vid,
              )
            }

          if (songs.isNotEmpty()) {
            val dto =
              CachedYouTubeListDto(
                songs =
                  songs.map {
                    CachedYouTubeSongDto(
                      id = it.id,
                      title = it.title,
                      artist = it.artist,
                      categoryName = it.category.name,
                      duration = it.duration,
                      thumbnailUrl = it.thumbnailUrl,
                      youtubeVideoId = it.youtubeVideoId,
                    )
                  }
              )
            cacheAdapter.toJson(dto)?.let { json ->
              dao.insertCache(
                YouTubeCacheEntity(
                  cacheKey = cacheKey,
                  responseJson = json,
                  cachedAtMillis = now,
                )
              )
            }
          }

          return@withContext Result.success(songs)
        } catch (e: Exception) {
          // Fallback to local sample filter if network/quota fails
          val fallback = filterLocalSamples(cleanQuery)
          return@withContext if (fallback.isNotEmpty()) {
            Result.success(fallback)
          } else {
            Result.failure(e)
          }
        }
      }

      // 3. Local Catalog Fallback when YOUTUBE_API_KEY is not yet configured
      Result.success(filterLocalSamples(cleanQuery))
    }

  private fun filterLocalSamples(query: String): List<KaraokeSong> {
    val q = query.lowercase()
    return SampleKaraokeRepository.sampleSongs.filter {
      it.title.lowercase().contains(q) ||
        it.artist.lowercase().contains(q) ||
        it.category.label.lowercase().contains(q)
    }
  }

  private fun CachedYouTubeSongDto.toDomainSong(): KaraokeSong =
    KaraokeSong(
      id = id,
      title = title,
      artist = artist,
      category =
        runCatching { KaraokeCategory.valueOf(categoryName) }.getOrDefault(KaraokeCategory.SEARCH),
      duration = duration,
      thumbnailRes = R.drawable.img_hero_karaoke_1793869687632,
      thumbnailUrl = thumbnailUrl,
      youtubeVideoId = youtubeVideoId,
    )
}
