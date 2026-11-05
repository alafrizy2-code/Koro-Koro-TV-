package com.example.audio

import com.example.model.MixerSettings
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Real-time Vocal DSP Engine for Koro Koro TV.
 *
 * Implements:
 * - 10 distinct vocal presets (0..9)
 * - 2nd-order Biquad Low-Shelf (Bass) & High-Shelf (Treble) EQ
 * - Circular-buffer Echo with feedback
 * - Multi-tap Reverb network
 * - Dual-tap crossfaded Pitch Shifter (-6..+6 semitones)
 * - Acoustic feedback suppression (high-pass + adaptive howling attenuation)
 * - Soft-knee anti-clipping limiter
 */
class VocalDspEngine(private val sampleRate: Int = 48000) {

  // Echo delay buffer (up to 600ms)
  private val echoBufferSize = (sampleRate * 0.6f).toInt()
  private val echoBuffer = FloatArray(echoBufferSize)
  private var echoWriteIdx = 0

  // Multi-tap Reverb buffers
  private val revTap1 = FloatArray((sampleRate * 0.0297f).toInt())
  private val revTap2 = FloatArray((sampleRate * 0.0371f).toInt())
  private val revTap3 = FloatArray((sampleRate * 0.0411f).toInt())
  private val revTap4 = FloatArray((sampleRate * 0.0437f).toInt())
  private var revIdx1 = 0
  private var revIdx2 = 0
  private var revIdx3 = 0
  private var revIdx4 = 0

  // Pitch shifter ring buffer (60ms window)
  private val pitchBufferSize = (sampleRate * 0.060f).toInt()
  private val pitchBuffer = FloatArray(pitchBufferSize)
  private var pitchWriteIdx = 0
  private var pitchPhase = 0f

  // Biquad states for Bass (low-shelf 180Hz) & Treble (high-shelf 3500Hz)
  private var bassX1 = 0f
  private var bassX2 = 0f
  private var bassY1 = 0f
  private var bassY2 = 0f

  private var trebleX1 = 0f
  private var trebleX2 = 0f
  private var trebleY1 = 0f
  private var trebleY2 = 0f

  // High-pass filter state (~95Hz) to prevent low-frequency rumble & acoustic feedback
  private var hpX1 = 0f
  private var hpY1 = 0f
  private val hpAlpha = 0.987f

  // Adaptive feedback suppressor gain
  private var feedbackGain = 1.0f

  // Cached DSP parameters & biquad coefficients for zero-allocation audio loop
  private var cachedSettings: MixerSettings? = null
  private var effEchoMix = 0f
  private var effEchoFeedback = 0f
  private var effEchoDelaySamples = (sampleRate * 0.22f).toInt()
  private var effReverbMix = 0.05f
  private var effReverbDecay = 0.2f
  private var effPitchSemitones = 0
  private var effPitchRatio = 1.0f
  private var effDrive = 1.0f

  private var bassB0 = 1f
  private var bassB1 = 0f
  private var bassB2 = 0f
  private var bassA1 = 0f
  private var bassA2 = 0f

  private var trebleB0 = 1f
  private var trebleB1 = 0f
  private var trebleB2 = 0f
  private var trebleA1 = 0f
  private var trebleA2 = 0f

  @Volatile var lastRmsLevel: Float = 0f
    private set

  data class EffectiveDspParams(
    val echoMix: Float,
    val echoFeedback: Float,
    val echoDelaySamples: Int,
    val reverbMix: Float,
    val reverbDecay: Float,
    val bassDb: Float,
    val trebleDb: Float,
    val pitchSemitones: Int,
    val drive: Float,
  )

