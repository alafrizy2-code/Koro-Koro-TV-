package com.example

import com.example.audio.VocalDspEngine
import com.example.data.SampleKaraokeRepository
import com.example.data.remote.parseIso8601Duration
import com.example.model.KaraokeCategory
import com.example.model.MixerSettings
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KoroKoroTvTest {

  @Test
  fun `sample catalog contains all required karaoke categories`() {
    val songs = SampleKaraokeRepository.sampleSongs
    assertTrue(songs.any { it.isFeatured })
    assertTrue(songs.any { it.category == KaraokeCategory.INDONESIA })
    assertTrue(songs.any { it.category == KaraokeCategory.BARAT })
    assertTrue(songs.any { it.category == KaraokeCategory.DANGDUT })
    assertTrue(songs.any { it.isPopular })
    assertEquals(10, SampleKaraokeRepository.vocalPresets.size)
  }

  @Test
  fun `iso8601 duration parser formats YouTube durations accurately`() {
    assertEquals("04:28", parseIso8601Duration("PT4M28S"))
    assertEquals("01:02:05", parseIso8601Duration("PT1H02M05S"))
    assertEquals("00:45", parseIso8601Duration("PT45S"))
    assertEquals("04:00", parseIso8601Duration(null))
  }

  @Test
  fun `vocal dsp engine 10 presets produce distinct parameters and process audio`() {
    val engine = VocalDspEngine(48000)
    val presets = (0..9).map { presetNum ->
      engine.resolveEffectiveParams(MixerSettings(selectedPresetNumber = presetNum))
    }
    assertEquals(10, presets.distinct().size)

    // Generate 1kHz test sine wave
    val input = ShortArray(512) { i ->
      (sin(2.0 * Math.PI * 1000.0 * i / 48000.0) * 16000.0).toInt().toShort()
    }
    val outNormal = ShortArray(512)
    val outDeep = ShortArray(512)
    val outPitchUp = ShortArray(512)
    val outMuted = ShortArray(512)

    engine.processBuffer(input, outNormal, 512, MixerSettings(selectedPresetNumber = 0))
    engine.processBuffer(input, outDeep, 512, MixerSettings(selectedPresetNumber = 5))
    repeat(6) {
      engine.processBuffer(input, outPitchUp, 512, MixerSettings(selectedPresetNumber = 0, pitchShift = 4))
    }
    VocalDspEngine(48000).processBuffer(input, outMuted, 512, MixerSettings(isMicMuted = true))

    assertNotEquals(outNormal.toList(), outDeep.toList())
    assertNotEquals(outNormal.toList(), outPitchUp.toList())
    assertTrue(outMuted.all { it == 0.toShort() })
  }
}
