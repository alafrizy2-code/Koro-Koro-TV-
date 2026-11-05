package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.KaraokeSong
import com.example.ui.theme.KoroBackground
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroGold
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary

@Composable
fun HeroKaraokeBanner(
  song: KaraokeSong,
  isFavorite: Boolean,
  onPlayClick: () -> Unit,
  onAddToQueueClick: () -> Unit,
  onToggleFavoriteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(248.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(KoroCardElevated)
  ) {
    // 16:9 Widescreen Stage Artwork
    Image(
      painter = painterResource(id = song.thumbnailRes),
      contentDescription = song.title,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    // Multi-stop YouTube TV Cinematic Scrim
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors =
                listOf(
                  KoroBackground.copy(alpha = 0.96f),
                  KoroBackground.copy(alpha = 0.78f),
                  KoroBackground.copy(alpha = 0.25f),
                )
            )
          )
    )
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, KoroBackground.copy(alpha = 0.85f))
            )
          )
    )

    // Foreground Content & Action Buttons
    Column(
      modifier = Modifier.fillMaxSize().padding(28.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Pill Badge
      Row(
        modifier =
          Modifier.clip(RoundedCornerShape(20.dp))
            .background(KoroCrimson.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Star,
          contentDescription = null,
          tint = KoroGold,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "VIDEO KARAOKE UNGGULAN • ${song.category.label.uppercase()}",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
        )
      }

      // Song Title, Artist & Action Buttons
      Column {
        Text(
          text = song.title,
          style = MaterialTheme.typography.displaySmall,
          color = KoroTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${song.artist}  •  Durasi ${song.duration}  •  Lirik HD & Audio Stereo",
          style = MaterialTheme.typography.bodyLarge,
          color = KoroTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // Primary Play Button
          Row(
            modifier =
              Modifier.height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KoroCrimson)
                .tvFocusable(
                  onClick = onPlayClick,
                  shape = RoundedCornerShape(12.dp),
                  focusedScale = 1.06f,
                )
                .testTag("hero_play_button")
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Putar Sekarang",
              tint = Color.White,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Putar Sekarang",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }

          // Add to Queue Button
          Row(
            modifier =
              Modifier.height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KoroCardElevated.copy(alpha = 0.9f))
                .tvFocusable(
                  onClick = onAddToQueueClick,
                  shape = RoundedCornerShape(12.dp),
                  focusedScale = 1.05f,
                )
                .testTag("hero_queue_button")
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
              contentDescription = "Tambah ke Antrean",
              tint = KoroTextPrimary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "+ Antrean",
              style = MaterialTheme.typography.labelLarge,
              color = KoroTextPrimary,
            )
          }

          // Favorite Toggle Button
          Row(
            modifier =
              Modifier.height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KoroCardElevated.copy(alpha = 0.9f))
                .tvFocusable(
                  onClick = onToggleFavoriteClick,
                  shape = RoundedCornerShape(12.dp),
                  focusedScale = 1.05f,
                )
                .testTag("hero_favorite_button")
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector =
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorit",
              tint = if (isFavorite) KoroCrimson else KoroTextPrimary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isFavorite) "Difavoritkan" else "Favorit",
              style = MaterialTheme.typography.labelLarge,
              color = KoroTextPrimary,
            )
          }
        }
      }
    }
  }
}
