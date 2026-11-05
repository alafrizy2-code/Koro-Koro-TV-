package com.example.audio

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.model.MixerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class UsbMicStatus(
  val isUsbMicConnected: Boolean = false,
  val deviceName: String = "Tidak ada mikrofon USB",
  val hasUsbPermission: Boolean = false,
  val hasRecordAudioPermission: Boolean = false,
  val isAudioEngineRunning: Boolean = false,
  val inputLevel: Float = 0f,
)

class UsbMicrophoneManager(private val context: Context) {

  private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val mainHandler = Handler(Looper.getMainLooper())

  private val dspEngine = VocalDspEngine(SAMPLE_RATE)

  private val _status = MutableStateFlow(UsbMicStatus())
  val status: StateFlow<UsbMicStatus> = _status.asStateFlow()

  @Volatile private var currentSettings: MixerSettings = MixerSettings()
  private var audioLoopJob: Job? = null
  private var levelPollJob: Job? = null

  companion object {
    private const val SAMPLE_RATE = 48000
    private const val ACTION_USB_PERMISSION = "com.korokoro.tv.USB_PERMISSION"
  }

  private val usbReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        when (intent.action) {
          ACTION_USB_PERMISSION,
          UsbManager.ACTION_USB_DEVICE_ATTACHED,
          UsbManager.ACTION_USB_DEVICE_DETACHED -> {
            refreshDeviceStatus()
          }
        }
      }
    }

  private val audioDeviceCallback =
    object : AudioDeviceCallback() {
      override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
        refreshDeviceStatus()
      }

      override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
        refreshDeviceStatus()
      }
    }

  init {
    val filter =
      IntentFilter().apply {
        addAction(ACTION_USB_PERMISSION)
        addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
      }
    ContextCompat.registerReceiver(
      context,
      usbReceiver,
      filter,
      ContextCompat.RECEIVER_NOT_EXPORTED,
    )
    audioManager.registerAudioDeviceCallback(audioDeviceCallback, mainHandler)
    refreshDeviceStatus()
  }

  fun updateMixerSettings(settings: MixerSettings) {
    currentSettings = settings
  }

  fun refreshDeviceStatus() {
    val hasRecordPerm =
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED

    val usbAudioDevice = findUsbAudioHostDevice()
    val audioInputDevice = findUsbAudioInputInfo()

    val isConnected = usbAudioDevice != null || audioInputDevice != null
    val name =
      audioInputDevice?.productName?.toString()?.takeIf { it.isNotBlank() }
        ?: usbAudioDevice?.productName?.takeIf { it.isNotBlank() }
        ?: if (isConnected) "Mikrofon USB Eksternal" else "Tidak ada mikrofon USB"

    val hasUsbPerm = usbAudioDevice?.let { usbManager.hasPermission(it) } ?: isConnected

    _status.update {
      it.copy(
        isUsbMicConnected = isConnected,
        deviceName = name,
        hasUsbPermission = hasUsbPerm,
        hasRecordAudioPermission = hasRecordPerm,
      )
    }

    if (hasRecordPerm && audioLoopJob == null) {
      startAudioEngine()
    }
  }

  fun requestUsbPermissionIfNeeded() {
    val device = findUsbAudioHostDevice() ?: return
    if (!usbManager.hasPermission(device)) {
      val flags =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
      val permissionIntent =
        PendingIntent.getBroadcast(context, 0, Intent(ACTION_USB_PERMISSION), flags)
      usbManager.requestPermission(device, permissionIntent)
    }
  }

  private fun findUsbAudioHostDevice(): UsbDevice? {
    return usbManager.deviceList.values.firstOrNull { device ->
      (0 until device.interfaceCount).any { idx ->
        device.getInterface(idx).interfaceClass == UsbConstants.USB_CLASS_AUDIO
      }
    }
  }

  private fun findUsbAudioInputInfo(): AudioDeviceInfo? {
    val inputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
    return inputs.firstOrNull {
      it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
        it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
          it.type == AudioDeviceInfo.TYPE_BLE_HEADSET)
    }
  }

  @SuppressLint("MissingPermission")
  fun startAudioEngine() {
    if (audioLoopJob != null) return
    val hasRecordPerm =
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    if (!hasRecordPerm) return

    audioLoopJob =
      scope.launch {
        val minRecBuf =
          AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )
        val minTrackBuf =
          AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
          )

        if (minRecBuf <= 0 || minTrackBuf <= 0) return@launch

        val frameSize = 512
        val recBufferSize = (minRecBuf * 2).coerceAtLeast(frameSize * 4)
        val trackBufferSize = (minTrackBuf * 2).coerceAtLeast(frameSize * 4)

        var audioRecord: AudioRecord? = null
        var audioTrack: AudioTrack? = null

        try {
          audioRecord =
            AudioRecord(
              MediaRecorder.AudioSource.VOICE_COMMUNICATION,
              SAMPLE_RATE,
              AudioFormat.CHANNEL_IN_MONO,
              AudioFormat.ENCODING_PCM_16BIT,
              recBufferSize,
            )

          // Prefer USB Microphone if connected
          findUsbAudioInputInfo()?.let { usbInfo ->
            audioRecord.preferredDevice = usbInfo
          }

          val trackBuilder =
            AudioTrack.Builder()
              .setAudioAttributes(
                AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_MEDIA)
                  .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                  .build()
              )
              .setAudioFormat(
                AudioFormat.Builder()
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setSampleRate(SAMPLE_RATE)
                  .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                  .build()
              )
              .setBufferSizeInBytes(trackBufferSize)
              .setTransferMode(AudioTrack.MODE_STREAM)

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            trackBuilder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
          }

          audioTrack = trackBuilder.build()

          // Do not activate monitoring before audio paths are initialized
          if (
            audioRecord.state != AudioRecord.STATE_INITIALIZED ||
              audioTrack.state != AudioTrack.STATE_INITIALIZED
          ) {
            return@launch
          }

          audioRecord.startRecording()
          audioTrack.play()
          _status.update { it.copy(isAudioEngineRunning = true) }

          val inSamples = ShortArray(frameSize)
          val outSamples = ShortArray(frameSize)

          while (isActive) {
            val readCount = audioRecord.read(inSamples, 0, frameSize)
            if (readCount > 0) {
              val settings = currentSettings
              dspEngine.processBuffer(inSamples, outSamples, readCount, settings)

              // Only output to TV speakers when Mic Monitor is ON and audio path is ready
              if (settings.isMicMonitorEnabled && !settings.isMicMuted) {
                audioTrack.write(outSamples, 0, readCount)
              }
            }
          }
        } catch (_: Exception) {
          // Graceful handling if audio hardware is unavailable
        } finally {
          _status.update { it.copy(isAudioEngineRunning = false, inputLevel = 0f) }
          runCatching {
            audioRecord?.stop()
            audioRecord?.release()
          }
          runCatching {
            audioTrack?.stop()
            audioTrack?.release()
          }
        }
      }

    levelPollJob =
      scope.launch {
        while (isActive) {
          _status.update { it.copy(inputLevel = dspEngine.lastRmsLevel) }
          delay(60L)
        }
      }
  }

  fun release() {
    audioLoopJob?.cancel()
    levelPollJob?.cancel()
    audioLoopJob = null
    levelPollJob = null
    runCatching { context.unregisterReceiver(usbReceiver) }
    runCatching { audioManager.unregisterAudioDeviceCallback(audioDeviceCallback) }
  }
}
