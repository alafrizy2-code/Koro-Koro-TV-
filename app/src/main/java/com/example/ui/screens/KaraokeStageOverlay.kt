package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.audio.UsbMicStatus
import com.example.data.SampleKaraokeRepository
import com.example.model.KaraokeSong
import com.example.model.MixerSettings
import com.example.model.VocalPreset
import com.example.ui.components.tvFocusable
import com.example.ui.player.YouTubeKaraokePlayer
import com.example.ui.player.YouTubePlayerController
import com.example.ui.theme.KoroBackground
import com.example.ui.theme.KoroCardCharcoal
import com.example.ui.theme.KoroCardElevated
import com.example.ui.theme.KoroCrimson
import com.example.ui.theme.KoroDivider
import com.example.ui.theme.KoroEmerald
import com.example.ui.theme.KoroGold
import com.example.ui.theme.KoroSurface
import com.example.ui.theme.KoroTextMuted
import com.example.ui.theme.KoroTextPrimary
import com.example.ui.theme.KoroTextSecondary

private enum class PlayerSidePanel {
  NONE,
  VOCAL_EFFECTS,
  QUEUE,
}

@Composable
fun KaraokeStageOverlay(
  song: KaraokeSong,
  isPlaying: Boolean,
  isFavorite: Boolean,
  queueSongs: List<KaraokeSong>,
  selectedPreset: VocalPreset,
  mixerSettings: MixerSettings,
  usbMicStatus: UsbMicStatus,
  onPlayingChanged: (Boolean) -> Unit,
  onNextInQueue: () -> Unit,
  onPlayQueuedSong: (KaraokeSong) -> Unit,
  onRemoveFromQueue: (String) -> Unit,
  onToggleFavorite: () -> Unit,
  onAddToQueue: () -> Unit,
  onSelectPreset: (Int) -> Unit,
  onAdjustMicVolume: (Int) -> Unit,
  onToggleMicMute: () -> Unit,
  onAdjustEcho: (Int) -> Unit,
  onAdjustReverb: (Int) -> Unit,
  onAdjustBass: (Int) -> Unit,
  onAdjustTreble: (Int) -> Unit,
  onAdjustPitch: (Int) -> Unit,
  onToggleMicMonitor: () -> Unit,
  onRequestUsbPermission: () -> Unit,
  onRequestAudioPermission: () -> Unit,
  onClose: () -> Unit,
) {
  var activePanel by remember { mutableStateOf(PlayerSidePanel.NONE) }
  var isFullscreenMode by remember { mutableStateOf(false) }

  BackHandler {
    when {
      activePanel != PlayerSidePanel.NONE -> activePanel = PlayerSidePanel.NONE
      isFullscreenMode -> isFullscreenMode = false
      else -> onClose()
    }
  }

  val playerController = remember { YouTubePlayerController() }
  var currentSeconds by remember(song.id) { mutableFloatStateOf(0f) }
  var durationSeconds by remember(song.id) { mutableFloatStateOf(240f) }
  var playerErrorCode by remember(song.id) { mutableStateOf<Int?>(null) }

  val togglePlayPauseAction = {
    if (isPlaying) {
      playerController.pause()
      onPlayingChanged(false)
    } else {
      playerController.play()
      onPlayingChanged(true)
    }
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color.Black)
        .onPreviewKeyEvent { event ->
          if (event.type == KeyEventType.KeyUp) {
            // Any D-pad navigation wakes up HUD if in fullscreen mode
            if (
              isFullscreenMode &&
                (event.key == Key.DirectionUp ||
                  event.key == Key.DirectionDown ||
                  event.key == Key.DirectionLeft ||
                  event.key == Key.DirectionRight)
            ) {
              isFullscreenMode = false
              return@onPreviewKeyEvent true
            }

            when (event.key) {
              Key.Zero, Key.NumPad0 -> { onSelectPreset(0); true }
              Key.One, Key.NumPad1 -> { onSelectPreset(1); true }
              Key.Two, Key.NumPad2 -> { onSelectPreset(2); true }
              Key.Three, Key.NumPad3 -> { onSelectPreset(3); true }
              Key.Four, Key.NumPad4 -> { onSelectPreset(4); true }
              Key.Five, Key.NumPad5 -> { onSelectPreset(5); true }
              Key.Six, Key.NumPad6 -> { onSelectPreset(6); true }
              Key.Seven, Key.NumPad7 -> { onSelectPreset(7); true }
              Key.Eight, Key.NumPad8 -> { onSelectPreset(8); true }
              Key.Nine, Key.NumPad9 -> { onSelectPreset(9); true }
              Key.VolumeUp -> { onAdjustMicVolume(5); true }
              Key.VolumeDown -> { onAdjustMicVolume(-5); true }
              Key.VolumeMute, Key.M -> { onToggleMicMute(); true }
              Key.Spacebar, Key.MediaPlayPause -> { togglePlayPauseAction(); true }
              Key.DirectionCenter, Key.Enter -> {
                if (isFullscreenMode) {
                  togglePlayPauseAction()
                  true
                } else {
                  false
                }
              }
              Key.Back, Key.Escape -> {
                when {
                  activePanel != PlayerSidePanel.NONE -> activePanel = PlayerSidePanel.NONE
                  isFullscreenMode -> isFullscreenMode = false
                  else -> onClose()
                }
                true
              }
              else -> false
            }
          } else {
            false
          }
        }
  ) {
    // 1. Official YouTube IFrame Player (Full 16:9 Stage)
    YouTubeKaraokePlayer(
      videoId = song.youtubeVideoId,
      controller = playerController,
      onPlayingChanged = onPlayingChanged,
      onProgressChanged = { cur, dur ->
        currentSeconds = cur
        if (dur > 0f) durationSeconds = dur
      },
      onVideoEnded = {
        if (queueSongs.isNotEmpty()) {
          onNextInQueue()
        } else {
          onPlayingChanged(false)
        }
      },
      onPlayerError = { code -> playerErrorCode = code },
      modifier =
        Modifier.fillMaxSize().clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
        ) {
          if (isFullscreenMode) isFullscreenMode = false
        },
    )

    // Error Banner if video owner disabled embedding
    if (playerErrorCode != null) {
      Box(
        modifier =
          Modifier.align(Alignment.Center)
            .clip(RoundedCornerShape(16.dp))
            .background(KoroCardCharcoal.copy(alpha = 0.95f))
            .border(1.5.dp, KoroCrimson, RoundedCornerShape(16.dp))
            .padding(24.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Video YouTube tidak dapat disematkan (Kode: $playerErrorCode)",
            style = MaterialTheme.typography.titleMedium,
            color = KoroTextPrimary,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Silakan pilih lagu lain atau lanjutkan ke antrean berikutnya.",
            style = MaterialTheme.typography.bodyMedium,
            color = KoroTextSecondary,
          )
        }
      }
    }

    // 2. HUD Overlay (Hidden in Fullscreen Mode)
    AnimatedVisibility(
      visible = !isFullscreenMode,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize(),
    ) {
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors =
                  listOf(
                    KoroBackground.copy(alpha = 0.82f),
                    Color.Transparent,
                    Color.Transparent,
                    KoroBackground.copy(alpha = 0.92f),
                  )
              )
            )
      ) {
        Column(
          modifier = Modifier.fillMaxSize().padding(24.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          // Top Bar: Back, Song Title & Artist, Preset & Mic Status
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier =
                  Modifier.height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KoroCardCharcoal.copy(alpha = 0.9f))
                    .tvFocusable(onClick = onClose, shape = RoundedCornerShape(12.dp))
                    .testTag("stage_back_button")
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Kembali",
                  tint = KoroTextPrimary,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Kembali",
                  style = MaterialTheme.typography.labelLarge,
                  color = KoroTextPrimary,
                )
              }

              Spacer(modifier = Modifier.width(18.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = song.title,
                  style = MaterialTheme.typography.headlineMedium,
                  color = KoroTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${song.artist}  •  YouTube Official Player",
                  style = MaterialTheme.typography.bodyMedium,
                  color = KoroTextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            // Right Status Pills
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier =
                  Modifier.clip(RoundedCornerShape(12.dp))
                    .background(KoroCardCharcoal.copy(alpha = 0.9f))
                    .border(1.dp, KoroGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Default.GraphicEq,
                  contentDescription = "Efek Vokal",
                  tint = KoroGold,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "[${selectedPreset.number}] ${selectedPreset.name}",
                  style = MaterialTheme.typography.labelLarge,
                  color = KoroGold,
                )
              }

              Row(
                modifier =
                  Modifier.clip(RoundedCornerShape(12.dp))
                    .background(KoroCardCharcoal.copy(alpha = 0.9f))
                    .border(1.dp, KoroDivider, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector =
                    if (mixerSettings.isMicMuted) Icons.AutoMirrored.Filled.VolumeOff
                    else Icons.Default.Mic,
                  contentDescription = "Mikrofon",
                  tint = if (mixerSettings.isMicMuted) KoroCrimson else KoroEmerald,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text =
                    if (mixerSettings.isMicMuted) "MUTE"
                    else "${mixerSettings.micVolume}%",
                  style = MaterialTheme.typography.labelLarge,
                  color =
                    if (mixerSettings.isMicMuted) KoroCrimson else KoroTextPrimary,
                )
              }
            }
          }

          // Bottom Control Bar
          Column(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(KoroCardCharcoal.copy(alpha = 0.92f))
                .border(1.dp, KoroDivider, RoundedCornerShape(18.dp))
                .padding(18.dp)
          ) {
            val progressFraction =
              if (durationSeconds > 0f) (currentSeconds / durationSeconds).coerceIn(0f, 1f)
              else 0f

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = formatSeconds(currentSeconds.toInt()),
                style = MaterialTheme.typography.labelSmall,
                color = KoroTextSecondary,
              )
              LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = KoroCrimson,
                trackColor = KoroCardElevated,
              )
              Text(
                text = formatSeconds(durationSeconds.toInt()),
                style = MaterialTheme.typography.labelSmall,
                color = KoroTextSecondary,
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              // Left: Play/Pause, Next, Favorite, Add Queue
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Box(
                  modifier =
                    Modifier.size(48.dp)
                      .clip(CircleShape)
                      .background(KoroCrimson)
                      .tvFocusable(onClick = togglePlayPauseAction, shape = CircleShape)
                      .testTag("stage_play_pause_button"),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector =
                      if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Jeda" else "Putar",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                  )
                }

                if (queueSongs.isNotEmpty()) {
                  Row(
                    modifier =
                      Modifier.height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KoroCardElevated)
                        .tvFocusable(
                          onClick = onNextInQueue,
                          shape = RoundedCornerShape(12.dp),
                        )
                        .testTag("stage_next_button")
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      imageVector = Icons.Default.SkipNext,
                      contentDescription = "Lagu Berikutnya",
                      tint = KoroTextPrimary,
                      modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Berikutnya",
                      style = MaterialTheme.typography.labelLarge,
                      color = KoroTextPrimary,
                    )
                  }
                }

                Box(
                  modifier =
                    Modifier.size(48.dp)
                      .clip(CircleShape)
                      .background(KoroCardElevated)
                      .tvFocusable(onClick = onToggleFavorite, shape = CircleShape)
                      .testTag("stage_favorite_button"),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector =
                      if (isFavorite) Icons.Default.Favorite
                      else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorit",
                    tint = if (isFavorite) KoroCrimson else KoroTextPrimary,
                    modifier = Modifier.size(22.dp),
                  )
                }

                Box(
                  modifier =
                    Modifier.size(48.dp)
                      .clip(CircleShape)
                      .background(KoroCardElevated)
                      .tvFocusable(onClick = onAddToQueue, shape = CircleShape)
                      .testTag("stage_queue_button"),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = "Tambah ke Antrean",
                    tint = KoroTextPrimary,
                    modifier = Modifier.size(22.dp),
                  )
                }
              }

              // Right: Panel Efek Vokal, Panel Antrean, Layar Penuh
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                // Non-interrupting Vocal Effects Panel Toggle
                Row(
                  modifier =
                    Modifier.height(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                        if (activePanel == PlayerSidePanel.VOCAL_EFFECTS) KoroCrimson
                        else KoroCardElevated
                      )
                      .tvFocusable(
                        onClick = {
                          activePanel =
                            if (activePanel == PlayerSidePanel.VOCAL_EFFECTS)
                              PlayerSidePanel.NONE
                            else PlayerSidePanel.VOCAL_EFFECTS
                        },
                        shape = RoundedCornerShape(12.dp),
                      )
                      .testTag("stage_vocal_panel_button")
                      .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Efek Vokal",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Efek Vokal",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                  )
                }

                // Queue Drawer Toggle
                Row(
                  modifier =
                    Modifier.height(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                        if (activePanel == PlayerSidePanel.QUEUE) KoroCrimson
                        else KoroCardElevated
                      )
                      .tvFocusable(
                        onClick = {
                          activePanel =
                            if (activePanel == PlayerSidePanel.QUEUE) PlayerSidePanel.NONE
                            else PlayerSidePanel.QUEUE
                        },
                        shape = RoundedCornerShape(12.dp),
                      )
                      .testTag("stage_queue_panel_button")
                      .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Antrean Lagu",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Antrean (${queueSongs.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                  )
                }

                // Fullscreen Mode Toggle
                Row(
                  modifier =
                    Modifier.height(48.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(KoroCardElevated)
                      .tvFocusable(
                        onClick = {
                          activePanel = PlayerSidePanel.NONE
                          isFullscreenMode = true
                        },
                        shape = RoundedCornerShape(12.dp),
                      )
                      .testTag("stage_fullscreen_button")
                      .padding(horizontal = 14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector =
                      if (isFullscreenMode) Icons.Default.FullscreenExit
                      else Icons.Default.Fullscreen,
                    contentDescription = "Layar Penuh",
                    tint = KoroTextPrimary,
                    modifier = Modifier.size(20.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Layar Penuh",
                    style = MaterialTheme.typography.labelLarge,
                    color = KoroTextPrimary,
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Non-Interrupting Side Panel (Vocal Effects OR Queue)
    AnimatedVisibility(
      visible = activePanel != PlayerSidePanel.NONE,
      enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
      exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
      modifier = Modifier.align(Alignment.CenterEnd).padding(20.dp),
    ) {
      when (activePanel) {
        PlayerSidePanel.VOCAL_EFFECTS ->
          VocalEffectsSidePanel(
            selectedPreset = selectedPreset,
            mixerSettings = mixerSettings,
            usbMicStatus = usbMicStatus,
            onSelectPreset = onSelectPreset,
            onAdjustMicVolume = onAdjustMicVolume,
            onToggleMicMute = onToggleMicMute,
            onAdjustEcho = onAdjustEcho,
            onAdjustReverb = onAdjustReverb,
            onAdjustBass = onAdjustBass,
            onAdjustTreble = onAdjustTreble,
            onAdjustPitch = onAdjustPitch,
            onToggleMicMonitor = onToggleMicMonitor,
            onRequestUsbPermission = onRequestUsbPermission,
            onRequestAudioPermission = onRequestAudioPermission,
            onClosePanel = { activePanel = PlayerSidePanel.NONE },
          )
        PlayerSidePanel.QUEUE ->
          QueueSidePanel(
            queueSongs = queueSongs,
            onPlaySong = { queued ->
              onPlayQueuedSong(queued)
              activePanel = PlayerSidePanel.NONE
            },
            onRemoveSong = onRemoveFromQueue,
            onClosePanel = { activePanel = PlayerSidePanel.NONE },
          )
        PlayerSidePanel.NONE -> Unit
      }
    }
  }
}

@Composable
private fun VocalEffectsSidePanel(
  selectedPreset: VocalPreset,
  mixerSettings: MixerSettings,
  usbMicStatus: UsbMicStatus,
  onSelectPreset: (Int) -> Unit,
  onAdjustMicVolume: (Int) -> Unit,
  onToggleMicMute: () -> Unit,
  onAdjustEcho: (Int) -> Unit,
  onAdjustReverb: (Int) -> Unit,
  onAdjustBass: (Int) -> Unit,
  onAdjustTreble: (Int) -> Unit,
  onAdjustPitch: (Int) -> Unit,
  onToggleMicMonitor: () -> Unit,
  onRequestUsbPermission: () -> Unit,
  onRequestAudioPermission: () -> Unit,
  onClosePanel: () -> Unit,
) {
  Column(
    modifier =
      Modifier.width(380.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(20.dp))
        .background(KoroSurface.copy(alpha = 0.96f))
        .border(1.5.dp, KoroDivider, RoundedCornerShape(20.dp))
        .padding(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = "Panel Efek Vokal",
          style = MaterialTheme.typography.titleLarge,
          color = KoroTextPrimary,
        )
        Text(
          text = "Preset [${selectedPreset.number}]: ${selectedPreset.name}",
          style = MaterialTheme.typography.bodyMedium,
          color = KoroGold,
        )
      }
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(KoroCardElevated)
            .tvFocusable(onClick = onClosePanel, shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Tutup Panel",
          tint = KoroTextPrimary,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    Column(
      modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // USB Mic Status & Real-Time Level Meter
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KoroCardCharcoal)
            .padding(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text =
              if (usbMicStatus.isUsbMicConnected) "USB: ${usbMicStatus.deviceName}"
              else "USB Mic: Terputus (Mic Internal)",
            style = MaterialTheme.typography.labelLarge,
            color = if (usbMicStatus.isUsbMicConnected) KoroEmerald else KoroGold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          if (!usbMicStatus.hasRecordAudioPermission) {
            Box(
              modifier =
                Modifier.clip(RoundedCornerShape(8.dp))
                  .background(KoroCrimson)
                  .tvFocusable(
                    onClick = onRequestAudioPermission,
                    shape = RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Izinkan Mic",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
              )
            }
          } else if (usbMicStatus.isUsbMicConnected && !usbMicStatus.hasUsbPermission) {
            Box(
              modifier =
                Modifier.clip(RoundedCornerShape(8.dp))
                  .background(KoroGold)
                  .tvFocusable(
                    onClick = onRequestUsbPermission,
                    shape = RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Izinkan USB",
                style = MaterialTheme.typography.labelSmall,
                color = KoroBackground,
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
          progress = { (usbMicStatus.inputLevel * 2.5f).coerceIn(0f, 1f) },
          modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
          color = KoroEmerald,
          trackColor = KoroCardElevated,
        )
      }

      // 0-9 Preset Grid (2 rows of 5)
      Text(
        text = "Preset Vokal (Tombol Remote 0–9)",
        style = MaterialTheme.typography.labelLarge,
        color = KoroTextSecondary,
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleKaraokeRepository.vocalPresets.chunked(5).forEach { rowPresets ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            rowPresets.forEach { preset ->
              val isSelected = preset.number == selectedPreset.number
              Box(
                modifier =
                  Modifier.weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) KoroCrimson else KoroCardElevated)
                    .tvFocusable(
                      onClick = { onSelectPreset(preset.number) },
                      shape = RoundedCornerShape(10.dp),
                    ),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "${preset.number}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else KoroTextPrimary,
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Mic Volume & Mute
      MixerStepperRow(
        label = "Volume Mikrofon",
        valueText = if (mixerSettings.isMicMuted) "MUTE" else "${mixerSettings.micVolume}%",
        onDecrease = { onAdjustMicVolume(-10) },
        onIncrease = { onAdjustMicVolume(10) },
      )

      // Mic Monitor ON/OFF
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (mixerSettings.isMicMonitorEnabled) KoroEmerald.copy(alpha = 0.2f)
              else KoroCardElevated
            )
            .tvFocusable(onClick = onToggleMicMonitor, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Headphones,
            contentDescription = "Mic Monitor",
            tint = if (mixerSettings.isMicMonitorEnabled) KoroEmerald else KoroTextSecondary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Mic Monitor (Speaker TV)",
            style = MaterialTheme.typography.labelLarge,
            color = KoroTextPrimary,
          )
        }
        Text(
          text = if (mixerSettings.isMicMonitorEnabled) "ON" else "OFF",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = if (mixerSettings.isMicMonitorEnabled) KoroEmerald else KoroTextMuted,
        )
      }

      MixerStepperRow(
        label = "Echo Level",
        valueText = "${mixerSettings.echoLevel}%",
        onDecrease = { onAdjustEcho(-10) },
        onIncrease = { onAdjustEcho(10) },
      )
      MixerStepperRow(
        label = "Reverb Level",
        valueText = "${mixerSettings.reverbLevel}%",
        onDecrease = { onAdjustReverb(-10) },
        onIncrease = { onAdjustReverb(10) },
      )
      MixerStepperRow(
        label = "Bass",
        valueText = "${mixerSettings.bassLevel}%",
        onDecrease = { onAdjustBass(-10) },
        onIncrease = { onAdjustBass(10) },
      )
      MixerStepperRow(
        label = "Treble",
        valueText = "${mixerSettings.trebleLevel}%",
        onDecrease = { onAdjustTreble(-10) },
        onIncrease = { onAdjustTreble(10) },
      )
      MixerStepperRow(
        label = "Pitch Control",
        valueText =
          if (mixerSettings.pitchShift > 0) "+${mixerSettings.pitchShift}"
          else "${mixerSettings.pitchShift}",
        onDecrease = { onAdjustPitch(-1) },
        onIncrease = { onAdjustPitch(1) },
      )

      // Mute Button
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (mixerSettings.isMicMuted) KoroCrimson else KoroCardElevated)
            .tvFocusable(onClick = onToggleMicMute, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector =
            if (mixerSettings.isMicMuted) Icons.AutoMirrored.Filled.VolumeOff
            else Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = "Mute Mikrofon",
          tint = Color.White,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (mixerSettings.isMicMuted) "Aktifkan Mikrofon" else "Bisukan Mikrofon (Mute)",
          style = MaterialTheme.typography.labelLarge,
          color = Color.White,
        )
      }
    }
  }
}

