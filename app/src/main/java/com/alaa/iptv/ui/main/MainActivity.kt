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
    
    private var currentChannels = listOf<Channel>()
    private var currentCategories = listOf<Category>()
    private var selectedChannel: Channel? = null
    private var currentMode = MediaMode.LIVE_TV
    
    private enum class MediaMode {
        LIVE_TV, MOVIES, SERIES, FAVORITES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs)
        
        setupRecyclerViews()
        setupTabs()
        setupButtons()
        
        // Load Live TV by default
        loadLiveTV()
    }

    private fun setupRecyclerViews() {
        // Categories RecyclerView
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            loadChannelsByCategory(category)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
        
        // Channels RecyclerView
        channelAdapter = ChannelAdapter(
            emptyList(),
            onChannelClick = { channel ->
                updatePreview(channel)
            },
            onChannelLongClick = { channel ->
                Toast.makeText(this, getString(R.string.long_press_to_reorder), Toast.LENGTH_SHORT).show()
            }
        )
        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }

    private fun setupTabs() {
        binding.liveTvTab.setOnClickListener {
            currentMode = MediaMode.LIVE_TV
            loadLiveTV()
        }
        
        binding.moviesTab.setOnClickListener {
            currentMode = MediaMode.MOVIES
            loadMovies()
        }
        
        binding.seriesTab.setOnClickListener {
            currentMode = MediaMode.SERIES
            loadSeries()
        }
        
        binding.favoritesTab.setOnClickListener {
            currentMode = MediaMode.FAVORITES
            loadFavorites()
        }
    }

    private fun setupButtons() {
        binding.playButton.setOnClickListener {
            selectedChannel?.let { channel ->
                playChannel(channel)
            }
        }
        
        binding.favoriteButton.setOnClickListener {
            selectedChannel?.let { channel ->
                toggleFavorite(channel)
            }
        }
    }

    private fun loadLiveTV() {
        showLoading(true)
        lifecycleScope.launch {
            // Load categories
            repository.getLiveCategories().onSuccess { categories ->
                currentCategories = listOf(Category("0", getString(R.string.all_channels), 0)) + categories
                categoryAdapter.updateCategories(currentCategories)
            }
            
            // Load all channels
            repository.getLiveStreams().onSuccess { channels ->
                currentChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) {
                    updatePreview(channels[0])
                }
                showLoading(false)
            }.onFailure { error ->
                showLoading(false)
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadMovies() {
        showLoading(true)
        lifecycleScope.launch {
            // Load categories
            repository.getMovieCategories().onSuccess { categories ->
                currentCategories = listOf(Category("0", getString(R.string.all_channels), 0)) + categories
                categoryAdapter.updateCategories(currentCategories)
            }
            
            // Load all movies - convert to Channel for display
            repository.getMovies().onSuccess { movies ->
                val movieChannels = movies.map { movie ->
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
                currentChannels = movieChannels
                channelAdapter.updateChannels(movieChannels)
                if (movieChannels.isNotEmpty()) {
                    updatePreview(movieChannels[0])
                }
                showLoading(false)
            }.onFailure { error ->
                showLoading(false)
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSeries() {
        showLoading(true)
        lifecycleScope.launch {
            // Load categories
            repository.getSeriesCategories().onSuccess { categories ->
                currentCategories = listOf(Category("0", getString(R.string.all_channels), 0)) + categories
                categoryAdapter.updateCategories(currentCategories)
            }
            
            // Load all series - convert to Channel for display
            repository.getSeries().onSuccess { seriesList ->
                val seriesChannels = seriesList.map { series ->
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
                currentChannels = seriesChannels
                channelAdapter.updateChannels(seriesChannels)
                if (seriesChannels.isNotEmpty()) {
                    updatePreview(seriesChannels[0])
                }
                showLoading(false)
            }.onFailure { error ->
                showLoading(false)
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFavorites() {
        val favorites = prefs.getFavorites()
        val favoriteChannels = currentChannels.filter { favorites.contains(it.streamId) }
        channelAdapter.updateChannels(favoriteChannels)
        if (favoriteChannels.isNotEmpty()) {
            updatePreview(favoriteChannels[0])
        }
        
        // Clear categories for favorites view
        categoryAdapter.updateCategories(emptyList())
    }

    private fun loadChannelsByCategory(category: Category) {
        if (category.categoryId == "0") {
            // Show all
            channelAdapter.updateChannels(currentChannels)
            return
        }
        
        showLoading(true)
        lifecycleScope.launch {
            when (currentMode) {
                MediaMode.LIVE_TV -> {
                    repository.getLiveStreams(category.categoryId).onSuccess { channels ->
                        channelAdapter.updateChannels(channels)
                        if (channels.isNotEmpty()) {
                            updatePreview(channels[0])
                        }
                        showLoading(false)
                    }.onFailure { error ->
                        showLoading(false)
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                    }
                }
                MediaMode.MOVIES -> {
                    repository.getMovies(category.categoryId).onSuccess { movies ->
                        val movieChannels = movies.map { movie ->
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
                        channelAdapter.updateChannels(movieChannels)
                        if (movieChannels.isNotEmpty()) {
                            updatePreview(movieChannels[0])
                        }
                        showLoading(false)
                    }.onFailure { error ->
                        showLoading(false)
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                    }
                }
                MediaMode.SERIES -> {
                    repository.getSeries(category.categoryId).onSuccess { seriesList ->
                        val seriesChannels = seriesList.map { series ->
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
                        channelAdapter.updateChannels(seriesChannels)
                        if (seriesChannels.isNotEmpty()) {
                            updatePreview(seriesChannels[0])
                        }
                        showLoading(false)
                    }.onFailure { error ->
                        showLoading(false)
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    showLoading(false)
                }
            }
        }
    }

    private fun updatePreview(channel: Channel) {
        selectedChannel = channel
        binding.previewTitle.text = channel.name
        binding.previewInfo.text = "Channel ${channel.num}"
        
        // Load preview image
        if (!channel.streamIcon.isNullOrEmpty()) {
            Glide.with(this)
                .load(channel.streamIcon)
                .placeholder(R.drawable.app_banner)
                .error(R.drawable.app_banner)
                .into(binding.previewImage)
        } else {
            binding.previewImage.setImageResource(R.drawable.app_banner)
        }
        
        // Update favorite button
        binding.favoriteButton.text = if (channel.isFavorite) "❤" else "♡"
    }

    private fun playChannel(channel: Channel) {
        val streamUrl = if (channel.directSource != null) {
            channel.directSource
        } else {
            channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        }
        
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("STREAM_URL", streamUrl)
            putExtra("CHANNEL_NAME", channel.name)
        }
        startActivity(intent)
    }

    private fun toggleFavorite(channel: Channel) {
        val favorites = prefs.getFavorites().toMutableSet()
        if (channel.isFavorite) {
            favorites.remove(channel.streamId)
            channel.isFavorite = false
            Toast.makeText(this, getString(R.string.remove_from_favorites), Toast.LENGTH_SHORT).show()
        } else {
            favorites.add(channel.streamId)
            channel.isFavorite = true
            Toast.makeText(this, getString(R.string.add_to_favorites), Toast.LENGTH_SHORT).show()
        }
        prefs.saveFavorites(favorites)
        
        // Update the button
        binding.favoriteButton.text = if (channel.isFavorite) "❤" else "♡"
        
        // Refresh adapter
        channelAdapter.notifyDataSetChanged()
    }

    private fun showLoading(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
    }
}
