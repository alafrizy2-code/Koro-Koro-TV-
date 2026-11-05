package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.KoroCardCharcoal
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroDivider
import com.example.ui.theme.KoroEmerald
import com.example.ui.theme.KoroFocusBorder
import com.example.ui.theme.KoroGold
import com.example.ui.theme.KoroTextMuted
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun TvTopHeader(
  searchQuery: String,
  isApiConfigured: Boolean,
  isUsbMicConnected: Boolean,
  onSearchQueryChange: (String) -> Unit,
  onVoiceSearchClick: () -> Unit,
  onApiSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var currentTimeText by remember { mutableStateOf("") }
  var currentDateText by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    val localeId = Locale("id", "ID")
    val timeFormat = SimpleDateFormat("HH:mm", localeId)
    val dateFormat = SimpleDateFormat("EEEE, d MMM yyyy", localeId)
    while (true) {
      val now = Date()
      currentTimeText = timeFormat.format(now)
      currentDateText = dateFormat.format(now)
      delay(1000L)
    }
  }

  var isSearchFocused by remember { mutableStateOf(false) }
  val searchFocusRequester = remember { FocusRequester() }

  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    // Brand Logo: Koro Koro TV
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Koro Koro",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = KoroTextPrimary,
      )
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier =
          Modifier.clip(RoundedCornerShape(6.dp))
            .background(Brush.horizontalGradient(listOf(KoroCrimson, KoroGold)))
            .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = "TV",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Black,
          color = Color.White,
        )
      }
    }

    // Center Search Bar + Voice Search Button
    Row(
      modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Row(
        modifier =
          Modifier.weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSearchFocused) KoroCardElevated else KoroCardCharcoal)
            .border(
              width = if (isSearchFocused) 2.dp else 1.dp,
              color = if (isSearchFocused) KoroFocusBorder else KoroDivider,
              shape = RoundedCornerShape(24.dp),
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Cari Lagu",
          tint = if (isSearchFocused) KoroGold else KoroTextSecondary,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
          if (searchQuery.isEmpty()) {
            Text(
              text = "Cari lagu karaoke, penyanyi, atau genre...",
              style = MaterialTheme.typography.bodyMedium,
              color = KoroTextMuted,
              maxLines = 1,
            )
          }
          BasicTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = KoroTextPrimary),
            cursorBrush = SolidColor(KoroCrimson),
            modifier =
              Modifier.fillMaxWidth()
                .focusRequester(searchFocusRequester)
                .onFocusChanged { isSearchFocused = it.isFocused }
                .testTag("search_input"),
          )
        }
        if (searchQuery.isNotEmpty()) {
          Box(
            modifier =
              Modifier.size(32.dp)
                .clip(CircleShape)
                .tvFocusable(
                  onClick = { onSearchQueryChange("") },
                  shape = CircleShape,
                  focusedScale = 1.1f,
                )
                .testTag("clear_search_button"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Hapus Pencarian",
              tint = KoroTextSecondary,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Voice Search Remote Button (Ready for Tahap 3)
      Box(
        modifier =
          Modifier.size(48.dp)
            .clip(CircleShape)
            .background(KoroCardCharcoal)
            .tvFocusable(
              onClick = onVoiceSearchClick,
              shape = CircleShape,
              focusedScale = 1.08f,
              unfocusedBorderColor = KoroDivider,
            )
            .testTag("voice_search_button"),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Mic,
          contentDescription = "Pencarian Suara",
          tint = KoroCrimson,
          modifier = Modifier.size(22.dp),
        )
      }
    }

    // Right: USB Mic Status, API Key Settings & Real-time Clock + Date
    Row(verticalAlignment = Alignment.CenterVertically) {
      Row(
        modifier =
          Modifier.height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(KoroCardCharcoal)
            .border(
              1.dp,
              if (isUsbMicConnected) KoroEmerald.copy(alpha = 0.6f) else KoroDivider,
              RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.size(8.dp)
              .clip(CircleShape)
              .background(if (isUsbMicConnected) KoroEmerald else KoroTextMuted)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isUsbMicConnected) "USB Mic" else "Mic Offline",
          style = MaterialTheme.typography.labelSmall,
          color = if (isUsbMicConnected) KoroEmerald else KoroTextSecondary,
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Row(
        modifier =
          Modifier.height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(KoroCardCharcoal)
            .tvFocusable(
              onClick = onApiSettingsClick,
              shape = RoundedCornerShape(20.dp),
              focusedScale = 1.05f,
              unfocusedBorderColor = KoroDivider,
            )
            .testTag("api_settings_button")
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = "Pengaturan YouTube API",
          tint = if (isApiConfigured) KoroEmerald else KoroGold,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isApiConfigured) "YouTube API" else "Atur API",
          style = MaterialTheme.typography.labelSmall,
          color = if (isApiConfigured) KoroEmerald else KoroGold,
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = currentTimeText,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = KoroTextPrimary,
        )
        Text(
          text = currentDateText,
          style = MaterialTheme.typography.bodyMedium,
          color = KoroTextSecondary,
        )
      }
    }
  }
}
