package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KoroDao {

  // Favorites
  @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
  fun getAllFavorites(): Flow<List<FavoriteSongEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(song: FavoriteSongEntity)

  @Query("DELETE FROM favorite_songs WHERE id = :songId")
  suspend fun deleteFavoriteById(songId: String)

  @Query("SELECT COUNT(*) FROM favorite_songs")
  suspend fun getFavoriteCount(): Int

  // History
  @Query("SELECT * FROM history_songs ORDER BY playedAt DESC LIMIT 25")
  fun getAllHistory(): Flow<List<HistorySongEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHistory(song: HistorySongEntity)

  @Query("SELECT COUNT(*) FROM history_songs")
  suspend fun getHistoryCount(): Int

  // Queue
  @Query("SELECT * FROM queue_songs ORDER BY position ASC, addedAt ASC")
  fun getAllQueue(): Flow<List<QueueSongEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQueueItem(song: QueueSongEntity)

  @Query("DELETE FROM queue_songs WHERE id = :songId")
  suspend fun deleteQueueItemById(songId: String)

  @Query("SELECT COALESCE(MAX(position), 0) FROM queue_songs")
  suspend fun getMaxQueuePosition(): Int

  @Query("SELECT COUNT(*) FROM queue_songs")
  suspend fun getQueueCount(): Int

  // YouTube API Cache
  @Query("SELECT * FROM youtube_cache WHERE cacheKey = :key LIMIT 1")
  suspend fun getCacheByKey(key: String): YouTubeCacheEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCache(cache: YouTubeCacheEntity)

  @Query("DELETE FROM youtube_cache WHERE cachedAtMillis < :expireBeforeMillis")
  suspend fun deleteExpiredCache(expireBeforeMillis: Long)
}
