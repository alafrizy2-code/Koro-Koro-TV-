package com.example.ui.player

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class YouTubePlayerController {
  internal var webView: WebView? = null

  fun play() {
    webView?.post { webView?.evaluateJavascript("if(window.koroPlay) window.koroPlay();", null) }
  }

  fun pause() {
    webView?.post { webView?.evaluateJavascript("if(window.koroPause) window.koroPause();", null) }
  }

  fun loadVideo(videoId: String) {
    webView?.post {
      webView?.evaluateJavascript("if(window.koroLoad) window.koroLoad('$videoId');", null)
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeKaraokePlayer(
  videoId: String,
  controller: YouTubePlayerController,
  onPlayingChanged: (Boolean) -> Unit,
  onProgressChanged: (currentSec: Float, durationSec: Float) -> Unit,
  onVideoEnded: () -> Unit,
  onPlayerError: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val mainHandler = remember { Handler(Looper.getMainLooper()) }

  val bridge =
    remember(onPlayingChanged, onProgressChanged, onVideoEnded, onPlayerError) {
      object {
        @JavascriptInterface
        fun onStateChange(state: Int) {
          mainHandler.post {
            when (state) {
              1 -> onPlayingChanged(true) // YT.PlayerState.PLAYING
              2 -> onPlayingChanged(false) // YT.PlayerState.PAUSED
              0 -> onVideoEnded() // YT.PlayerState.ENDED
            }
          }
        }

        @JavascriptInterface
        fun onTimeUpdate(current: Float, duration: Float) {
          mainHandler.post { onProgressChanged(current, duration) }
        }

        @JavascriptInterface
        fun onError(code: Int) {
          mainHandler.post { onPlayerError(code) }
        }
      }
    }

  val htmlContent =
    remember(videoId) {
      """
      <!DOCTYPE html>
      <html>
      <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <style>
          * { margin: 0; padding: 0; box-sizing: border-box; }
          html, body { width: 100%; height: 100%; background-color: #000000; overflow: hidden; }
          #player { width: 100%; height: 100%; }
        </style>
      </head>
      <body>
        <div id="player"></div>
        <script src="https://www.youtube.com/iframe_api"></script>
        <script>
          var player;
          var timer;
          function onYouTubeIframeAPIReady() {
            player = new YT.Player('player', {
              width: '100%',
              height: '100%',
              videoId: '$videoId',
              playerVars: {
                'autoplay': 1,
                'controls': 0,
                'rel': 0,
                'modestbranding': 1,
                'playsinline': 1,
                'fs': 1,
                'iv_load_policy': 3
              },
              events: {
                'onReady': function(e) {
                  e.target.playVideo();
                  if (timer) clearInterval(timer);
                  timer = setInterval(function() {
                    if (player && player.getCurrentTime && player.getDuration) {
                      KoroBridge.onTimeUpdate(player.getCurrentTime() || 0, player.getDuration() || 0);
                    }
                  }, 1000);
                },
                'onStateChange': function(e) {
                  KoroBridge.onStateChange(e.data);
                },
                'onError': function(e) {
                  KoroBridge.onError(e.data);
                }
              }
            });
          }
          window.koroPlay = function() { if (player && player.playVideo) player.playVideo(); };
          window.koroPause = function() { if (player && player.pauseVideo) player.pauseVideo(); };
          window.koroLoad = function(id) { if (player && player.loadVideoById) player.loadVideoById(id); };
        </script>
      </body>
      </html>
      """.trimIndent()
    }

  LaunchedEffect(videoId) { controller.loadVideo(videoId) }

  DisposableEffect(Unit) {
    onDispose {
      controller.webView?.apply {
        stopLoading()
        destroy()
      }
      controller.webView = null
    }
  }

  AndroidView(
    modifier = modifier,
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams =
          ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
          )
        setBackgroundColor(Color.BLACK)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        webChromeClient = WebChromeClient()
        webViewClient = WebViewClient()
        addJavascriptInterface(bridge, "KoroBridge")
        loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
        controller.webView = this
      }
    },
  )
}
