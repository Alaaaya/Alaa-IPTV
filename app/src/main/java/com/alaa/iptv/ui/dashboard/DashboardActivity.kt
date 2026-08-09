package com.alaa.iptv.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.ui.main.MainActivity
import com.alaa.iptv.ui.player.PlayerActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DashboardActivity"
    }

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository

    private var allChannels: List<Channel> = emptyList()
    private var allMovies: List<Channel> = emptyList()
    private var allSeries: List<Channel> = emptyList()

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateDateTime()
            handler.postDelayed(this, 60000) // update every minute
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        setupSidebar()
        setupHeroBanner()
        setupCategories()
        setupContinueWatching()
        setupBottomInfo()
        startClock()
        loadContent()
    }

    // ================= SIDEBAR =================

    private fun setupSidebar() {
        val items = listOf(
            SidebarItem("Live TV", R.drawable.ic_live_tv, true) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem("Movies", R.drawable.ic_movies, false) { openMain(MainActivity.MODE_MOVIES) },
            SidebarItem("Series", R.drawable.ic_series, false) { openMain(MainActivity.MODE_SERIES) },
            SidebarItem("Favorites", R.drawable.ic_favorite, false) { /* TODO */ },
            SidebarItem("Recently", R.drawable.ic_recent, false) { /* TODO */ },
            SidebarItem("Categories", R.drawable.ic_categories, false) { /* TODO */ },
            SidebarItem("Change Server", R.drawable.ic_server, false) { /* TODO */ },
            SidebarItem("Settings", R.drawable.ic_settings, false) { /* TODO */ }
        )

        val adapter = SidebarAdapter(items) { item ->
            item.action.invoke()
        }

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = adapter
            post {
                val firstView = layoutManager?.findViewByPosition(0)
                firstView?.requestFocus()
            }
        }
    }

    // ================= HERO BANNER =================

    private fun setupHeroBanner() {
        // Static hero for now - can be dynamic based on featured content
        binding.heroTitle.text = "Live TV"
        binding.heroSubtitle.text = "Watch 1000+ Live Channels"
        binding.heroWatchNow.setOnClickListener {
            openMain(MainActivity.MODE_LIVE)
        }

        // Load hero background
        Glide.with(this)
            .load(R.drawable.bg_hero_sports)
            .placeholder(R.drawable.bg_dark_pattern)
            .into(binding.heroImage)
    }

    // ================= CATEGORIES =================

    private fun setupCategories() {
        // Will be populated after loading content
    }

    private fun updateCategories(categories: List<CategoryItem>) {
        val adapter = CategoryCardAdapter(categories) { category ->
            when (category.type) {
                "live" -> openMain(MainActivity.MODE_LIVE)
                "movie" -> openMain(MainActivity.MODE_MOVIES)
                "series" -> openMain(MainActivity.MODE_SERIES)
            }
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@DashboardActivity, 1, GridLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    // ================= CONTINUE WATCHING =================

    private fun setupContinueWatching() {
        // Will show recent/mixed content
    }

    private fun updateContinueWatching(items: List<ContinueWatchingItem>) {
        val adapter = ContinueWatchingAdapter(items) { item ->
            playContent(item)
        }

        binding.continueWatchingRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    // ================= BOTTOM INFO =================

    private fun setupBottomInfo() {
        binding.infoSecure.text = "Secure & Safe"
        binding.infoSecureDesc.text = "Your data is protected"
        binding.infoUptime.text = "99.9% Uptime"
        binding.infoUptimeDesc.text = "Reliable Servers"
        binding.infoQuality.text = "High Quality"
        binding.infoQualityDesc.text = "HD / FHD / 4K"
        binding.infoSupport.text = "24/7 Support"
        binding.infoSupportDesc.text = "Always Here to Help"
    }

    // ================= CLOCK =================

    private fun startClock() {
        updateDateTime()
        handler.post(updateTimeRunnable)
    }

    private fun updateDateTime() {
        val now = Date()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
        binding.timeText.text = timeFormat.format(now)

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("ar"))
        binding.dateText.text = dateFormat.format(now)
    }

    // ================= CONTENT LOADING =================

    private fun loadContent() {
        lifecycleScope.launch {
            try {
                // Load channels for counts
                val channelsResult = repository.getLiveStreams(null)
                val channels = channelsResult.getOrDefault(emptyList())
                allChannels = channels

                // Load movies
                val moviesResult = repository.getMovies(null)
                val movies = moviesResult.getOrDefault(emptyList())
                allMovies = movies.map { movie ->
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

                // Load series
                val seriesResult = repository.getSeries(null)
                val seriesList = seriesResult.getOrDefault(emptyList())
                allSeries = seriesList.map { series ->
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

                // Build category cards
                val categories = mutableListOf<CategoryItem>()
                categories.add(CategoryItem("All Channels", channels.size, R.drawable.ic_live_tv, "#E53935", "live"))
                categories.add(CategoryItem("Sports", channels.count { it.categoryName?.contains("sport", true) == true || it.name.contains("sport", true) }, R.drawable.ic_sports, "#1E88E5", "live"))
                categories.add(CategoryItem("News", channels.count { it.categoryName?.contains("news", true) == true || it.name.contains("news", true) }, R.drawable.ic_news, "#43A047", "live"))
                categories.add(CategoryItem("Movies", movies.size, R.drawable.ic_movies, "#E53935", "movie"))
                categories.add(CategoryItem("Kids", channels.count { it.categoryName?.contains("kids", true) == true || it.name.contains("kids", true) }, R.drawable.ic_kids, "#FB8C00", "live"))
                categories.add(CategoryItem("Documentary", channels.count { it.categoryName?.contains("doc", true) == true || it.name.contains("doc", true) }, R.drawable.ic_documentary, "#00897B", "live"))
                categories.add(CategoryItem("More", 0, R.drawable.ic_more, "#5E35B1", "live"))

                updateCategories(categories)

                // Build continue watching (mix of movies and series)
                val continueItems = mutableListOf<ContinueWatchingItem>()
                allMovies.take(5).forEach { movie ->
                    continueItems.add(ContinueWatchingItem(
                        id = movie.streamId,
                        title = movie.name,
                        subtitle = "Movie",
                        imageUrl = movie.streamIcon,
                        progress = (0..100).random(),
                        channel = movie
                    ))
                }
                allSeries.take(3).forEach { series ->
                    continueItems.add(ContinueWatchingItem(
                        id = series.streamId,
                        title = series.name,
                        subtitle = "Series",
                        imageUrl = series.streamIcon,
                        progress = (0..100).random(),
                        channel = series
                    ))
                }

                updateContinueWatching(continueItems)

                // Update hero with first movie if available
                if (allMovies.isNotEmpty()) {
                    val featured = allMovies.first()
                    binding.heroTitle.text = featured.name
                    binding.heroSubtitle.text = "Featured Movie"
                    if (!featured.streamIcon.isNullOrEmpty()) {
                        Glide.with(this@DashboardActivity)
                            .load(featured.streamIcon)
                            .placeholder(R.drawable.bg_hero_sports)
                            .into(binding.heroImage)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error loading content", e)
                Toast.makeText(this@DashboardActivity, "Error loading content", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ================= NAVIGATION =================

    private fun openMain(mode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_MODE, mode)
        }
        startActivity(intent)
    }

    private fun playContent(item: ContinueWatchingItem) {
        val channel = item.channel
        val url = channel.directSource
            ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)

        if (url.isNullOrBlank()) {
            Toast.makeText(this, "No stream URL", Toast.LENGTH_SHORT).show()
            return
        }

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
                .putExtra("STREAM_TYPE", channel.streamType)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        _binding = null
    }
}

// ================= DATA CLASSES =================

data class SidebarItem(
    val title: String,
    val iconRes: Int,
    val isSelected: Boolean = false,
    val action: () -> Unit
)

data class CategoryItem(
    val name: String,
    val count: Int,
    val iconRes: Int,
    val colorHex: String,
    val type: String
)

data class ContinueWatchingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val progress: Int,
    val channel: Channel
)