  fun resolveEffectiveParams(settings: MixerSettings): EffectiveDspParams {
    return when (settings.selectedPresetNumber) {
      0 -> // 0: Normal (Clean natural vocal)
        EffectiveDspParams(
          echoMix = 0f,
          echoFeedback = 0f,
          echoDelaySamples = (sampleRate * 0.22f).toInt(),
          reverbMix = 0.05f,
          reverbDecay = 0.2f,
          bassDb = 0f,
          trebleDb = 0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.0f,
        )
      1 -> // 1: Studio Reverb
        EffectiveDspParams(
          echoMix = 0.12f,
          echoFeedback = 0.22f,
          echoDelaySamples = (sampleRate * 0.18f).toInt(),
          reverbMix = 0.42f,
          reverbDecay = 0.58f,
          bassDb = 1.5f,
          trebleDb = 2.5f,
          pitchSemitones = settings.pitchShift,
          drive = 1.05f,
        )
      2 -> // 2: Echo
        EffectiveDspParams(
          echoMix = 0.48f,
          echoFeedback = 0.45f,
          echoDelaySamples = (sampleRate * 0.30f).toInt(),
          reverbMix = 0.18f,
          reverbDecay = 0.35f,
          bassDb = 0.5f,
          trebleDb = 1.0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.05f,
        )
      3 -> // 3: Concert Hall
        EffectiveDspParams(
          echoMix = 0.28f,
          echoFeedback = 0.35f,
          echoDelaySamples = (sampleRate * 0.36f).toInt(),
          reverbMix = 0.65f,
          reverbDecay = 0.76f,
          bassDb = 2.5f,
          trebleDb = 2.0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.1f,
        )
      4 -> // 4: Karaoke Pop
        EffectiveDspParams(
          echoMix = 0.30f,
          echoFeedback = 0.32f,
          echoDelaySamples = (sampleRate * 0.24f).toInt(),
          reverbMix = 0.35f,
          reverbDecay = 0.50f,
          bassDb = 2.0f,
          trebleDb = 4.0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.15f,
        )
      5 -> // 5: Deep Vocal
        EffectiveDspParams(
          echoMix = 0.18f,
          echoFeedback = 0.25f,
          echoDelaySamples = (sampleRate * 0.22f).toInt(),
          reverbMix = 0.28f,
          reverbDecay = 0.45f,
          bassDb = 6.5f,
          trebleDb = -1.5f,
          pitchSemitones = settings.pitchShift,
          drive = 1.15f,
        )
      6 -> // 6: Bright Vocal
        EffectiveDspParams(
          echoMix = 0.20f,
          echoFeedback = 0.25f,
          echoDelaySamples = (sampleRate * 0.20f).toInt(),
          reverbMix = 0.30f,
          reverbDecay = 0.45f,
          bassDb = -1.5f,
          trebleDb = 6.5f,
          pitchSemitones = settings.pitchShift,
          drive = 1.12f,
        )
      7 -> // 7: Warm Vocal
        EffectiveDspParams(
          echoMix = 0.22f,
          echoFeedback = 0.28f,
          echoDelaySamples = (sampleRate * 0.25f).toInt(),
          reverbMix = 0.40f,
          reverbDecay = 0.55f,
          bassDb = 4.5f,
          trebleDb = 1.0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.12f,
        )
      8 -> // 8: Powerful Vocal
        EffectiveDspParams(
          echoMix = 0.32f,
          echoFeedback = 0.35f,
          echoDelaySamples = (sampleRate * 0.26f).toInt(),
          reverbMix = 0.48f,
          reverbDecay = 0.62f,
          bassDb = 5.0f,
          trebleDb = 5.0f,
          pitchSemitones = settings.pitchShift,
          drive = 1.35f,
        )
      else -> // 9: Custom Effect (uses user's manual sliders)
        EffectiveDspParams(
          echoMix = (settings.echoLevel / 100f) * 0.65f,
          echoFeedback = (settings.echoLevel / 100f) * 0.52f,
          echoDelaySamples = (sampleRate * 0.26f).toInt(),
          reverbMix = (settings.reverbLevel / 100f) * 0.75f,
          reverbDecay = 0.25f + (settings.reverbLevel / 100f) * 0.55f,
          bassDb = ((settings.bassLevel - 50) / 50f) * 7.5f,
          trebleDb = ((settings.trebleLevel - 50) / 50f) * 7.5f,
          pitchSemitones = settings.pitchShift,
          drive = 1.1f,
        )
    }
  }

