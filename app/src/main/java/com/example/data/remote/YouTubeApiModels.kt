package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(val items: List<YouTubeSearchItem> = emptyList())

@JsonClass(generateAdapter = true)
data class YouTubeSearchItem(val id: YouTubeVideoId, val snippet: YouTubeSnippet)

@JsonClass(generateAdapter = true)
data class YouTubeVideoId(val videoId: String? = null)

@JsonClass(generateAdapter = true)
data class YouTubeSnippet(
  val title: String = "",
  val channelTitle: String = "",
  val thumbnails: YouTubeThumbnails = YouTubeThumbnails(),
)

@JsonClass(generateAdapter = true)
data class YouTubeThumbnails(
  val high: YouTubeThumbnail? = null,
  val medium: YouTubeThumbnail? = null,
  val default: YouTubeThumbnail? = null,
) {
  val bestUrl: String?
    get() = high?.url ?: medium?.url ?: default?.url
}

@JsonClass(generateAdapter = true)
data class YouTubeThumbnail(val url: String = "")

@JsonClass(generateAdapter = true)
data class YouTubeVideosResponse(val items: List<YouTubeVideoDetailItem> = emptyList())

@JsonClass(generateAdapter = true)
data class YouTubeVideoDetailItem(
  val id: String = "",
  val contentDetails: YouTubeContentDetails? = null,
)

@JsonClass(generateAdapter = true)
data class YouTubeContentDetails(val duration: String? = null)

@JsonClass(generateAdapter = true)
data class CachedYouTubeSongDto(
  val id: String,
  val title: String,
  val artist: String,
  val categoryName: String,
  val duration: String,
  val thumbnailUrl: String?,
  val youtubeVideoId: String,
)

@JsonClass(generateAdapter = true)
data class CachedYouTubeListDto(val songs: List<CachedYouTubeSongDto> = emptyList())

fun parseIso8601Duration(iso: String?): String {
  if (iso.isNullOrBlank()) return "04:00"
  val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
  val match = regex.matchEntire(iso) ?: return "04:00"
  val hours = match.groupValues[1].toIntOrNull() ?: 0
  val minutes = match.groupValues[2].toIntOrNull() ?: 0
  val seconds = match.groupValues[3].toIntOrNull() ?: 0
  return if (hours > 0) {
    String.format("%02d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}
