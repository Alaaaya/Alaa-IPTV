package com.alaa.iptv.ui.player

import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.annotation.SuppressLint
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.alaa.iptv.R
import com.alaa.iptv.data.models.LiveUrlFallbackPolicy
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.preferences.MediaLibraryEntry
import com.alaa.iptv.databinding.ActivityPlayerBinding
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import com.alaa.iptv.ui.theme.DisplayTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("UnsafeOptInUsageError")
class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PlayerActivity"
        const val EXTRA_CHANNEL_INDEX = "CHANNEL_INDEX"
        const val EXTRA_RESUME_POSITION_MS = "RESUME_POSITION_MS"
        const val EXTRA_EPISODE_INDEX = "EPISODE_INDEX"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var streamUrl: String? = null
    private var channelName: String? = null
    private var streamType: String? = null
    private var channelIndex = -1
    private var episodeIndex = -1
    private val attemptedLiveUrls = linkedSetOf<String>()
    private var playerOpenedAtMs = 0L
    private var resumePositionMs = 0L
    private lateinit var prefs: AppPreferences
    private var sleepTimerJob: Job? = null
    private var inactivityReminderJob: Job? = null

    private data class TrackOption(
        val type: Int,
        val title: String,
        val group: Tracks.Group? = null,
        val trackIndex: Int = -1
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerOpenedAtMs = SystemClock.elapsedRealtime()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = AppPreferences(this)
        DisplayTheme.applyPlayer(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) window.attributes = window.attributes.apply { screenBrightness = 0.82f }
        binding.playerView.post { DisplayTheme.applyPlayerControls(binding.playerView, prefs) }

        streamUrl = intent.getStringExtra("STREAM_URL")
        channelName = intent.getStringExtra("CHANNEL_NAME")
        streamType = intent.getStringExtra("STREAM_TYPE") ?: "live"
        channelIndex = intent.getIntExtra(EXTRA_CHANNEL_INDEX, -1)
        episodeIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, -1)
        resumePositionMs = intent.getLongExtra(EXTRA_RESUME_POSITION_MS, 0L)

        binding.channelNameText.text = channelName ?: ""
        binding.trackSelectionButton.setOnClickListener { showTrackSelection() }
        binding.trackSelectionButton.setOnLongClickListener {
            if (prefs.isFeatureEnabled(FeatureCatalog.SLEEP_TIMER)) showSleepTimer()
            else Toast.makeText(this, "فعّل مؤقت النوم من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
            true
        }
        scheduleInactivityReminder()
        binding.errorText.setOnClickListener {
            streamUrl?.takeIf { it.isNotBlank() }?.let {
                attemptedLiveUrls.clear()
                initializePlayer(it)
            }
        }

        Log.d(TAG, "▶️ onCreate - Channel: $channelName")
        Log.d(TAG, "▶️ onCreate - Type: $streamType")

        lifecycleScope.launch {
            if (!ControlPlaneActivityGuard.refreshAndEnforce(this@PlayerActivity, prefs)) return@launch
            streamUrl?.takeIf { it.isNotBlank() }?.let(::initializePlayer) ?: run {
                Log.e(TAG, "❌ No stream URL provided")
                showError(getString(R.string.player_error))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scheduleInactivityReminder()
        lifecycleScope.launch {
            ControlPlaneActivityGuard.refreshAndEnforce(this@PlayerActivity, prefs)
        }
    }

    private fun initializePlayer(url: String) {
        showLoading(true)
        showError(null)
        attemptedLiveUrls += url

        Log.d(TAG, "▶️ Initializing player with URL type: ${streamType}")
        Log.d(TAG, "▶️ URL protocol: ${Uri.parse(url).scheme}")

        try {
            val shouldAttachListener = player == null
            if (shouldAttachListener) {
                val selector = DefaultTrackSelector(this)
                trackSelector = selector
                val lowLatencyMode = prefs.isFeatureEnabled(FeatureCatalog.LOW_LATENCY_MODE)
                val fastLiveLoadControl = DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        when {
                            prefs.isFeatureEnabled(FeatureCatalog.COMPATIBILITY_MODE) -> 2_500
                            lowLatencyMode -> 1_000
                            else -> 1_500
                        },
                        when {
                            prefs.isFeatureEnabled(FeatureCatalog.COMPATIBILITY_MODE) -> 12_000
                            prefs.isFeatureEnabled(FeatureCatalog.HIGH_PERFORMANCE_MODE) -> 16_000
                            lowLatencyMode -> 3_000
                            else -> 6_000
                        },
                        if (lowLatencyMode) 250 else 300,
                        if (lowLatencyMode) 600 else 800
                    )
                    .build()
                player = ExoPlayer.Builder(this)
                    .setTrackSelector(selector)
                    .setLoadControl(fastLiveLoadControl)
                    .build()
                binding.playerView.player = player
            }

            val uri = Uri.parse(url)
            val mediaItem = MediaItem.fromUri(uri)
            if (streamType.equals("live", ignoreCase = true) && prefs.isFeatureEnabled(FeatureCatalog.LIVE_AUDIO_ONLY)) {
                trackSelector?.let { currentSelector ->
                    currentSelector.parameters = currentSelector.buildUponParameters()
                        .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true)
                        .build()
                }
                binding.playerView.visibility = View.INVISIBLE
                binding.channelNameText.visibility = View.VISIBLE
            }

            // Configure HTTP data source to allow cleartext and custom headers
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent("Mozilla/5.0 (Linux; Android 11; Android TV) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36")
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(30_000)

            val mediaSource = when {
                url.endsWith(".m3u8", ignoreCase = true) || 
                url.contains(".m3u8", ignoreCase = true) -> {
                    Log.d(TAG, "📺 Using HLS media source")
                    HlsMediaSource.Factory(httpDataSourceFactory)
                        .createMediaSource(mediaItem)
                }
                else -> {
                    Log.d(TAG, "📺 Using Progressive media source")
                    val progressiveItem = if (url.substringBefore('?').endsWith(".ts", ignoreCase = true)) {
                        mediaItem.buildUpon().setMimeType(MimeTypes.VIDEO_MP2T).build()
                    } else {
                        mediaItem
                    }
                    ProgressiveMediaSource.Factory(httpDataSourceFactory)
                        .createMediaSource(progressiveItem)
                }
            }

            player?.apply {
                // سجّل المستمع قبل prepare حتى لا تضيع أخطاء التهيئة الأولى.
                if (shouldAttachListener) addListener(playerListener)
                setMediaSource(mediaSource)
                prepare()
                if (!streamType.equals("live", ignoreCase = true) &&
                    prefs.isFeatureEnabled(FeatureCatalog.RESUME_PLAYBACK) && resumePositionMs > 0L
                ) {
                    seekTo(resumePositionMs)
                    resumePositionMs = 0L
                }
                play()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize player", e)
            showLoading(false)
            showError("فشل تهيئة المشغل: ${e.message}")
        }
    }

    private val playerListener = object : Player.Listener {
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
                    showLoading(false)
                    if (streamType.equals("series", ignoreCase = true) &&
                        prefs.isFeatureEnabled(FeatureCatalog.AUTO_NEXT_EPISODE) &&
                        promptNextEpisode()
                    ) return
                    val message = if (streamType.equals("live", ignoreCase = true)) {
                        "انتهى البث مؤقتاً، جرّب قناة أخرى"
                    } else {
                        "انتهى التشغيل أو تعذر متابعة الفيديو"
                    }
                    showError(message)
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
            if (prefs.isFeatureEnabled(FeatureCatalog.AUTO_RECONNECT) && tryAlternativeLiveUrl()) return
            val errorMsg = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "فشل الاتصال بالشبكة"
                PlaybackException.ERROR_CODE_IO_INVALID_HTTP_CONTENT_TYPE -> "نوع المحتوى غير مدعوم"
                PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED -> "صيغة الملف غير مدعومة"
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "فشل تشغيل الفيديو (Decoder)"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "خطأ في الخادم (HTTP ${error.message})"
                else -> "خطأ في التشغيل: ${error.message}"
            }
            // لا تُغلق الشاشة عند خطأ البث؛ النص قابل للضغط لإعادة المحاولة.
            showError("$errorMsg\nاضغط هنا لإعادة المحاولة")
            Toast.makeText(this@PlayerActivity, errorMsg, Toast.LENGTH_LONG).show()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "▶️ isPlaying changed: $isPlaying")
        }
    }

    private fun tryAlternativeLiveUrl(): Boolean {
        if (!streamType.equals("live", ignoreCase = true)) return false
        val currentUrl = streamUrl ?: return false
        val alternativeUrl = LiveUrlFallbackPolicy.nextAlternative(currentUrl, attemptedLiveUrls) ?: return false

        streamUrl = alternativeUrl
        Log.w(TAG, "↻ Retrying live stream with alternate container")
        initializePlayer(alternativeUrl)
        return true
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
            val isMediaLanguageTrack = !streamType.equals("live", ignoreCase = true) && when (group.type) {
                C.TRACK_TYPE_AUDIO -> prefs.isFeatureEnabled(FeatureCatalog.PLAYER_AUDIO_TRACKS)
                C.TRACK_TYPE_TEXT -> prefs.isFeatureEnabled(FeatureCatalog.PLAYER_SUBTITLES)
                else -> false
            }
            val isVideoQualityTrack = prefs.isFeatureEnabled(FeatureCatalog.QUALITY_SELECTION) &&
                group.type == C.TRACK_TYPE_VIDEO
            (isMediaLanguageTrack || isVideoQualityTrack) &&
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
            val isAllowed = (group.type == C.TRACK_TYPE_AUDIO && prefs.isFeatureEnabled(FeatureCatalog.PLAYER_AUDIO_TRACKS)) ||
                (group.type == C.TRACK_TYPE_TEXT && prefs.isFeatureEnabled(FeatureCatalog.PLAYER_SUBTITLES)) ||
                (group.type == C.TRACK_TYPE_VIDEO && prefs.isFeatureEnabled(FeatureCatalog.QUALITY_SELECTION))
            if (!isAllowed) return@forEach
            (0 until group.length)
                .filter { group.isTrackSupported(it) }
                .forEach { index ->
                    if (group.type == C.TRACK_TYPE_TEXT) hasSubtitleTrack = true
                    val format = group.getTrackFormat(index)
                    val prefix = when (group.type) {
                        C.TRACK_TYPE_AUDIO -> "الصوت"
                        C.TRACK_TYPE_TEXT -> "الترجمة"
                        else -> "الجودة"
                    }
                    val details = format.label?.takeIf { it.isNotBlank() }
                        ?: if (group.type == C.TRACK_TYPE_VIDEO && format.height > 0) "${format.height}p"
                        else displayLanguage(format.language)
                    options += TrackOption(group.type, "$prefix: $details", group, index)
                }
        }

        if (hasSubtitleTrack && prefs.isFeatureEnabled(FeatureCatalog.PLAYER_SUBTITLES)) options += TrackOption(C.TRACK_TYPE_TEXT, "الترجمة: إيقاف")
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
        attemptedLiveUrls.clear()
        channelName = nextChannel.name
        streamType = nextChannel.streamType
        binding.channelNameText.text = nextChannel.name
        binding.channelNameText.visibility = View.VISIBLE
        Toast.makeText(this, "${nextChannel.name}  ${channelIndex + 1}/${PlayerChannelNavigator.size()}", Toast.LENGTH_SHORT).show()
        initializePlayer(nextChannel.streamUrl)
    }

    private fun promptNextEpisode(): Boolean {
        val next = PlayerEpisodeNavigator.episodeAt(episodeIndex + 1) ?: return false
        AlertDialog.Builder(this)
            .setTitle("الحلقة التالية")
            .setMessage("تشغيل ${next.name} الآن؟")
            .setPositiveButton("تشغيل") { _, _ -> switchEpisode(next) }
            .setNegativeButton("إلغاء", null)
            .show()
        return true
    }

    private fun switchEpisode(next: PlayableEpisode) {
        episodeIndex += 1
        streamUrl = next.streamUrl
        channelName = next.name
        attemptedLiveUrls.clear()
        binding.channelNameText.text = next.name
        binding.channelNameText.visibility = View.VISIBLE
        initializePlayer(next.streamUrl)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((binding.trackSelectionButton.hasFocus() || binding.errorText.hasFocus()) &&
            keyCode in setOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER)
        ) {
            return super.onKeyDown(keyCode, event)
        }
        return when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                if (SystemClock.elapsedRealtime() - playerOpenedAtMs < 1_500L) return true
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
                if (prefs.isFeatureEnabled(FeatureCatalog.SKIP_CONTROLS)) {
                    player?.seekTo((player?.currentPosition ?: 0) + 10000)
                }
                true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (prefs.isFeatureEnabled(FeatureCatalog.SKIP_CONTROLS)) {
                    player?.seekTo(maxOf(0, (player?.currentPosition ?: 0) - 10000))
                }
                true
            }
            KeyEvent.KEYCODE_PROG_RED -> {
                if (prefs.isFeatureEnabled(FeatureCatalog.REMOTE_SHORTCUTS)) showTrackSelection()
                true
            }
            KeyEvent.KEYCODE_PROG_GREEN -> {
                if (prefs.isFeatureEnabled(FeatureCatalog.REMOTE_SHORTCUTS) && streamType.equals("live", true)) switchChannel(1)
                true
            }
            KeyEvent.KEYCODE_INFO -> {
                showStreamInfo()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun showStreamInfo() {
        val state = when (player?.playbackState) {
            Player.STATE_READY -> "يعمل"
            Player.STATE_BUFFERING -> "يخزن مؤقتاً"
            Player.STATE_ENDED -> "منتهٍ"
            else -> "غير جاهز"
        }
        Toast.makeText(this, "نوع المحتوى: ${streamType ?: "غير محدد"}\nالحالة: $state", Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onStop() {
        inactivityReminderJob?.cancel()
        if (prefs.isFeatureEnabled(FeatureCatalog.PLAYER_BACKGROUND_AUDIO)) {
            Log.d(TAG, "🎧 onStop - Keeping audio active by user preference")
        } else {
            Log.d(TAG, "⏸️ onStop - Pausing player")
            player?.pause()
        }
        super.onStop()
    }

    override fun onDestroy() {
        sleepTimerJob?.cancel()
        inactivityReminderJob?.cancel()
        persistPlaybackState()
        Log.d(TAG, "🛑 onDestroy - Releasing player")
        player?.release()
        player = null
        super.onDestroy()
    }

    private fun persistPlaybackState() {
        if (prefs.isFeatureEnabled(FeatureCatalog.GUEST_MODE)) return
        val url = streamUrl ?: return
        val title = channelName?.takeIf { it.isNotBlank() } ?: return
        val type = streamType ?: "live"
        val activePlayer = player
        val entry = MediaLibraryEntry(
            id = "$type:$title",
            title = title,
            streamUrl = url,
            streamType = type,
            positionMs = activePlayer?.currentPosition ?: 0L,
            durationMs = activePlayer?.duration?.takeIf { it > 0L } ?: 0L
        )
        if (type.equals("live", ignoreCase = true)) {
            if (prefs.isFeatureEnabled(FeatureCatalog.RECENT_CHANNELS)) prefs.saveRecentChannel(entry)
        } else if (prefs.isFeatureEnabled(FeatureCatalog.WATCH_HISTORY)) {
            prefs.savePlayback(entry)
        }
    }

    private fun showSleepTimer() {
        val labels = arrayOf("بعد 15 دقيقة", "بعد 30 دقيقة", "بعد 60 دقيقة", "إلغاء المؤقت")
        val minutes = intArrayOf(15, 30, 60, 0)
        AlertDialog.Builder(this)
            .setTitle("مؤقت النوم")
            .setItems(labels) { _, index ->
                sleepTimerJob?.cancel()
                val selectedMinutes = minutes[index]
                if (selectedMinutes == 0) {
                    Toast.makeText(this, "تم إلغاء مؤقت النوم", Toast.LENGTH_SHORT).show()
                    return@setItems
                }
                sleepTimerJob = lifecycleScope.launch {
                    delay(selectedMinutes * 60_000L)
                    player?.pause()
                    Toast.makeText(this@PlayerActivity, "انتهى مؤقت النوم", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Toast.makeText(this, "سيتوقف التشغيل بعد $selectedMinutes دقيقة", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        scheduleInactivityReminder()
    }

    private fun scheduleInactivityReminder() {
        inactivityReminderJob?.cancel()
        if (!::prefs.isInitialized || !prefs.isFeatureEnabled(FeatureCatalog.IDLE_REMINDER)) return
        inactivityReminderJob = lifecycleScope.launch {
            delay(120 * 60_000L)
            Toast.makeText(this@PlayerActivity, "لا يزال البث يعمل. اضغط أي زر للمتابعة؛ سيتوقف محلياً بعد 5 دقائق.", Toast.LENGTH_LONG).show()
            delay(5 * 60_000L)
            player?.pause()
            finish()
        }
    }
}