  private fun updateCachedParamsIfNeeded(settings: MixerSettings) {
    if (cachedSettings == settings) return
    cachedSettings = settings
    val params = resolveEffectiveParams(settings)
    effEchoMix = params.echoMix
    effEchoFeedback = params.echoFeedback
    effEchoDelaySamples = params.echoDelaySamples
    effReverbMix = params.reverbMix
    effReverbDecay = params.reverbDecay
    effPitchSemitones = params.pitchSemitones
    effPitchRatio = 2.0.pow(params.pitchSemitones / 12.0).toFloat()
    effDrive = params.drive

    val bass = computeLowShelf(180f, params.bassDb)
    bassB0 = bass.b0
    bassB1 = bass.b1
    bassB2 = bass.b2
    bassA1 = bass.a1
    bassA2 = bass.a2

    val treble = computeHighShelf(3500f, params.trebleDb)
    trebleB0 = treble.b0
    trebleB1 = treble.b1
    trebleB2 = treble.b2
    trebleA1 = treble.a1
    trebleA2 = treble.a2
  }

  fun processBuffer(input: ShortArray, output: ShortArray, count: Int, settings: MixerSettings) {
    if (count <= 0) return

    updateCachedParamsIfNeeded(settings)
    val micGain = if (settings.isMicMuted) 0f else (settings.micVolume / 100f) * 1.4f
    var sumSquares = 0.0

    for (i in 0 until count) {
      var sample = input[i] / 32768f

      // 1. Acoustic Feedback Protection: High-pass filter (~95Hz)
      val hpOut = hpAlpha * (hpY1 + sample - hpX1)
      hpX1 = sample
      hpY1 = hpOut
      sample = hpOut

      // 2. Apply Mic Gain & Preset Drive
      sample *= micGain * effDrive

      // 3. Pitch Shift (if non-zero semitones)
      if (effPitchSemitones != 0) {
        sample = processPitchShift(sample, effPitchRatio)
      }

      // 4. Bass Low-Shelf EQ
      val bOut =
        bassB0 * sample +
          bassB1 * bassX1 +
          bassB2 * bassX2 -
          bassA1 * bassY1 -
          bassA2 * bassY2
      bassX2 = bassX1
      bassX1 = sample
      bassY2 = bassY1
      bassY1 = bOut
      sample = bOut

      // 5. Treble High-Shelf EQ
      val tOut =
        trebleB0 * sample +
          trebleB1 * trebleX1 +
          trebleB2 * trebleX2 -
          trebleA1 * trebleY1 -
          trebleA2 * trebleY2
      trebleX2 = trebleX1
      trebleX1 = sample
      trebleY2 = trebleY1
      trebleY1 = tOut
      sample = tOut

      // 6. Echo Delay Line
      if (effEchoMix > 0.001f) {
        val readIdx =
          (echoWriteIdx - effEchoDelaySamples + echoBufferSize) % echoBufferSize
        val delayed = echoBuffer[readIdx]
        echoBuffer[echoWriteIdx] = (sample + delayed * effEchoFeedback).coerceIn(-1.5f, 1.5f)
        echoWriteIdx = (echoWriteIdx + 1) % echoBufferSize
        sample += delayed * effEchoMix
      }

      // 7. Multi-Tap Reverb Network
      if (effReverbMix > 0.001f) {
        val r1 = revTap1[revIdx1]
        val r2 = revTap2[revIdx2]
        val r3 = revTap3[revIdx3]
        val r4 = revTap4[revIdx4]

        revTap1[revIdx1] = sample + r1 * effReverbDecay
        revTap2[revIdx2] = sample + r2 * effReverbDecay
        revTap3[revIdx3] = sample + r3 * effReverbDecay
        revTap4[revIdx4] = sample + r4 * effReverbDecay

        revIdx1 = (revIdx1 + 1) % revTap1.size
        revIdx2 = (revIdx2 + 1) % revTap2.size
        revIdx3 = (revIdx3 + 1) % revTap3.size
        revIdx4 = (revIdx4 + 1) % revTap4.size

        val revSum = (r1 + r2 + r3 + r4) * 0.25f
        sample += revSum * effReverbMix
      }

      // 8. Adaptive Acoustic Feedback Howling Guard
      val absVal = abs(sample)
      if (absVal > 0.92f) {
        feedbackGain = (feedbackGain * 0.995f).coerceAtLeast(0.35f)
      } else {
        feedbackGain = (feedbackGain + 0.0002f).coerceAtMost(1.0f)
      }
      sample *= feedbackGain

      // 9. Soft-Knee Anti-Clipping Limiter
      val limited = tanh(sample.toDouble()).toFloat()

      sumSquares += (limited * limited)
      output[i] = (limited * 32767f).toInt().coerceIn(-32768, 32767).toShort()
    }

    lastRmsLevel = sqrt(sumSquares / count).toFloat().coerceIn(0f, 1f)
  }

