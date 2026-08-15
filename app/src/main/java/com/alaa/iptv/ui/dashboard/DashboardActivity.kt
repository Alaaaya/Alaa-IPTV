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
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.data.repository.SubscriptionSessionExpiredException
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.ui.main.MainActivity
import com.alaa.iptv.ui.main.MoviesActivity
import com.alaa.iptv.ui.main.SeriesActivity
import com.alaa.iptv.ui.library.LibraryActivity
import com.alaa.iptv.ui.library.SearchActivity
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.login.LoginActivity
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
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
        DisplayTheme.applyDashboard(binding, prefs)

        setupSidebar()
        setupHeroBanner()
        setupCategories()
        setupContinueWatching()
        setupBottomInfo()
        checkUpdates()
        startClock()
        refreshControlPlane(force = true) {
            binding.root.post { loadContent() }
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

    override fun onResume() {
        super.onResume()
        refreshControlPlane()
    }

    private fun refreshControlPlane(force: Boolean = false, onAccessGranted: (() -> Unit)? = null) {
        lifecycleScope.launch {
            val accessGranted = ControlPlaneActivityGuard.refreshAndEnforce(this@DashboardActivity, prefs, force)
            if (accessGranted && _binding != null) {
                setupSidebar()
                updateUI()
                onAccessGranted?.invoke()
            }
        }
    }

    private fun setupSidebar() {
        val standardItems = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, true) { /* Home */ },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, false) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem(getString(R.string.menu_movies), R.drawable.ic_movies, false) { openMain(MainActivity.MODE_MOVIES) },
            SidebarItem(getString(R.string.menu_series), R.drawable.ic_series, false) { openMain(MainActivity.MODE_SERIES) },
            SidebarItem(getString(R.string.menu_favorites), R.drawable.ic_favorite, false) { openMain(MainActivity.MODE_FAVORITES) },
            SidebarItem(getString(R.string.menu_recent), R.drawable.ic_recent, false) {
                if (prefs.isFeatureEnabled(FeatureCatalog.WATCHLIST) || prefs.isFeatureEnabled(FeatureCatalog.WATCH_HISTORY) || prefs.isFeatureEnabled(FeatureCatalog.RECENT_CHANNELS)) {
                    startActivity(Intent(this, LibraryActivity::class.java))
                } else showToast("فعّل ميزات المكتبة من الإعدادات أولاً")
            },
            SidebarItem(getString(R.string.menu_categories), R.drawable.ic_categories, false) {
                if (prefs.isFeatureEnabled(FeatureCatalog.GLOBAL_SEARCH)) startActivity(Intent(this, SearchActivity::class.java))
                else showToast("فعّل البحث الشامل من الإعدادات أولاً")
            },
            SidebarItem(getString(R.string.menu_server), R.drawable.ic_server, false) {
                if (prefs.isFeatureEnabled(FeatureCatalog.CONTENT_RELOAD)) reloadContent()
                else startActivity(Intent(this, SettingsActivity::class.java))
            },
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { startActivity(Intent(this, SettingsActivity::class.java)) }
        )
        val items = if (prefs.isFeatureEnabled(FeatureCatalog.SIMPLE_MODE)) {
            standardItems.filter { item ->
                item.title in setOf(
                    getString(R.string.menu_home),
                    getString(R.string.menu_live),
                    getString(R.string.menu_movies),
                    getString(R.string.menu_series),
                    getString(R.string.menu_favorites),
                    getString(R.string.menu_settings)
                )
            }
        } else standardItems

        val adapter = SidebarAdapter(items, prefs.displayTheme) { item ->
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

    private fun updateHeroBanner() {
        if (_binding == null) return
        val featured = allMovies.firstOrNull { !it.streamIcon.isNullOrBlank() }
            ?: allSeries.firstOrNull { !it.streamIcon.isNullOrBlank() }
            ?: return

        val typeLabel = if (featured.streamType.equals("series", ignoreCase = true)) "مسلسل" else "فيلم"
        binding.heroTitle.text = featured.name
        binding.heroSubtitle.text = "$typeLabel متاح الآن في مكتبتك"
        binding.heroWatchNow.text = "عرض $typeLabel"
        binding.heroImage.contentDescription = "صورة $typeLabel ${featured.name}"
        binding.heroWatchNow.setOnClickListener {
            openMain(
                if (featured.streamType.equals("series", ignoreCase = true)) {
                    MainActivity.MODE_SERIES
                } else {
                    MainActivity.MODE_MOVIES
                }
            )
        }

        Glide.with(this)
            .load(featured.streamIcon)
            .placeholder(R.drawable.bg_hero_sports)
            .error(R.drawable.bg_hero_sports)
            .centerCrop()
            .into(binding.heroImage)
    }

    private fun setupCategories() {
        binding.categoriesHeaderTitle.text = getString(R.string.browse_categories)
        binding.categoriesViewAll.text = getString(R.string.view_all)
        updateCategories(emptyList())
    }

    private fun updateCategories(categories: List<CategoryItem>) {
        if (_binding == null) return
        val adapter = CategoryCardAdapter(categories, prefs.displayTheme) { category ->
            when (category.type) {
                "live" -> openMain(MainActivity.MODE_LIVE)
                "movie" -> openMain(MainActivity.MODE_MOVIES)
                "series" -> openMain(MainActivity.MODE_SERIES)
                else -> openMain(MainActivity.MODE_LIVE)
            }
        }
        binding.categoriesRecyclerView.apply {
            val gridStyle = DisplayTheme.dashboardCategoryGrid(prefs.displayTheme)
            layoutManager = GridLayoutManager(this@DashboardActivity, gridStyle.spanCount, gridStyle.orientation, false)
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

    private fun reloadContent() {
        refreshControlPlane(force = true) {
            allChannels = emptyList()
            allMovies = emptyList()
            allSeries = emptyList()
            updateUI()
            loadContent(showFeedback = true)
        }
    }

    private fun loadContent(showFeedback: Boolean = false) {
        lifecycleScope.launch {
            try {
                setContentLoadState("جاري تحديث المحتوى…", visible = true)
                var lastFailure: Throwable? = null
                val channelsResult = repository.getLiveStreams(null)
                if (channelsResult.isSuccess) {
                    allChannels = channelsResult.getOrDefault(emptyList())
                } else lastFailure = channelsResult.exceptionOrNull()
                updateUI()
                val moviesResult = repository.getMovies(null)
                if (moviesResult.isSuccess) {
                    allMovies = moviesResult.getOrDefault(emptyList()).map { it.toChannel() }
                } else lastFailure = moviesResult.exceptionOrNull()
                updateUI()
                val seriesResult = repository.getSeries(null)
                if (seriesResult.isSuccess) {
                    allSeries = seriesResult.getOrDefault(emptyList()).map { it.toChannel() }
                } else lastFailure = seriesResult.exceptionOrNull()
                updateUI()
                if (lastFailure is SubscriptionSessionExpiredException && prefs.isFeatureEnabled(FeatureCatalog.SESSION_RECOVERY)) {
                    showToast("انتهت جلسة الاشتراك. أدخل بياناتك من جديد.")
                    startActivity(Intent(this@DashboardActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else if (lastFailure != null && prefs.isFeatureEnabled(FeatureCatalog.RECOVERY_ACTIONS)) {
                    val reference = if (prefs.isFeatureEnabled(FeatureCatalog.SAFE_ERROR_LOG)) {
                        prefs.addSafeDiagnostic("dashboard-content", lastFailure)
                    } else ""
                    val referenceText = if (reference.isBlank() || !prefs.isFeatureEnabled(FeatureCatalog.CONNECTION_REFERENCE)) "" else " ($reference)"
                    showToast("تعذر تحديث بعض المحتوى. تحقق من الاتصال وحاول مجدداً$referenceText")
                } else if (showFeedback) {
                    showToast("تم تحديث المحتوى.")
                }
                val isEmpty = allChannels.isEmpty() && allMovies.isEmpty() && allSeries.isEmpty()
                setContentLoadState(
                    if (isEmpty && prefs.isFeatureEnabled(FeatureCatalog.SMART_EMPTY_STATES)) {
                        "لا يوجد محتوى معروض حالياً. تحقق من اتصال الخادم ثم استخدم إعادة التحميل من القائمة."
                    } else "",
                    visible = isEmpty && prefs.isFeatureEnabled(FeatureCatalog.SMART_EMPTY_STATES)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Content loading failed", e)
                val reference = if (prefs.isFeatureEnabled(FeatureCatalog.SAFE_ERROR_LOG)) prefs.addSafeDiagnostic("dashboard-content", e) else ""
                val referenceText = if (reference.isBlank() || !prefs.isFeatureEnabled(FeatureCatalog.CONNECTION_REFERENCE)) "" else " ($reference)"
                showToast("تعذر تحديث المحتوى. حاول لاحقاً$referenceText")
                setContentLoadState("تعذر جلب المحتوى. استخدم إعادة التحميل بعد التحقق من الاتصال.", visible = true)
            }
        }
    }

    private fun setContentLoadState(message: String, visible: Boolean) {
        if (_binding == null) return
        binding.dashboardLoadState.text = message
        binding.dashboardLoadState.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updateUI() {
        if (_binding == null) return
        val categories = mutableListOf<CategoryItem>()
        categories.add(CategoryItem("كل القنوات", allChannels.size, R.drawable.ic_live_tv, CategoryVisuals.backgroundFor("live"), "#E53935", "live"))
        categories.add(CategoryItem("الرياضة", allChannels.count { it.name.contains("sport", true) }, R.drawable.ic_sports, CategoryVisuals.backgroundFor("sports"), "#2196F3", "live"))
        categories.add(CategoryItem("الأخبار", allChannels.count { it.name.contains("news", true) }, R.drawable.ic_news, CategoryVisuals.backgroundFor("news"), "#4CAF50", "live"))
        categories.add(CategoryItem("الأفلام", allMovies.size, R.drawable.ic_movies, CategoryVisuals.backgroundFor("movies"), "#E53935", "movie"))
        categories.add(CategoryItem("المسلسلات", allSeries.size, R.drawable.ic_series, CategoryVisuals.backgroundFor("series"), "#8B5CF6", "series"))
        categories.add(CategoryItem("الأطفال", allChannels.count { it.name.contains("kids", true) }, R.drawable.ic_kids, CategoryVisuals.backgroundFor("kids"), "#FF9800", "live"))
        categories.add(CategoryItem("الوثائقيات", allChannels.count { it.name.contains("doc", true) }, R.drawable.ic_documentary, CategoryVisuals.backgroundFor("documentary"), "#00BCD4", "live"))
        categories.add(CategoryItem("الموسيقى", allChannels.count { it.name.contains("music", true) }, R.drawable.ic_music, CategoryVisuals.backgroundFor("music"), "#EC4899", "live"))
        val homeTypes = listOf("live", "sports", "news", "movie", "series", "kids", "documentary", "music")
        val visibleTypes = if (prefs.isFeatureEnabled(FeatureCatalog.HOME_CUSTOMIZATION) || prefs.isFeatureEnabled(FeatureCatalog.CATEGORY_ORDER)) {
            prefs.getHomeCategoryTypes(homeTypes)
        } else homeTypes
        val categoryType = mapOf(
            "كل القنوات" to "live", "الرياضة" to "sports", "الأخبار" to "news", "الأفلام" to "movie",
            "المسلسلات" to "series", "الأطفال" to "kids", "الوثائقيات" to "documentary", "الموسيقى" to "music"
        )
        val remotelyVisibleTypes = visibleTypes.filterNot { prefs.isHomeCategoryRemotelyHidden(it) }
        updateCategories(categories.filter { categoryType[it.name] in remotelyVisibleTypes }.sortedBy { remotelyVisibleTypes.indexOf(categoryType[it.name]) })
        updateHeroBanner()

        val history = if (prefs.isFeatureEnabled(FeatureCatalog.WATCH_HISTORY)) prefs.getPlaybackHistory() else emptyList()
        updateContinueWatching(history.map { entry ->
            ContinueWatchingItem(
                id = entry.id,
                title = entry.title,
                subtitle = if (entry.positionMs > 0) "استئناف المشاهدة" else "شاهد مجدداً",
                imageUrl = entry.imageUrl,
                progress = if (entry.durationMs > 0) ((entry.positionMs * 100) / entry.durationMs).toInt().coerceIn(0, 100) else 0,
                channel = Channel(
                    streamId = entry.id,
                    num = "",
                    name = entry.title,
                    streamType = entry.streamType,
                    streamIcon = entry.imageUrl,
                    epgChannelId = null,
                    added = null,
                    categoryId = null,
                    categoryName = null,
                    customSid = null,
                    tvArchive = 0,
                    directSource = entry.streamUrl,
                    tvArchiveDuration = 0
                ),
                resumePositionMs = entry.positionMs
            )
        })
    }

    private fun openMain(mode: String) {
        if (prefs.isDeviceAccessBlocked()) {
            refreshControlPlane()
            return
        }
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
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", item.channel.name)
            .putExtra("STREAM_TYPE", item.channel.streamType)
            .putExtra(PlayerActivity.EXTRA_RESUME_POSITION_MS, item.resumePositionMs))
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
data class CategoryItem(
    val name: String,
    val count: Int,
    val iconRes: Int,
    val backgroundRes: Int,
    val colorHex: String,
    val type: String
)
data class ContinueWatchingItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String?,
    val progress: Int,
    val channel: Channel,
    val resumePositionMs: Long = 0L
)
