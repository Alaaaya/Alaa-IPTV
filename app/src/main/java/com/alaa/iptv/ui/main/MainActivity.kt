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

    private var allChannels = listOf<Channel>()
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

        // 🔹 قراءة MODE القادم من Dashboard
        val mode = intent.getStringExtra("MODE") ?: "live"
        currentMode = when (mode) {
            "movies" -> MediaMode.MOVIES
            "series" -> MediaMode.SERIES
            else -> MediaMode.LIVE_TV
        }

        setupRecyclerViews()
        setupTabs()
        setupButtons()

        // 🔹 تحميل المحتوى حسب MODE
        when (currentMode) {
            MediaMode.MOVIES -> {
                highlightTab(binding.moviesTab)
                loadMovies()
            }
            MediaMode.SERIES -> {
                highlightTab(binding.seriesTab)
                loadSeries()
            }
            else -> {
                highlightTab(binding.liveTvTab)
                loadLiveTV()
            }
        }
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            loadChannelsByCategory(category)
        }
        binding.categoriesRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }

        channelAdapter = ChannelAdapter(
            emptyList(),
            onChannelClick = { updatePreview(it) },
            onChannelLongClick = {
                Toast.makeText(
                    this,
                    getString(R.string.long_press_to_reorder),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onReorderRequest = { _, _ -> }
        )

        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }

    private fun setupTabs() {
        binding.liveTvTab.setOnClickListener {
            currentMode = MediaMode.LIVE_TV
            highlightTab(binding.liveTvTab)
            loadLiveTV()
        }

        binding.moviesTab.setOnClickListener {
            currentMode = MediaMode.MOVIES
            highlightTab(binding.moviesTab)
            loadMovies()
        }

        binding.seriesTab.setOnClickListener {
            currentMode = MediaMode.SERIES
            highlightTab(binding.seriesTab)
            loadSeries()
        }
    }

    private fun highlightTab(tab: View) {
        listOf(binding.liveTvTab, binding.moviesTab, binding.seriesTab).forEach {
            it.alpha = 0.6f
        }
        tab.alpha = 1f
    }

    private fun setupButtons() {
        binding.playButton.setOnClickListener {
            selectedChannel?.let { playChannel(it) }
        }
    }

    // ===================== LIVE TV =====================

    private fun loadLiveTV() {
        showLoading(true)
        lifecycleScope.launch {
            repository.getLiveCategories().onSuccess { categories ->
                categoryAdapter.updateCategories(
                    listOf(Category("0", getString(R.string.all_channels), 0)) + categories
                )
            }

            repository.getLiveStreams(null).onSuccess { channels ->
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
                showLoading(false)
            }.onFailure {
                showLoading(false)
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
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
            repository.getLiveStreams(category.categoryId).onSuccess {
                channelAdapter.updateChannels(it)
                if (it.isNotEmpty()) updatePreview(it[0])
            }
        }
    }

    // ===================== MOVIES =====================

    private fun loadMovies() {
        showLoading(true)
        lifecycleScope.launch {
            repository.getMovies(null).onSuccess { movies ->
                val channels = movies.map {
                    Channel(
                        streamId = it.streamId,
                        num = it.streamId,
                        name = it.name,
                        streamType = "movie",
                        streamIcon = it.streamIcon,
                        epgChannelId = null,
                        added = null,
                        categoryId = it.categoryId,
                        categoryName = null,
                        customSid = null,
                        tvArchive = 0,
                        directSource = it.getStreamUrl(
                            prefs.serverUrl,
                            prefs.username,
                            prefs.password
                        ),
                        tvArchiveDuration = 0,
                        isFavorite = it.isFavorite
                    )
                }
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
                showLoading(false)
            }
        }
    }

    // ===================== SERIES =====================

    private fun loadSeries() {
        showLoading(true)
        lifecycleScope.launch {
            repository.getSeries(null).onSuccess { series ->
                val channels = series.map {
                    Channel(
                        streamId = it.seriesId,
                        num = it.seriesId,
                        name = it.name,
                        streamType = "series",
                        streamIcon = it.cover,
                        epgChannelId = null,
                        added = null,
                        categoryId = it.categoryId,
                        categoryName = null,
                        customSid = null,
                        tvArchive = 0,
                        directSource = null,
                        tvArchiveDuration = 0,
                        isFavorite = it.isFavorite
                    )
                }
                allChannels = channels
                channelAdapter.updateChannels(channels)
                if (channels.isNotEmpty()) updatePreview(channels[0])
                showLoading(false)
            }
        }
    }

    private fun updatePreview(channel: Channel) {
        selectedChannel = channel
        binding.previewTitle.text = channel.name
        binding.previewInfo.text = channel.streamType ?: ""

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

    private fun showLoading(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
    }
}
