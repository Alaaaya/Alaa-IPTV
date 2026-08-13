package com.alaa.iptv.ui.player

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ActivityPlayerBinding
import java.util.Locale

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PlayerActivity"
        const val EXTRA_CHANNEL_INDEX = "CHANNEL_INDEX"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var streamUrl: String? = null
    private var channelName: String? = null
    private var streamType: String? = null
    private var channelIndex = -1

    private data class TrackOption(
        val type: Int,
        val title: String,
        val group: Tracks.Group? = null,
        val trackIndex: Int = -1
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        streamUrl = intent.getStringExtra("STREAM_URL")
        channelName = intent.getStringExtra("CHANNEL_NAME")
        streamType = intent.getStringExtra("STREAM_TYPE") ?: "live"
        channelIndex = intent.getIntExtra(EXTRA_CHANNEL_INDEX, -1)

        binding.channelNameText.text = channelName ?: ""
        binding.trackSelectionButton.setOnClickListener { showTrackSelection() }

        Log.d(TAG, "▶️ onCreate - Channel: $channelName")
        Log.d(TAG, "▶️ onCreate - Type: $streamType")

        if (!streamUrl.isNullOrBlank()) {
            initializePlayer(streamUrl!!)
        } else {
            Log.e(TAG, "❌ No stream URL provided")
            showError(getString(R.string.player_error))
        }
    }

    private fun initializePlayer(url: String) {
        showLoading(true)
        showError(null)
        player?.release()
        player = null

        Log.d(TAG, "▶️ Initializing player with URL type: ${streamType}")
        Log.d(TAG, "▶️ URL protocol: ${Uri.parse(url).scheme}")

        try {
            trackSelector = DefaultTrackSelector(this)
            player = ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector!!)
                .build()
            binding.playerView.player = player

            val uri = Uri.parse(url)
            val mediaItem = MediaItem.fromUri(uri)

            // Configure HTTP data source to allow cleartext and custom headers
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)

            val mediaSource = when {
                url.endsWith(".m3u8", ignoreCase = true) || 
                url.contains(".m3u8", ignoreCase = true) -> {
                    Log.d(TAG, "📺 Using HLS media source")
                    HlsMediaSource.Factory(httpDataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    Log.d(TAG, "📺 Using Progressive media source")
                    ProgressiveMediaSource.Factory(httpDataSourceFactory)
                        .createMediaSource(mediaItem)
                }
            }

            player?.apply {
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                Log.d(TAG, "⏳ STATE_BUFFERING")
                                showLoading(true)
                            }
                            Player.STATE_READY -> {
                                Log.d(TAG, "✅ STATE_READY - Playing")
                                showLoading(false)
                                hideChannelName()
                                updateTrackSelectionVisibility()
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "🏁 STATE_ENDED")
                                finish()
                            }
                            Player.STATE_IDLE -> {
                                Log.d(TAG, "⏸️ STATE_IDLE")
                                showLoading(false)
                            }
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(TAG, "❌ Player Error: ${error.errorCodeName}", error)
                        showLoading(false)

                        val errorMsg = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "فشل الاتصال بالشبكة"
                            PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE ->
                                "نوع المحتوى غير مدعوم"
                            PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                                "صيغة الملف غير مدعومة"
                            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ->
                                "فشل تشغيل الفيديو (Decoder)"
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                                "خطأ في الخادم (HTTP ${error.message})"
                            else -> "خطأ في التشغيل: ${error.message}"
                        }

                        showError(errorMsg)
                        Toast.makeText(this@PlayerActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "▶️ isPlaying changed: $isPlaying")
                    }
                })
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize player", e)
            showLoading(false)
            showError("فشل تهيئة المشغل: ${e.message}")
        }
    }

    private fun hideChannelName() {
        binding.channelNameText.postDelayed({
            binding.channelNameText.visibility = View.GONE
        }, 3000)
    }

    private fun showLoading(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        if (message == null) {
            binding.errorText.visibility = View.GONE
        } else {
            binding.errorText.text = message
            binding.errorText.visibility = View.VISIBLE
            Log.e(TAG, "Error displayed: $message")
        }
    }

    private fun updateTrackSelectionVisibility() {
        val hasTrackOptions = player?.currentTracks?.groups?.any { group ->
            !streamType.equals("live", ignoreCase = true) &&
                (group.type == C.TRACK_TYPE_AUDIO || group.type == C.TRACK_TYPE_TEXT) &&
                (0 until group.length).any { group.isTrackSupported(it) }
        } == true
        binding.trackSelectionButton.visibility = if (hasTrackOptions) View.VISIBLE else View.GONE
    }

    private fun showTrackSelection() {
        val activePlayer = player ?: return
        val selector = trackSelector ?: return
        val options = mutableListOf<TrackOption>()
        var hasSubtitleTrack = false

        activePlayer.currentTracks.groups.forEach { group ->
            if (group.type != C.TRACK_TYPE_AUDIO && group.type != C.TRACK_TYPE_TEXT) return@forEach
            (0 until group.length)
                .filter { group.isTrackSupported(it) }
                .forEach { index ->
                    if (group.type == C.TRACK_TYPE_TEXT) hasSubtitleTrack = true
                    val format = group.getTrackFormat(index)
                    val prefix = if (group.type == C.TRACK_TYPE_AUDIO) "الصوت" else "الترجمة"
                    val details = format.label?.takeIf { it.isNotBlank() } ?: displayLanguage(format.language)
                    options += TrackOption(group.type, "$prefix: $details", group, index)
                }
        }

        if (hasSubtitleTrack) options += TrackOption(C.TRACK_TYPE_TEXT, "الترجمة: إيقاف")
        if (options.isEmpty()) {
            Toast.makeText(this, "لا توجد مسارات صوت أو ترجمة إضافية", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("الصوت والترجمة")
            .setItems(options.map { it.title }.toTypedArray()) { dialog, selectedIndex ->
                val selected = options[selectedIndex]
                val parameters = selector.buildUponParameters()
                if (selected.type == C.TRACK_TYPE_TEXT && selected.group == null) {
                    parameters.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                } else {
                    parameters
                        .setTrackTypeDisabled(selected.type, false)
                        .clearOverridesOfType(selected.type)
                        .setOverrideForType(
                            TrackSelectionOverride(
                                requireNotNull(selected.group).mediaTrackGroup,
                                listOf(selected.trackIndex)
                            )
                        )
                }
                selector.parameters = parameters.build()
                Toast.makeText(this, "تم اختيار ${selected.title}", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun displayLanguage(code: String?): String {
        val normalized = code?.trim()?.lowercase().orEmpty()
        return when (normalized) {
            "ar", "ara" -> "العربية"
            "tr", "tur" -> "التركية"
            "de", "deu", "ger" -> "الألمانية"
            "en", "eng" -> "الإنجليزية"
            "fr", "fra", "fre" -> "الفرنسية"
            "es", "spa" -> "الإسبانية"
            else -> Locale.forLanguageTag(normalized).displayLanguage.takeIf { it.isNotBlank() } ?: "غير محدد"
        }
    }

    private fun switchChannel(offset: Int) {
        if (!streamType.equals("live", ignoreCase = true) || channelIndex < 0) {
            Toast.makeText(this, "تنقل القنوات متاح للبث المباشر فقط", Toast.LENGTH_SHORT).show()
            return
        }

        val nextIndex = channelIndex + offset
        val nextChannel = PlayerChannelNavigator.channelAt(nextIndex)
        if (nextChannel == null) {
            val boundaryMessage = if (offset > 0) "هذه آخر قناة" else "هذه أول قناة"
            Toast.makeText(this, boundaryMessage, Toast.LENGTH_SHORT).show()
            return
        }

        channelIndex = nextIndex
        streamUrl = nextChannel.streamUrl
        channelName = nextChannel.name
        streamType = nextChannel.streamType
        binding.channelNameText.text = nextChannel.name
        binding.channelNameText.visibility = View.VISIBLE
        Toast.makeText(this, "${nextChannel.name}  ${channelIndex + 1}/${PlayerChannelNavigator.size()}", Toast.LENGTH_SHORT).show()
        initializePlayer(nextChannel.streamUrl)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                finish()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_DPAD_CENTER -> {
                player?.let {
                    if (it.isPlaying) {
                        it.pause()
                    } else {
                        it.play()
                    }
                }
                true
            }
            KeyEvent.KEYCODE_MEDIA_PLAY -> {
                player?.play()
                true
            }
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                player?.pause()
                true
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                switchChannel(1)
                true
            }
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                switchChannel(-1)
                true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_CAPTIONS -> {
                showTrackSelection()
                true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                player?.seekTo((player?.currentPosition ?: 0) + 10000)
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_DPAD_LEFT -> {
                player?.seekTo(maxOf(0, (player?.currentPosition ?: 0) - 10000))
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onStop() {
        Log.d(TAG, "⏸️ onStop - Pausing player")
        player?.pause()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 onDestroy - Releasing player")
        player?.release()
        player = null
    }
}
