package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.ui.player.PlayerActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_MODE = "MODE"
        const val MODE_LIVE = "live"
        const val MODE_MOVIES = "movies"
        const val MODE_SERIES = "series"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private lateinit var channelAdapter: ChannelAdapter
    private var allChannels: List<Channel> = emptyList()
    private var currentMode = "live"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_LIVE

        setupChannelsList()
        loadContent()
    }

    private fun setupChannelsList() {
        channelAdapter = ChannelAdapter(
            emptyList(),
            onChannelClick = { channel -> playChannel(channel) },
            onChannelLongClick = { channel -> /* Toggle Favorite */ }
        )

        channelAdapter.setOnChannelFocusListener { channel ->
            updatePreview(channel)
        }

        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }

    private fun loadContent() {
        lifecycleScope.launch {
            try {
                val result = when (currentMode) {
                    MODE_MOVIES -> repository.getMovies(null).map { list -> list.map { it.toChannel() } }
                    MODE_SERIES -> repository.getSeries(null).map { list -> list.map { it.toChannel() } }
                    else -> repository.getLiveStreams(null)
                }

                if (result.isSuccess) {
                    allChannels = result.getOrDefault(emptyList())
                    channelAdapter.updateChannels(allChannels)
                    if (allChannels.isNotEmpty()) {
                        updatePreview(allChannels[0])
                    }
                    binding.channelCountFooter.text = "1 / ${allChannels.size}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading content", e)
            }
        }
    }

    private fun updatePreview(channel: Channel) {
        binding.previewTitle.text = channel.name
        binding.previewSubtitle.text = channel.categoryName ?: "بث مباشر"
        
        Glide.with(this)
            .load(channel.streamIcon)
            .placeholder(R.drawable.bg_hero_sports)
            .error(R.drawable.bg_hero_sports)
            .into(binding.previewImage)
            
        val pos = allChannels.indexOf(channel) + 1
        binding.channelCountFooter.text = "$pos / ${allChannels.size}"
    }

    private fun playChannel(channel: Channel) {
        val url = channel.directSource ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isNullOrBlank()) return

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
                .putExtra("STREAM_TYPE", channel.streamType)
        )
    }
    
    // Helper to convert Movie/Series to Channel for unified list
    private fun com.alaa.iptv.data.models.Movie.toChannel() = Channel(
        streamId = streamId, num = streamId, name = name, streamType = "movie",
        streamIcon = streamIcon, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )
    
    private fun com.alaa.iptv.data.models.Series.toChannel() = Channel(
        streamId = seriesId, num = seriesId, name = name, streamType = "series",
        streamIcon = cover, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )
}
