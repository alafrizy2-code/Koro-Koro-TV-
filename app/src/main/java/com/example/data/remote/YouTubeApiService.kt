package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

  @GET("search")
  suspend fun searchVideos(
    @Query("part") part: String = "snippet",
    @Query("q") query: String,
    @Query("type") type: String = "video",
    @Query("videoEmbeddable") videoEmbeddable: String = "true",
    @Query("maxResults") maxResults: Int = 12,
    @Query("key") apiKey: String,
  ): YouTubeSearchResponse

  @GET("videos")
  suspend fun getVideoDetails(
    @Query("part") part: String = "contentDetails",
    @Query("id") videoIds: String,
    @Query("key") apiKey: String,
  ): YouTubeVideosResponse
}

object YouTubeNetworkModule {
  private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

  val moshi: Moshi by lazy {
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  }

  private val okHttpClient: OkHttpClient by lazy {
    val logging =
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
    OkHttpClient.Builder()
      .addInterceptor(logging)
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(15, TimeUnit.SECONDS)
      .build()
  }

  val apiService: YouTubeApiService by lazy {
    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
      .create(YouTubeApiService::class.java)
  }
}
