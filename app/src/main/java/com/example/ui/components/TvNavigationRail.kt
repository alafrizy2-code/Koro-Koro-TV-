package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.KaraokeCategory
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroGold
import com.example.ui.theme.KoroRailBackground
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary

@Composable
fun TvNavigationRail(
  selectedCategory: KaraokeCategory,
  queueCount: Int,
  favoriteCount: Int,
  onSelectCategory: (KaraokeCategory) -> Unit,
  modifier: Modifier = Modifier,
) {
  var isRailFocused by remember { mutableStateOf(false) }

  val railWidth by
    animateDpAsState(
      targetValue = if (isRailFocused) 232.dp else 76.dp,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      label = "railWidth",
    )

  Column(
    modifier =
      modifier
        .width(railWidth)
        .fillMaxHeight()
        .background(
          Brush.horizontalGradient(
            colors = listOf(KoroRailBackground, KoroRailBackground.copy(alpha = 0.94f))
          )
        )
        .onFocusChanged { isRailFocused = it.hasFocus }
        .padding(vertical = 18.dp, horizontal = 12.dp),
    horizontalAlignment = Alignment.Start,
  ) {
    // Compact Brand Emblem in Rail
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors = listOf(KoroCrimson, KoroGold))),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "Logo Koro Koro TV",
          tint = Color.White,
          modifier = Modifier.size(24.dp),
        )
      }

      AnimatedVisibility(visible = isRailFocused, enter = fadeIn(), exit = fadeOut()) {
        Column(modifier = Modifier.padding(start = 12.dp)) {
          Text(
            text = "KORO KORO",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = KoroTextPrimary,
            maxLines = 1,
          )
          Text(
            text = "KARAOKE TV",
            style = MaterialTheme.typography.labelSmall,
            color = KoroGold,
            maxLines = 1,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Scrollable Navigation Items for 16:9 TV screens
    Column(
      modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      KaraokeCategory.entries.forEach { category ->
        val isSelected = selectedCategory == category
        val badgeCount =
          when (category) {
            KaraokeCategory.ANTREAN -> queueCount
            KaraokeCategory.FAVORIT -> favoriteCount
            else -> null
          }

        TvRailItem(
          category = category,
          isSelected = isSelected,
          isExpanded = isRailFocused,
          badgeCount = badgeCount,
          onClick = { onSelectCategory(category) },
        )
      }
    }
  }
}

@Composable
private fun TvRailItem(
  category: KaraokeCategory,
  isSelected: Boolean,
  isExpanded: Boolean,
  badgeCount: Int?,
  onClick: () -> Unit,
) {
  val bgColor =
    if (isSelected) KoroCrimson.copy(alpha = 0.22f) else Color.Transparent
  val contentColor = if (isSelected) KoroTextPrimary else KoroTextSecondary

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(48.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(bgColor)
        .tvFocusable(
          onClick = onClick,
          shape = RoundedCornerShape(12.dp),
          focusedScale = 1.03f,
          borderWidth = 2.dp,
          unfocusedBorderColor =
            if (isSelected) KoroCrimson.copy(alpha = 0.6f) else Color.Transparent,
        )
        .testTag("nav_${category.routeId}")
        .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = categoryIcon(category),
      contentDescription = category.label,
      tint = if (isSelected) KoroCrimson else contentColor,
      modifier = Modifier.size(22.dp),
    )

    AnimatedVisibility(visible = isExpanded, enter = fadeIn(), exit = fadeOut()) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = category.label,
          style = MaterialTheme.typography.labelLarge,
          color = contentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (badgeCount != null && badgeCount > 0) {
          Box(
            modifier =
              Modifier.clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) KoroCrimson else KoroCardElevated)
                .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Text(
              text = badgeCount.toString(),
              style = MaterialTheme.typography.labelSmall,
              color = KoroTextPrimary,
            )
          }
        }
      }
    }
  }
}

private fun categoryIcon(category: KaraokeCategory): ImageVector =
  when (category) {
    KaraokeCategory.SEARCH -> Icons.Default.Search
    KaraokeCategory.HOME -> Icons.Default.Home
    KaraokeCategory.INDONESIA -> Icons.Default.MusicNote
    KaraokeCategory.BARAT -> Icons.Default.Public
    KaraokeCategory.DANGDUT -> Icons.Default.Star
    KaraokeCategory.POPULER -> Icons.Default.LocalFireDepartment
    KaraokeCategory.FAVORIT -> Icons.Default.Favorite
    KaraokeCategory.RIWAYAT -> Icons.Default.History
    KaraokeCategory.ANTREAN -> Icons.AutoMirrored.Filled.QueueMusic
  }
