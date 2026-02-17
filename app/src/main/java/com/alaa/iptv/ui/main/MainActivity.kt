package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.ui.player.PlayerActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository

    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var allChannels: List<Channel> = emptyList()
    private var selectedChannel: Channel? = null
    private var currentMode = MediaMode.LIVE_TV

    private enum class MediaMode {
        LIVE_TV, MOVIES, SERIES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        val mode = intent.getStringExtra("MODE") ?: "live"
        currentMode = when (mode) {
            "movies" -> MediaMode.MOVIES
            "series" -> MediaMode.SERIES
            else -> MediaMode.LIVE_TV
        }

        setupRecyclerViews()
        setupButtons()

        when (currentMode) {
            MediaMode.MOVIES -> {
                binding.moduleTitle.text = "الأفلام"
                loadMovies()
            }
            MediaMode.SERIES -> {
                binding.moduleTitle.text = "المسلسلات"
                loadSeries()
            }
            MediaMode.LIVE_TV -> {
                binding.moduleTitle.text = "القنوات"
                loadLiveTV()
            }
        }
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(emptyList()) { category: Category ->
            if (currentMode == MediaMode.LIVE_TV) {
                binding.categoryTitle.text = category.categoryName
                loadChannelsByCategory(category)
            }
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryAdapter
        }

        channelAdapter = ChannelAdapter(
            emptyList(),
            onChannelClick = { channel: Channel -> updatePreview(channel) },
            onChannelLongClick = {
                // Handle long click if needed
            }
        )

        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadLiveTV() {
        lifecycleScope.launch {
            repository.getLiveCategories().onSuccess { categories: List<Category> ->
                categoryAdapter.updateCategories(
                    listOf(Category("0", "كل القنوات", 0)) + categories
                )
            }

            repository.getLiveStreams(null).onSuccess { channels: List<Channel> ->
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
            }
        }
    }

    private fun loadChannelsByCategory(category: Category) {
        if (category.categoryId == "0") {
            channelAdapter.updateChannels(allChannels)
            if (allChannels.isNotEmpty()) updatePreview(allChannels[0])
            return
        }

        lifecycleScope.launch {
            repository.getLiveStreams(category.categoryId).onSuccess { channels: List<Channel> ->
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
            }
        }
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            repository.getMovies(null).onSuccess { movies: List<Movie> ->
                val channels = movies.map { movie ->
                    Channel(
                        streamId = movie.streamId,
                        num = movie.streamId,
                        name = movie.name,
                        streamType = "movie",
                        streamIcon = movie.streamIcon,
                        epgChannelId = null,
                        added = null,
                        categoryId = movie.categoryId,
                        categoryName = null,
                        customSid = null,
                        tvArchive = 0,
                        directSource = movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password),
                        tvArchiveDuration = 0,
                        isFavorite = movie.isFavorite
                    )
                }
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
            }
        }
    }

    private fun loadSeries() {
        lifecycleScope.launch {
            repository.getSeries(null).onSuccess { seriesList: List<Series> ->
                val channels = seriesList.map { series ->
                    Channel(
                        streamId = series.seriesId,
                        num = series.seriesId,
                        name = series.name,
                        streamType = "series",
                        streamIcon = series.cover,
                        epgChannelId = null,
                        added = null,
                        categoryId = series.categoryId,
                        categoryName = null,
                        customSid = null,
                        tvArchive = 0,
                        directSource = null,
                        tvArchiveDuration = 0,
                        isFavorite = series.isFavorite
                    )
                }
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
            }
        }
    }

    private fun updatePreview(channel: Channel) {
        selectedChannel = channel
        binding.previewTitle.text = channel.name

        Glide.with(this)
            .load(channel.streamIcon)
            .placeholder(R.drawable.app_banner)
            .into(binding.previewImage)
    }

    private fun playChannel(channel: Channel) {
        val url = channel.directSource
            ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
        )
    }
}
