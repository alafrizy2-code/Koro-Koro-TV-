package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.model.KaraokeSong
import com.example.ui.theme.KoroBackground
import com.example.ui.theme.KoroCardCharcoal
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary

@Composable
fun KaraokeVideoCard(
  song: KaraokeSong,
  isFavorite: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tagPrefix: String = "card",
) {
  var isFocused by remember { mutableStateOf(false) }

  Column(
    modifier =
      modifier
        .width(256.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(if (isFocused) KoroCardElevated else KoroCardCharcoal)
        .tvFocusable(
          onClick = onClick,
          shape = RoundedCornerShape(14.dp),
          focusedScale = 1.06f,
          borderWidth = 2.5.dp,
          onFocused = { isFocused = true },
        )
        .testTag("${tagPrefix}_${song.id}")
  ) {
    // 16:9 Thumbnail Container
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
      if (!song.thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
          model = song.thumbnailUrl,
          contentDescription = song.title,
          placeholder = painterResource(id = song.thumbnailRes),
          error = painterResource(id = song.thumbnailRes),
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Image(
          painter = painterResource(id = song.thumbnailRes),
          contentDescription = song.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      // Subtle bottom gradient for duration badge legibility
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, KoroBackground.copy(alpha = 0.65f))
              )
            )
      )

      // Favorite indicator badge
      if (isFavorite) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .padding(8.dp)
              .size(28.dp)
              .clip(CircleShape)
              .background(KoroBackground.copy(alpha = 0.75f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorit",
            tint = KoroCrimson,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      // Play badge on focus
      if (isFocused) {
        Box(
          modifier =
            Modifier.align(Alignment.Center)
              .size(44.dp)
              .clip(CircleShape)
              .background(KoroCrimson.copy(alpha = 0.92f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Putar",
            tint = Color.White,
            modifier = Modifier.size(26.dp),
          )
        }
      }

      // Duration Pill
      Box(
        modifier =
          Modifier.align(Alignment.BottomEnd)
            .padding(8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(KoroBackground.copy(alpha = 0.82f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
      ) {
        Text(
          text = song.duration,
          style = MaterialTheme.typography.labelSmall,
          color = KoroTextPrimary,
        )
      }
    }

    // Charcoal Info Footer: Song Title & Singer Name
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
      Text(
        text = song.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = KoroTextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(3.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = song.artist,
          style = MaterialTheme.typography.bodyMedium,
          color = KoroTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}
