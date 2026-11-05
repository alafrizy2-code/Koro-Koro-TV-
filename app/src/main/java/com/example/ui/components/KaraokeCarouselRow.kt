package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.KaraokeSong
import com.example.ui.theme.KoroCardCharcoal
import com.example.ui.theme.KoroTextMuted
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary

@Composable
fun KaraokeCarouselRow(
  title: String,
  songs: List<KaraokeSong>,
  favoriteIds: Set<String>,
  onSongClick: (KaraokeSong) -> Unit,
  tagPrefix: String,
  emptyMessage: String = "Belum ada lagu di daftar ini.",
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = KoroTextPrimary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .background(KoroCardCharcoal)
              .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${songs.size} Lagu",
            style = MaterialTheme.typography.labelSmall,
            color = KoroTextSecondary,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (songs.isEmpty()) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(KoroCardCharcoal.copy(alpha = 0.6f))
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Text(
          text = emptyMessage,
          style = MaterialTheme.typography.bodyLarge,
          color = KoroTextMuted,
        )
      }
    } else {
      LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items(items = songs, key = { "${tagPrefix}_${it.id}" }) { song ->
          KaraokeVideoCard(
            song = song,
            isFavorite = favoriteIds.contains(song.id),
            onClick = { onSongClick(song) },
            tagPrefix = tagPrefix,
          )
        }
      }
    }
  }
}
