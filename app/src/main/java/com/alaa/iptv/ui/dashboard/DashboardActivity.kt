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
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.ui.main.MainActivity
import com.alaa.iptv.ui.main.MoviesActivity
import com.alaa.iptv.ui.main.SeriesActivity
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.utils.UpdateChecker
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
            if (_binding != null) {
                updateDateTime()
                handler.postDelayed(this, 60000)
            }
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
        checkUpdates()
        startClock()

        binding.root.post {
            loadContent()
        }
    }

    private fun checkUpdates() {
        lifecycleScope.launch {
            try {
                UpdateChecker(this@DashboardActivity).checkForUpdate(showToast = false)
            } catch (e: Exception) {
                Log.e(TAG, "Update check failed", e)
            }
        }
    }

    private fun setupSidebar() {
        val items = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, true) { /* Home */ },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, false) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem(getString(R.string.menu_movies), R.drawable.ic_movies, false) { openMain(MainActivity.MODE_MOVIES) },
            SidebarItem(getString(R.string.menu_series), R.drawable.ic_series, false) { openMain(MainActivity.MODE_SERIES) },
            SidebarItem(getString(R.string.menu_favorites), R.drawable.ic_favorite, false) { openMain(MainActivity.MODE_FAVORITES) },
            SidebarItem(getString(R.string.menu_recent), R.drawable.ic_recent, false) { showToast("قريباً") },
            SidebarItem(getString(R.string.menu_categories), R.drawable.ic_categories, false) { showToast("قريباً") },
            SidebarItem(getString(R.string.menu_server), R.drawable.ic_server, false) { showToast("قريباً") },
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { showToast("قريباً") }
        )

        val adapter = SidebarAdapter(items) { item ->
            item.action.invoke()
        }

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = adapter
        }
        
        binding.securityTitle.text = getString(R.string.security_title)
        binding.securityDesc.text = getString(R.string.security_desc)
    }

    private fun setupHeroBanner() {
        binding.heroTitle.text = getString(R.string.hero_title)
        binding.heroSubtitle.text = getString(R.string.hero_desc)
        binding.heroWatchNow.text = getString(R.string.watch_now)
        binding.heroWatchNow.setOnClickListener {
            openMain(MainActivity.MODE_LIVE)
        }

        Glide.with(this)
            .load(R.drawable.bg_hero_sports)
            .placeholder(R.drawable.bg_dark_pattern)
            .into(binding.heroImage)
    }

    private fun setupCategories() {
        binding.categoriesHeaderTitle.text = getString(R.string.browse_categories)
        binding.categoriesViewAll.text = getString(R.string.view_all)
        updateCategories(emptyList())
    }

    private fun updateCategories(categories: List<CategoryItem>) {
        if (_binding == null) return
        val adapter = CategoryCardAdapter(categories) { category ->
            when (category.type) {
                "live" -> openMain(MainActivity.MODE_LIVE)
                "movie" -> openMain(MainActivity.MODE_MOVIES)
                "series" -> openMain(MainActivity.MODE_SERIES)
                else -> openMain(MainActivity.MODE_LIVE)
            }
        }
        binding.categoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@DashboardActivity, 1, GridLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    private fun setupContinueWatching() {
        binding.continueWatchingHeaderTitle.text = getString(R.string.continue_watching)
        binding.continueWatchingViewAll.text = getString(R.string.view_all)
        updateContinueWatching(emptyList())
    }

    private fun updateContinueWatching(items: List<ContinueWatchingItem>) {
        if (_binding == null) return
        val hasItems = items.isNotEmpty()
        binding.continueWatchingHeaderTitle.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.continueWatchingViewAll.visibility = if (hasItems) View.VISIBLE else View.GONE
        binding.continueWatchingRecyclerView.visibility = if (hasItems) View.VISIBLE else View.GONE
        if (!hasItems) return

        binding.continueWatchingRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = ContinueWatchingAdapter(items, ::playContent)
        }
    }

    private fun setupBottomInfo() {
        if (_binding == null) return
        binding.infoSecure.text = getString(R.string.info_secure)
        binding.infoSecureDesc.text = getString(R.string.info_secure_desc)
        binding.infoUptime.text = getString(R.string.info_uptime)
        binding.infoUptimeDesc.text = getString(R.string.info_uptime_desc)
        binding.infoQuality.text = getString(R.string.info_quality)
        binding.infoQualityDesc.text = getString(R.string.info_quality_desc)
        binding.infoSupport.text = getString(R.string.info_support)
        binding.infoSupportDesc.text = getString(R.string.info_support_desc)
    }

    private fun startClock() {
        updateDateTime()
        handler.post(updateTimeRunnable)
    }

    private fun updateDateTime() {
        if (_binding == null) return
        try {
            val now = Date()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
            binding.timeText.text = timeFormat.format(now)

            val dateFormat = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale("ar"))
            binding.dateText.text = dateFormat.format(now)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date/time", e)
        }
    }

    private fun loadContent() {
        lifecycleScope.launch {
            try {
                val channelsResult = repository.getLiveStreams(null)
                if (channelsResult.isSuccess) {
                    allChannels = channelsResult.getOrDefault(emptyList())
                }
                updateUI()
                val moviesResult = repository.getMovies(null)
                if (moviesResult.isSuccess) {
                    allMovies = moviesResult.getOrDefault(emptyList()).map { it.toChannel() }
                }
                updateUI()
                val seriesResult = repository.getSeries(null)
                if (seriesResult.isSuccess) {
                    allSeries = seriesResult.getOrDefault(emptyList()).map { it.toChannel() }
                }
                updateUI()
            } catch (e: Exception) {
                Log.e(TAG, "Content loading failed", e)
            }
        }
    }

    private fun updateUI() {
        if (_binding == null) return
        val categories = mutableListOf<CategoryItem>()
        categories.add(CategoryItem("كل القنوات", allChannels.size, R.drawable.ic_live_tv, "#E53935", "live"))
        categories.add(CategoryItem("الرياضة", allChannels.count { it.name.contains("sport", true) }, R.drawable.ic_sports, "#2196F3", "live"))
        categories.add(CategoryItem("الأخبار", allChannels.count { it.name.contains("news", true) }, R.drawable.ic_news, "#4CAF50", "live"))
        categories.add(CategoryItem("الأفلام", allMovies.size, R.drawable.ic_movies, "#E53935", "movie"))
        categories.add(CategoryItem("الأطفال", allChannels.count { it.name.contains("kids", true) }, R.drawable.ic_kids, "#FF9800", "live"))
        categories.add(CategoryItem("الوثائقيات", allChannels.count { it.name.contains("doc", true) }, R.drawable.ic_documentary, "#00BCD4", "live"))
        updateCategories(categories)

        // لا نعرض محتوى في "متابعة المشاهدة" إلا عندما يتوفر سجل مشاهدة حقيقي.
        updateContinueWatching(emptyList())
    }

    private fun openMain(mode: String) {
        val intent = when (mode) {
            MainActivity.MODE_MOVIES -> Intent(this, MoviesActivity::class.java)
            MainActivity.MODE_SERIES -> Intent(this, SeriesActivity::class.java)
            else -> Intent(this, MainActivity::class.java).apply { putExtra(MainActivity.EXTRA_MODE, mode) }
        }
        startActivity(intent)
    }

    private fun playContent(item: ContinueWatchingItem) {
        val url = item.channel.directSource ?: item.channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isNullOrBlank()) return
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url).putExtra("CHANNEL_NAME", item.channel.name).putExtra("STREAM_TYPE", item.channel.streamType))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

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

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        _binding = null
    }
}

data class SidebarItem(val title: String, val iconRes: Int, val isSelected: Boolean = false, val action: () -> Unit)
data class CategoryItem(val name: String, val count: Int, val iconRes: Int, val colorHex: String, val type: String)
data class ContinueWatchingItem(val id: String, val title: String, val subtitle: String, val imageUrl: String?, val progress: Int, val channel: Channel)
