package com.example.data

import com.example.R
import com.example.model.KaraokeCategory
import com.example.model.KaraokeSong
import com.example.model.VocalPreset

object SampleKaraokeRepository {

  val vocalPresets: List<VocalPreset> =
    listOf(
      VocalPreset(0, "Normal", "Vokal Alami"),
      VocalPreset(1, "Studio Reverb", "Ruang Rekaman"),
      VocalPreset(2, "Echo", "Gema Klasik"),
      VocalPreset(3, "Concert Hall", "Panggung Megah"),
      VocalPreset(4, "Karaoke Pop", "Cerah & Seimbang"),
      VocalPreset(5, "Deep Vocal", "Bass Hangat"),
      VocalPreset(6, "Bright Vocal", "Treble Jernih"),
      VocalPreset(7, "Warm Vocal", "Lembut & Intim"),
      VocalPreset(8, "Powerful Vocal", "Dorongan Penuh"),
      VocalPreset(9, "Custom Effect", "Setelan Pribadi"),
    )

  val sampleSongs: List<KaraokeSong> =
    listOf(
      // Featured & Karaoke Indonesia
      KaraokeSong(
        id = "indo_1",
        title = "Separuh Aku (Karaoke)",
        artist = "NOAH",
        category = KaraokeCategory.INDONESIA,
        duration = "04:28",
        thumbnailRes = R.drawable.img_hero_karaoke_1793869687632,
        youtubeVideoId = "dQw4w9WgXcQ",
        isFeatured = true,
        isPopular = true,
        sampleLyrics =
          listOf(
            "Dan terjadi lagi...",
            "Kisah lama yang terulang kembali",
            "Kau terluka lagi dari cinta rumit yang kau jalani",
            "Dengar laraku, suara hati ini memanggil namamu",
          ),
      ),
      KaraokeSong(
        id = "indo_2",
        title = "Dan... (Karaoke)",
        artist = "Sheila on 7",
        category = KaraokeCategory.INDONESIA,
        duration = "04:46",
        thumbnailRes = R.drawable.img_thumb_indo_1793869688245,
        youtubeVideoId = "kJQP7kiw5Fk",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Dan... bila esok datang kembali",
            "Seperti sedia kala dimana kau bisa bercanda",
            "Dan... perlahan kau pun lupakan aku",
            "Mimpi burukmu dimana t'lah kutancapkan duri tajam",
          ),
      ),
      KaraokeSong(
        id = "indo_3",
        title = "Hati-Hati di Jalan (Karaoke)",
        artist = "Tulus",
        category = KaraokeCategory.INDONESIA,
        duration = "04:02",
        thumbnailRes = R.drawable.img_thumb_indo_1793869688245,
        youtubeVideoId = "9bZkp7q19f0",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Perjalanan membawamu bertemu denganku",
            "Ku bertemu kamu...",
            "Sepertimu yang kucari, konon aku juga seperti yang kaucari",
          ),
      ),
      KaraokeSong(
        id = "indo_4",
        title = "Kangen (Karaoke)",
        artist = "Dewa 19",
        category = KaraokeCategory.INDONESIA,
        duration = "05:32",
        thumbnailRes = R.drawable.img_thumb_indo_1793869688245,
        youtubeVideoId = "JGwWNGJdvx8",
        sampleLyrics =
          listOf(
            "Kutrima suratmu t'lah kubaca dan aku mengerti",
            "Betapa merindunya dirimu akan hadirnya diriku",
            "Semua kata rindumu semakin membuatku tak berdaya",
          ),
      ),
      KaraokeSong(
        id = "indo_5",
        title = "Kali Kedua (Karaoke)",
        artist = "Raisa",
        category = KaraokeCategory.INDONESIA,
        duration = "04:15",
        thumbnailRes = R.drawable.img_thumb_indo_1793869688245,
        youtubeVideoId = "RgKAFK5djSk",
        sampleLyrics =
          listOf(
            "Jika wangimu saja bisa memindahkan duniaku",
            "Maka cintamu pasti bisa mengubah jalan hidupku",
          ),
      ),
      KaraokeSong(
        id = "indo_6",
        title = "Asal Kau Bahagia (Karaoke)",
        artist = "Armada",
        category = KaraokeCategory.INDONESIA,
        duration = "04:30",
        thumbnailRes = R.drawable.img_thumb_indo_1793869688245,
        youtubeVideoId = "OPf0YbXqDm0",
        sampleLyrics =
          listOf(
            "Yang, kemarin ku melihatmu",
            "Kau bertemu dengannya",
            "Ku rasa sekarang kau masih memikirkan tentang dia",
          ),
      ),

      // Karaoke Barat
      KaraokeSong(
        id = "barat_1",
        title = "Bohemian Rhapsody (Karaoke)",
        artist = "Queen",
        category = KaraokeCategory.BARAT,
        duration = "05:55",
        thumbnailRes = R.drawable.img_thumb_barat_1793869689043,
        youtubeVideoId = "fJ9rUzIMcZQ",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Is this the real life? Is this just fantasy?",
            "Caught in a landslide, no escape from reality",
            "Open your eyes, look up to the skies and see...",
          ),
      ),
      KaraokeSong(
        id = "barat_2",
        title = "Just The Way You Are (Karaoke)",
        artist = "Bruno Mars",
        category = KaraokeCategory.BARAT,
        duration = "03:40",
        thumbnailRes = R.drawable.img_thumb_barat_1793869689043,
        youtubeVideoId = "LjhCEhWiKXk",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Oh, her eyes, her eyes make the stars look like they're not shinin'",
            "Her hair, her hair falls perfectly without her tryin'",
            "When I see your face, there's not a thing that I would change",
          ),
      ),
      KaraokeSong(
        id = "barat_3",
        title = "Someone Like You (Karaoke)",
        artist = "Adele",
        category = KaraokeCategory.BARAT,
        duration = "04:45",
        thumbnailRes = R.drawable.img_thumb_barat_1793869689043,
        youtubeVideoId = "hLQl3WQQoQ0",
        sampleLyrics =
          listOf(
            "I heard that you're settled down",
            "That you found a girl and you're married now",
            "Never mind, I'll find someone like you",
          ),
      ),
      KaraokeSong(
        id = "barat_4",
        title = "Fix You (Karaoke)",
        artist = "Coldplay",
        category = KaraokeCategory.BARAT,
        duration = "04:55",
        thumbnailRes = R.drawable.img_thumb_barat_1793869689043,
        youtubeVideoId = "k4V3Mo61fJM",
        sampleLyrics =
          listOf(
            "When you try your best, but you don't succeed",
            "When you get what you want, but not what you need",
            "Lights will guide you home, and ignite your bones",
          ),
      ),
      KaraokeSong(
        id = "barat_5",
        title = "Perfect (Karaoke)",
        artist = "Ed Sheeran",
        category = KaraokeCategory.BARAT,
        duration = "04:23",
        thumbnailRes = R.drawable.img_thumb_barat_1793869689043,
        youtubeVideoId = "2Vv-BfVoq4g",
        sampleLyrics =
          listOf(
            "I found a love for me",
            "Darling, just dive right in and follow my lead",
            "Baby, I'm dancing in the dark with you between my arms",
          ),
      ),

      // Karaoke Dangdut
      KaraokeSong(
        id = "dangdut_1",
        title = "Ojo Dibandingke (Karaoke)",
        artist = "Denny Caknan",
        category = KaraokeCategory.DANGDUT,
        duration = "04:12",
        thumbnailRes = R.drawable.img_thumb_dangdut_1793869690367,
        youtubeVideoId = "m1234567890",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Wong ko ngene kok dibanding-bandingke",
            "Saing-saingke yo mesti kalah",
            "Tak oyak'o aku yo ora mampu",
            "Mung sak kuatku mencintaimu",
          ),
      ),
      KaraokeSong(
        id = "dangdut_2",
        title = "Sayang (Karaoke)",
        artist = "Via Vallen",
        category = KaraokeCategory.DANGDUT,
        duration = "04:38",
        thumbnailRes = R.drawable.img_thumb_dangdut_1793869690367,
        youtubeVideoId = "n1234567890",
        isPopular = true,
        sampleLyrics =
          listOf(
            "Sayang opo kowe krungu jerite atiku",
            "Mengharap engkau kembali",
            "Sayang nganti memutih rambutku",
          ),
      ),
      KaraokeSong(
        id = "dangdut_3",
        title = "Begadang (Karaoke)",
        artist = "Rhoma Irama",
        category = KaraokeCategory.DANGDUT,
        duration = "03:50",
        thumbnailRes = R.drawable.img_thumb_dangdut_1793869690367,
        youtubeVideoId = "o1234567890",
        sampleLyrics =
          listOf(
            "Begadang jangan begadang",
            "Kalau tiada artinya",
            "Begadang boleh saja, kalau ada perlunya",
          ),
      ),
      KaraokeSong(
        id = "dangdut_4",
        title = "Seperti Mati Lampu (Karaoke)",
        artist = "Nassar",
        category = KaraokeCategory.DANGDUT,
        duration = "04:20",
        thumbnailRes = R.drawable.img_thumb_dangdut_1793869690367,
        youtubeVideoId = "p1234567890",
        sampleLyrics =
          listOf(
            "Seperti mati lampu ya sayang",
            "Seperti mati lampu...",
            "Cintaku tanpamu ya sayang, bagai malam tiada berbintang",
          ),
      ),
      KaraokeSong(
        id = "dangdut_5",
        title = "Goyang Inul (Karaoke)",
        artist = "Inul Daratista",
        category = KaraokeCategory.DANGDUT,
        duration = "04:05",
        thumbnailRes = R.drawable.img_thumb_dangdut_1793869690367,
        youtubeVideoId = "q1234567890",
        sampleLyrics =
          listOf(
            "Goyang Inul... goyang ngebor...",
            "Mari kita bergoyang bersama",
            "Hilangkan semua rasa duka di hati",
          ),
      ),
    )

  val initialFavorites: Set<String> = setOf("indo_1", "barat_1", "dangdut_1", "indo_2")

  val initialHistory: List<String> = listOf("indo_2", "barat_2", "dangdut_2", "indo_3")

  val initialQueue: List<String> = listOf("barat_1", "dangdut_1", "indo_4", "barat_4")
}
