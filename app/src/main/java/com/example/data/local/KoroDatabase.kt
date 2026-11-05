package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities =
    [
      FavoriteSongEntity::class,
      HistorySongEntity::class,
      QueueSongEntity::class,
      YouTubeCacheEntity::class,
    ],
  version = 1,
  exportSchema = false,
)
abstract class KoroDatabase : RoomDatabase() {
  abstract fun koroDao(): KoroDao

  companion object {
    @Volatile private var INSTANCE: KoroDatabase? = null

    fun getInstance(context: Context): KoroDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                KoroDatabase::class.java,
                "koro_koro_tv.db",
              )
              .fallbackToDestructiveMigration()
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
