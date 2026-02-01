package com.alaaaya.iptv.ui.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.alaaaya.iptv.R
import com.alaaaya.iptv.utils.Constants
import com.alaaaya.iptv.utils.gone
import com.alaaaya.iptv.utils.visible

class PlayerActivity : AppCompatActivity() {
    
    private lateinit var playerView: PlayerView
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var errorContainer: LinearLayout
    private lateinit var tvErrorText: TextView
    private lateinit var btnRetry: Button
    
    private var player: ExoPlayer? = null
    private var streamUrl: String? = null
    private var streamTitle: String? = null
    private var streamId: String? = null
    private var streamType: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        
        // Get intent data
        streamUrl = intent.getStringExtra(Constants.EXTRA_STREAM_URL)
        streamTitle = intent.getStringExtra(Constants.EXTRA_STREAM_TITLE)
        streamId = intent.getStringExtra(Constants.EXTRA_STREAM_ID)
        streamType = intent.getStringExtra(Constants.EXTRA_STREAM_TYPE)
        
        initViews()
        setupPlayer()
    }
    
    private fun initViews() {
        playerView = findViewById(R.id.playerView)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        errorContainer = findViewById(R.id.errorContainer)
        tvErrorText = findViewById(R.id.tvErrorText)
        btnRetry = findViewById(R.id.btnRetry)
        
        btnRetry.setOnClickListener {
            errorContainer.gone()
            setupPlayer()
        }
    }
    
    private fun setupPlayer() {
        // Release any existing player
        releasePlayer()
        
        if (streamUrl.isNullOrEmpty()) {
            showError("Invalid stream URL")
            return
        }
        
        // Create player
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                playerView.player = exoPlayer
                
                // Prepare media item
                val mediaItem = MediaItem.fromUri(streamUrl!!)
                exoPlayer.setMediaItem(mediaItem)
                
                // Add listener
                exoPlayer.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                showLoading("Buffering...")
                            }
                            Player.STATE_READY -> {
                                hideLoading()
                                hideError()
                            }
                            Player.STATE_ENDED -> {
                                // Stream ended
                                finish()
                            }
                            Player.STATE_IDLE -> {
                                // Idle
                            }
                        }
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        showError("Error playing stream: ${error.message}")
                    }
                })
                
                // Prepare and play
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                
                showLoading("Loading stream...")
            }
    }
    
    private fun showLoading(message: String) {
        loadingContainer.visible()
        tvLoadingText.text = message
        errorContainer.gone()
    }
    
    private fun hideLoading() {
        loadingContainer.gone()
    }
    
    private fun showError(message: String) {
        errorContainer.visible()
        tvErrorText.text = message
        loadingContainer.gone()
    }
    
    private fun hideError() {
        errorContainer.gone()
    }
    
    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.release()
        }
        player = null
    }
    
    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        player?.play()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        
        // Handle playback controls
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                player?.let { exoPlayer ->
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
                return true
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                finish()
                return true
            }
        }
        
        return super.onKeyDown(keyCode, event)
    }
}