@Composable
private fun MixerStepperRow(
  label: String,
  valueText: String,
  onDecrease: () -> Unit,
  onIncrease: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(KoroCardCharcoal)
        .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = KoroTextSecondary,
      )
      Text(
        text = valueText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = KoroTextPrimary,
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KoroCardElevated)
            .tvFocusable(onClick = onDecrease, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = "-", style = MaterialTheme.typography.titleLarge, color = KoroTextPrimary)
      }
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KoroCardElevated)
            .tvFocusable(onClick = onIncrease, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Text(text = "+", style = MaterialTheme.typography.titleLarge, color = KoroTextPrimary)
      }
    }
  }
}

@Composable
private fun QueueSidePanel(
  queueSongs: List<KaraokeSong>,
  onPlaySong: (KaraokeSong) -> Unit,
  onRemoveSong: (String) -> Unit,
  onClosePanel: () -> Unit,
) {
  Column(
    modifier =
      Modifier.width(380.dp)
        .fillMaxHeight()
        .clip(RoundedCornerShape(20.dp))
        .background(KoroSurface.copy(alpha = 0.96f))
        .border(1.5.dp, KoroDivider, RoundedCornerShape(20.dp))
        .padding(20.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column {
        Text(
          text = "Antrean Lagu",
          style = MaterialTheme.typography.titleLarge,
          color = KoroTextPrimary,
        )
        Text(
          text = "${queueSongs.size} lagu berikutnya",
          style = MaterialTheme.typography.bodyMedium,
          color = KoroTextSecondary,
        )
      }
      Box(
        modifier =
          Modifier.size(44.dp)
            .clip(CircleShape)
            .background(KoroCardElevated)
            .tvFocusable(onClick = onClosePanel, shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Tutup Antrean",
          tint = KoroTextPrimary,
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    if (queueSongs.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
          text = "Antrean lagu kosong.",
          style = MaterialTheme.typography.bodyLarge,
          color = KoroTextMuted,
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        itemsIndexed(items = queueSongs, key = { _, s -> "qpanel_${s.id}" }) { index, qSong ->
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KoroCardCharcoal)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier =
                Modifier.weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .tvFocusable(onClick = { onPlaySong(qSong) }, shape = RoundedCornerShape(8.dp))
                  .padding(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.titleMedium,
                color = KoroGold,
                modifier = Modifier.width(28.dp),
              )
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = qSong.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = KoroTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${qSong.artist} • ${qSong.duration}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = KoroTextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
              modifier =
                Modifier.size(44.dp)
                  .clip(CircleShape)
                  .background(KoroCardElevated)
                  .tvFocusable(
                    onClick = { onRemoveSong(qSong.id) },
                    shape = CircleShape,
                  ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Hapus dari Antrean",
                tint = KoroCrimson,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
      }
    }
  }
}

private fun formatSeconds(totalSec: Int): String {
  val mins = totalSec / 60
  val secs = totalSec % 60
  return String.format("%02d:%02d", mins, secs)
}
