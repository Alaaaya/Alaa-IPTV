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
    private lateinit var categoryAdapter: CategoryAdapter

    private var allChannels: List<Channel> = emptyList()
    private var selectedChannel: Channel? = null
    private var currentMode = MediaMode.LIVE_TV

    private enum class MediaMode {
        LIVE_TV, MOVIES, SERIES
    }

    // ================= LIFECYCLE =================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        currentMode = when (intent.getStringExtra(EXTRA_MODE)) {
            MODE_MOVIES -> MediaMode.MOVIES
            MODE_SERIES -> MediaMode.SERIES
            else        -> MediaMode.LIVE_TV
        }

        setupRecyclerViews()
        setupButtons()
        loadContent()
    }

    // ================= SETUP =================

    private fun setupRecyclerViews() {

        // Adapter الفئات
        categoryAdapter = CategoryAdapter(emptyList()) { category ->
            binding.categoryTitle.text = category.categoryName
            filterByCategory(category)
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryAdapter
            isFocusable = true
            isFocusableInTouchMode = true
        }

        // Adapter القنوات
        channelAdapter = ChannelAdapter(
            emptyList(),
            onChannelClick      = { channel -> updatePreview(channel) },
            onChannelLongClick  = { channel -> playChannel(channel) }
        )

        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
            isFocusable = true
            isFocusableInTouchMode = true
        }
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener { finish() }
    }

    // ================= LOAD CONTENT =================

    private fun loadContent() {
        when (currentMode) {
            MediaMode.LIVE_TV -> {
                binding.moduleTitle.text = "القنوات"
                loadLiveTV()
            }
            MediaMode.MOVIES -> {
                binding.moduleTitle.text = "الأفلام"
                loadMovies()
            }
            MediaMode.SERIES -> {
                binding.moduleTitle.text = "المسلسلات"
                loadSeries()
            }
        }
    }

    // ================= LIVE TV =================

    private fun loadLiveTV() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                // جلب القنوات
                val streamsResult = repository.getLiveStreams(null)

                if (streamsResult.isFailure) {
                    showError("فشل تحميل القنوات: ${streamsResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val channels = streamsResult.getOrDefault(emptyList())
                allChannels = channels

                Log.d(TAG, "Loaded ${channels.size} channels")

                // ===== الفئات =====
                val categories: List<Category>

                if (repository.isM3U()) {
                    // M3U: استخرج الفئات من القنوات مباشرة
                    categories = channels
                        .mapNotNull { it.categoryName?.trim()?.takeIf { n -> n.isNotBlank() } }
                        .distinct()
                        .sorted()
                        .map { name -> Category(categoryId = name, categoryName = name, parentId = 0) }

                    Log.d(TAG, "M3U categories: ${categories.size}")

                } else {
                    // Xtream Codes: جلب الفئات من API
                    val catsResult = repository.getLiveCategories()
                    categories = catsResult.getOrDefault(emptyList())

                    Log.d(TAG, "Xtream categories: ${categories.size}")
                }

                // إضافة "كل القنوات" في البداية
                val allCats = mutableListOf(
                    Category(categoryId = "all", categoryName = "كل القنوات", parentId = 0)
                ).apply { addAll(categories) }

                // تحديث الواجهة
                categoryAdapter.updateCategories(allCats)
                channelAdapter.updateChannels(channels)

                if (channels.isNotEmpty()) {
                    updatePreview(channels[0])
                }

                showLoading(false)
                binding.categoriesRecyclerView.requestFocus()

            } catch (e: Exception) {
                Log.e(TAG, "loadLiveTV exception", e)
                showError("خطأ غير متوقع: ${e.message}")
            }
        }
    }

    // ================= MOVIES =================

    private fun loadMovies() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val moviesResult  = repository.getMovies(null)
                val catsResult    = repository.getMovieCategories()

                if (moviesResult.isFailure) {
                    showError("فشل تحميل الأفلام: ${moviesResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val movies = moviesResult.getOrDefault(emptyList())

                // تحويل الأفلام إلى Channel للعرض
                allChannels = movies.map { movie ->
                    Channel(
                        streamId        = movie.streamId,
                        num             = movie.streamId,
                        name            = movie.name,
                        streamType      = "movie",
                        streamIcon      = movie.streamIcon,
                        epgChannelId    = null,
                        added           = null,
                        categoryId      = movie.categoryId,
                        categoryName    = null,
                        customSid       = null,
                        tvArchive       = 0,
                        directSource    = movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password),
                        tvArchiveDuration = 0,
                        isFavorite      = movie.isFavorite
                    )
                }

                Log.d(TAG, "Loaded ${movies.size} movies")

                // الفئات
                val cats = catsResult.getOrDefault(emptyList())
                val allCats = mutableListOf(
                    Category(categoryId = "all", categoryName = "كل الأفلام", parentId = 0)
                ).apply { addAll(cats) }

                categoryAdapter.updateCategories(allCats)
                channelAdapter.updateChannels(allChannels)

                if (allChannels.isNotEmpty()) updatePreview(allChannels[0])

                showLoading(false)
                binding.channelsRecyclerView.requestFocus()

            } catch (e: Exception) {
                Log.e(TAG, "loadMovies exception", e)
                showError("خطأ: ${e.message}")
            }
        }
    }

    // ================= SERIES =================

    private fun loadSeries() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val seriesResult = repository.getSeries(null)
                val catsResult   = repository.getSeriesCategories()

                if (seriesResult.isFailure) {
                    showError("فشل تحميل المسلسلات: ${seriesResult.exceptionOrNull()?.message}")
                    return@launch
                }

                val seriesList = seriesResult.getOrDefault(emptyList())

                // تحويل المسلسلات إلى Channel
                allChannels = seriesList.map { series ->
                    Channel(
                        streamId        = series.seriesId,
                        num             = series.seriesId,
                        name            = series.name,
                        streamType      = "series",
                        streamIcon      = series.cover,
                        epgChannelId    = null,
                        added           = null,
                        categoryId      = series.categoryId,
                        categoryName    = null,
                        customSid       = null,
                        tvArchive       = 0,
                        directSource    = null,
                        tvArchiveDuration = 0,
                        isFavorite      = series.isFavorite
                    )
                }

                Log.d(TAG, "Loaded ${seriesList.size} series")

                // الفئات
                val cats = catsResult.getOrDefault(emptyList())
                val allCats = mutableListOf(
                    Category(categoryId = "all", categoryName = "كل المسلسلات", parentId = 0)
                ).apply { addAll(cats) }

                categoryAdapter.updateCategories(allCats)
                channelAdapter.updateChannels(allChannels)

                if (allChannels.isNotEmpty()) updatePreview(allChannels[0])

                showLoading(false)
                binding.channelsRecyclerView.requestFocus()

            } catch (e: Exception) {
                Log.e(TAG, "loadSeries exception", e)
                showError("خطأ: ${e.message}")
            }
        }
    }

    // ================= FILTER BY CATEGORY =================

    private fun filterByCategory(category: Category) {

        val filtered = if (category.categoryId == "all") {
            allChannels
        } else {
            allChannels.filter { channel ->
                // دعم Xtream (categoryId) وM3U (categoryName)
                channel.categoryId == category.categoryId ||
                channel.categoryName == category.categoryName
            }
        }

        Log.d(TAG, "Filter '${category.categoryName}': ${filtered.size} items")

        channelAdapter.updateChannels(filtered)

        if (filtered.isNotEmpty()) {
            updatePreview(filtered[0])
            binding.channelsRecyclerView.scrollToPosition(0)
        }
    }

    // ================= PREVIEW =================

    private fun updatePreview(channel: Channel) {
        selectedChannel = channel
        binding.previewTitle.text = channel.name

        Glide.with(this)
            .load(channel.streamIcon)
            .placeholder(R.drawable.app_banner)
            .error(R.drawable.app_banner)
            .into(binding.previewImage)
    }

    // ================= PLAY =================

    private fun playChannel(channel: Channel) {
        val url = channel.directSource
            ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)

        if (url.isNullOrBlank()) {
            Toast.makeText(this, "لا يوجد رابط للبث", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Playing: $url")

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
        )
    }

    // ================= UI HELPERS =================

    private fun showLoading(show: Boolean) {
        // إذا عندك ProgressBar في الـ layout اسمها progressBar فعّله هنا
        // binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        showLoading(false)
        Log.e(TAG, message)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
