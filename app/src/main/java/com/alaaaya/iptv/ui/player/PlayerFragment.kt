package com.alaaaya.iptv.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.local.AppDatabase
import com.alaaaya.iptv.data.repository.ChannelRepository

class PlayerFragment : Fragment() {
    
    private lateinit var viewModel: PlayerViewModel
    private lateinit var playerView: PlayerView
    private lateinit var channelNameTextView: TextView
    private var exoPlayer: ExoPlayer? = null
    private var channelId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channelId = arguments?.getString(ARG_CHANNEL_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val channelRepository = ChannelRepository(database.channelDao())
        viewModel = PlayerViewModel(channelRepository)
        
        // Initialize views
        playerView = view.findViewById(R.id.player_view)
        channelNameTextView = view.findViewById(R.id.channel_name)
        
        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerView.player = exoPlayer
        
        // Set up player listener
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> viewModel.onBuffering()
                    Player.STATE_READY -> viewModel.onPlaying()
                    Player.STATE_IDLE, Player.STATE_ENDED -> {}
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                viewModel.onPlaybackError(error.message ?: "Playback error")
            }
        })
        
        // Observe player state
        viewModel.playerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlayerViewModel.PlayerState.Ready -> {
                    channelNameTextView.text = state.channel.name
                    // Play the stream
                    val streamUrl = state.channel.streamUrl
                    if (streamUrl.isNotEmpty()) {
                        val mediaItem = MediaItem.fromUri(streamUrl)
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                    }
                }
                is PlayerViewModel.PlayerState.Error -> {
                    channelNameTextView.text = "Error: ${state.message}"
                }
                is PlayerViewModel.PlayerState.Buffering -> {
                    // Show buffering indicator
                }
                is PlayerViewModel.PlayerState.Playing -> {
                    // Hide buffering indicator
                }
                else -> {}
            }
        }
        
        // Load channel
        channelId?.let { viewModel.loadChannel(it) }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        exoPlayer?.release()
        exoPlayer = null
    }
    
    companion object {
        private const val ARG_CHANNEL_ID = "channel_id"
        
        fun newInstance(channelId: String): PlayerFragment {
            return PlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHANNEL_ID, channelId)
                }
            }
        }
    }
}