  private fun processPitchShift(inputSample: Float, ratio: Float): Float {
    pitchBuffer[pitchWriteIdx] = inputSample
    val step = (1.0f - ratio) / pitchBufferSize
    pitchPhase = (pitchPhase + step + 1.0f) % 1.0f

    val offset1 = pitchPhase * pitchBufferSize
    val offset2 = ((pitchPhase + 0.5f) % 1.0f) * pitchBufferSize

    val s1 = readInterpolated(pitchBuffer, pitchWriteIdx - offset1)
    val s2 = readInterpolated(pitchBuffer, pitchWriteIdx - offset2)

    val win1 = 0.5f - 0.5f * cos(2.0 * PI * pitchPhase).toFloat()
    val win2 = 1.0f - win1

    pitchWriteIdx = (pitchWriteIdx + 1) % pitchBufferSize
    return s1 * win1 + s2 * win2
  }

  private fun readInterpolated(buf: FloatArray, exactIndex: Float): Float {
    val size = buf.size
    val wrapped = ((exactIndex % size) + size) % size
    val i0 = wrapped.toInt()
    val i1 = (i0 + 1) % size
    val frac = wrapped - i0
    return buf[i0] * (1f - frac) + buf[i1] * frac
  }

  private data class BiquadCoeffs(
    val b0: Float,
    val b1: Float,
    val b2: Float,
    val a1: Float,
    val a2: Float,
  )

  private fun computeLowShelf(freqHz: Float, gainDb: Float): BiquadCoeffs {
    if (abs(gainDb) < 0.05f) return BiquadCoeffs(1f, 0f, 0f, 0f, 0f)
    val a = 10.0.pow(gainDb / 40.0)
    val w0 = 2.0 * PI * freqHz / sampleRate
    val cosW0 = cos(w0)
    val sinW0 = sin(w0)
    val alpha = sinW0 / 2.0 * sqrt(2.0)
    val twoSqrtAAlpha = 2.0 * sqrt(a) * alpha

    val b0 = a * ((a + 1) - (a - 1) * cosW0 + twoSqrtAAlpha)
    val b1 = 2 * a * ((a - 1) - (a + 1) * cosW0)
    val b2 = a * ((a + 1) - (a - 1) * cosW0 - twoSqrtAAlpha)
    val a0 = (a + 1) + (a - 1) * cosW0 + twoSqrtAAlpha
    val a1 = -2 * ((a - 1) + (a + 1) * cosW0)
    val a2 = (a + 1) + (a - 1) * cosW0 - twoSqrtAAlpha

    return BiquadCoeffs(
      (b0 / a0).toFloat(),
      (b1 / a0).toFloat(),
      (b2 / a0).toFloat(),
      (a1 / a0).toFloat(),
      (a2 / a0).toFloat(),
    )
  }

  private fun computeHighShelf(freqHz: Float, gainDb: Float): BiquadCoeffs {
    if (abs(gainDb) < 0.05f) return BiquadCoeffs(1f, 0f, 0f, 0f, 0f)
    val a = 10.0.pow(gainDb / 40.0)
    val w0 = 2.0 * PI * freqHz / sampleRate
    val cosW0 = cos(w0)
    val sinW0 = sin(w0)
    val alpha = sinW0 / 2.0 * sqrt(2.0)
    val twoSqrtAAlpha = 2.0 * sqrt(a) * alpha

    val b0 = a * ((a + 1) + (a - 1) * cosW0 + twoSqrtAAlpha)
    val b1 = -2 * a * ((a - 1) + (a + 1) * cosW0)
    val b2 = a * ((a + 1) + (a - 1) * cosW0 - twoSqrtAAlpha)
    val a0 = (a + 1) - (a - 1) * cosW0 + twoSqrtAAlpha
    val a1 = 2 * ((a - 1) - (a + 1) * cosW0)
    val a2 = (a + 1) - (a - 1) * cosW0 - twoSqrtAAlpha

    return BiquadCoeffs(
      (b0 / a0).toFloat(),
      (b1 / a0).toFloat(),
      (b2 / a0).toFloat(),
      (a1 / a0).toFloat(),
      (a2 / a0).toFloat(),
    )
  }
}
