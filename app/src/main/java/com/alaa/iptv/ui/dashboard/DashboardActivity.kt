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
import com.alaa.iptv.ui.player.PlaybackUrlPolicy
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.login.LoginActivity
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import com.alaa.iptv.ui.common.DirectSectionNavigationPolicy
import com.alaa.iptv.utils.UpdateChecker
import com.bumptech.glide.Glide
import kotlinx.coroutines.CancellationException
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
    private var smartStartHandled = false

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
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)
        DisplayTheme.applyDashboard(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) window.attributes = window.attributes.apply { screenBrightness = 0.82f }

        setupSidebar()
        setupHeroBanner()
        setupCategories()
        setupContinueWatching()
        setupBottomInfo()
        checkUpdates()
        startClock()
        refreshControlPlane(force = true) {
            if (!applySmartStart()) binding.root.post { loadContent() }
        }
    }

    private fun checkUpdates() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.IN_APP_UPDATES)) return
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
        val neonIptv = DisplayTheme.isNeonIptv(prefs.displayTheme)
        val standardItems = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, !neonIptv) { /* Home */ },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, neonIptv) { openMain(MainActivity.MODE_LIVE) },
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
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { openSettings() }
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
        val navigationItems = if (neonIptv) {
            val neonOrder = listOf(
                getString(R.string.menu_live),
                getString(R.string.menu_movies),
                getString(R.string.menu_series),
                getString(R.string.menu_favorites),
                getString(R.string.menu_recent),
                getString(R.string.menu_categories),
                getString(R.string.menu_settings),
                getString(R.string.menu_server)
            )
            items.filterNot { it.title == getString(R.string.menu_home) }
                .sortedBy { neonOrder.indexOf(it.title).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
        } else items

        val adapter = SidebarAdapter(navigationItems, prefs.displayTheme) { item ->
            item.action.invoke()
        }

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = adapter
        }

        if (neonIptv) {
            binding.sidebarFooterIcon.visibility = View.GONE
            binding.securityTitle.textSize = 24f
            binding.securityDesc.textSize = 11f
            binding.securityTitle.text = "--:--"
            binding.securityDesc.text = ""
        } else {
            binding.sidebarFooterIcon.visibility = View.VISIBLE
            binding.securityTitle.textSize = 13f
            binding.securityDesc.textSize = 10f
            binding.securityTitle.text = getString(R.string.security_title)
            binding.securityDesc.text = getString(R.string.security_desc)
        }
    }

    private fun setupHeroBanner() {
        showFallbackHero()
    }

    private fun showFallbackHero() {
        binding.heroTitle.text = getString(R.string.hero_title)
        binding.heroSubtitle.text = getString(R.string.hero_desc)
        binding.heroWatchNow.text = getString(R.string.watch_now)
        binding.heroPosterCard.visibility = View.GONE
        binding.heroImage.contentDescription = null
        binding.heroWatchNow.setOnClickListener {
            openMain(MainActivity.MODE_LIVE)
        }

        val fallbackHero = if (DisplayTheme.isNeonIptv(prefs.displayTheme)) {
            R.drawable.alaa_neon_iptv_hero
        } else {
            R.drawable.bg_hero_sports
        }
        Glide.with(this)
            .load(fallbackHero)
            .placeholder(R.drawable.bg_dark_pattern)
            .into(binding.heroImage)
    }

    private fun updateHeroBanner() {
        if (_binding == null) return
        val featured = allMovies.firstOrNull { !it.streamIcon.isNullOrBlank() }
            ?: allSeries.firstOrNull { !it.streamIcon.isNullOrBlank() }
            ?: run {
                showFallbackHero()
                return
            }

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

        if (prefs.isFeatureEnabled(FeatureCatalog.DATA_SAVER) || prefs.isFeatureEnabled(FeatureCatalog.LOW_BANDWIDTH_POSTERS)) {
            binding.heroPosterCard.visibility = View.GONE
            binding.heroImage.setImageResource(
                if (DisplayTheme.isNeonIptv(prefs.displayTheme)) R.drawable.alaa_neon_iptv_hero else R.drawable.bg_hero_sports
            )
        } else {
            binding.heroPosterCard.visibility = View.VISIBLE
            Glide.with(this)
                .load(featured.streamIcon)
                .placeholder(R.drawable.bg_hero_sports)
                .error(R.drawable.bg_hero_sports)
                .centerCrop()
                .into(binding.heroImage)
            Glide.with(this)
                .load(featured.streamIcon)
                .placeholder(R.drawable.bg_hero_sports)
                .error(R.drawable.bg_hero_sports)
                .fitCenter()
                .into(binding.heroPosterImage)
        }
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
            val density = resources.displayMetrics.density
            val rowHeightDp = DisplayTheme.dashboardCategoryRailHeightDp(prefs.displayTheme)
            layoutParams = layoutParams.apply { height = (rowHeightDp * density).toInt() }
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            setItemViewCacheSize(10)
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

        val continueWatchingAdapter = ContinueWatchingAdapter(items, prefs.displayTheme, ::playContent)
        binding.continueWatchingRecyclerView.apply {
            val density = resources.displayMetrics.density
            layoutParams = layoutParams.apply {
                height = (continueWatchingAdapter.railHeightDp() * density).toInt()
            }
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            clipToPadding = false
            setPadding(paddingLeft, (8 * density).toInt(), paddingRight, (8 * density).toInt())
            adapter = continueWatchingAdapter
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
            val time = timeFormat.format(now)
            binding.timeText.text = time

            val dateFormat = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale("ar"))
            val date = dateFormat.format(now)
            binding.dateText.text = date
            if (DisplayTheme.isNeonIptv(prefs.displayTheme)) {
                binding.securityTitle.text = time
                binding.securityDesc.text = date
            }
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
                var hasCatalogCategories = false

                // الصفحة الرئيسية لا تحمل عشرات الآلاف من العناصر. نجلب الفئات فقط،
                // ثم عينة محدودة من فئة أفلام ومسلسلات لاختيار بانر حقيقي.
                val liveCategoriesResult = repository.getLiveCategories()
                if (liveCategoriesResult.isSuccess) {
                    hasCatalogCategories = hasCatalogCategories || liveCategoriesResult.getOrDefault(emptyList()).isNotEmpty()
                } else lastFailure = liveCategoriesResult.exceptionOrNull()

                val movieCategoriesResult = repository.getMovieCategories()
                val movieCategories = movieCategoriesResult.getOrDefault(emptyList())
                if (movieCategoriesResult.isSuccess) {
                    hasCatalogCategories = hasCatalogCategories || movieCategories.isNotEmpty()
                    movieCategories.firstOrNull()?.let { category ->
                        val moviesResult = repository.getMovies(category.categoryId, page = 0)
                        if (moviesResult.isSuccess) {
                            allMovies = moviesResult.getOrDefault(emptyList()).map { it.toChannel() }
                        } else lastFailure = moviesResult.exceptionOrNull()
                    }
                } else lastFailure = movieCategoriesResult.exceptionOrNull()

                val seriesCategoriesResult = repository.getSeriesCategories()
                val seriesCategories = seriesCategoriesResult.getOrDefault(emptyList())
                if (seriesCategoriesResult.isSuccess) {
                    hasCatalogCategories = hasCatalogCategories || seriesCategories.isNotEmpty()
                    seriesCategories.firstOrNull()?.let { category ->
                        val seriesResult = repository.getSeries(category.categoryId, page = 0)
                        if (seriesResult.isSuccess) {
                            allSeries = seriesResult.getOrDefault(emptyList()).map { it.toChannel() }
                        } else lastFailure = seriesResult.exceptionOrNull()
                    }
                } else lastFailure = seriesCategoriesResult.exceptionOrNull()
                updateUI()
                if (lastFailure is SubscriptionSessionExpiredException) {
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
                val isEmpty = !hasCatalogCategories
                setContentLoadState(
                    if (isEmpty && prefs.isFeatureEnabled(FeatureCatalog.SMART_EMPTY_STATES)) {
                        "لا يوجد محتوى معروض حالياً. تحقق من اتصال الخادم ثم استخدم إعادة التحميل من القائمة."
                    } else "",
                    visible = isEmpty && prefs.isFeatureEnabled(FeatureCatalog.SMART_EMPTY_STATES)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
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
        // لا نعرض أرقاماً مضللة من عينة الصفحة الرئيسية؛ الأعداد الحقيقية تظهر بعد اختيار الفئة.
        categories.add(CategoryItem("كل القنوات", 0, R.drawable.ic_live_tv, CategoryVisuals.backgroundFor("live"), "#E53935", "live"))
        categories.add(CategoryItem("الرياضة", 0, R.drawable.ic_sports, CategoryVisuals.backgroundFor("sports"), "#2196F3", "live"))
        categories.add(CategoryItem("الأخبار", 0, R.drawable.ic_news, CategoryVisuals.backgroundFor("news"), "#4CAF50", "live"))
        categories.add(CategoryItem("الأفلام", 0, R.drawable.ic_movies, CategoryVisuals.backgroundFor("movies"), "#E53935", "movie"))
        categories.add(CategoryItem("المسلسلات", 0, R.drawable.ic_series, CategoryVisuals.backgroundFor("series"), "#8B5CF6", "series"))
        categories.add(CategoryItem("الأطفال", 0, R.drawable.ic_kids, CategoryVisuals.backgroundFor("kids"), "#FF9800", "live"))
        categories.add(CategoryItem("الوثائقيات", 0, R.drawable.ic_documentary, CategoryVisuals.backgroundFor("documentary"), "#00BCD4", "live"))
        categories.add(CategoryItem("الموسيقى", 0, R.drawable.ic_music, CategoryVisuals.backgroundFor("music"), "#EC4899", "live"))
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
        prefs.lastVisitedSection = mode
        val intent = when (mode) {
            MainActivity.MODE_MOVIES -> Intent(this, MoviesActivity::class.java)
            MainActivity.MODE_SERIES -> Intent(this, SeriesActivity::class.java)
            else -> Intent(this, MainActivity::class.java).apply { putExtra(MainActivity.EXTRA_MODE, mode) }
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        if (DirectSectionNavigationPolicy.shouldRetireOriginAfterOpen()) finish()
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java).putExtra(SettingsActivity.EXTRA_RETURN_TO_DASHBOARD, true))
        overridePendingTransition(0, 0)
        if (DirectSectionNavigationPolicy.shouldRetireOriginAfterOpen()) finish()
    }

    /**
     * لا يُنفذ إلا مرة واحدة لكل فتح للصفحة الرئيسية، ولا يجلب أي فهرس أو بيانات جديدة.
     * أولوية البدء السريع للقناة الأخيرة؛ ثم آخر قسم محفوظ إذا اختاره المستخدم.
     */
    private fun applySmartStart(): Boolean {
        if (smartStartHandled) return false
        smartStartHandled = true

        if (prefs.isFeatureEnabled(FeatureCatalog.QUICK_START)) {
            val recent = prefs.getRecentChannels().firstOrNull { it.streamType.equals("live", ignoreCase = true) }
            if (recent != null && recent.streamUrl.isNotBlank()) {
                startActivity(Intent(this, PlayerActivity::class.java)
                    .putExtra("STREAM_URL", recent.streamUrl)
                    .putExtra("CHANNEL_NAME", recent.title)
                    .putExtra("STREAM_TYPE", recent.streamType))
                return true
            }
        }

        if (prefs.isFeatureEnabled(FeatureCatalog.START_SCREEN)) {
            when (prefs.lastVisitedSection) {
                MainActivity.MODE_LIVE,
                MainActivity.MODE_MOVIES,
                MainActivity.MODE_SERIES,
                MainActivity.MODE_FAVORITES -> {
                    openMain(prefs.lastVisitedSection)
                    return true
                }
            }
        }
        return false
    }

    private fun playContent(item: ContinueWatchingItem) {
        val rawUrl = item.channel.directSource
            ?: item.channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        val url = PlaybackUrlPolicy.normalizedHttpUrlOrNull(rawUrl)
        if (url == null) {
            Log.w(TAG, "Skipping playback because the continue-watching item has no valid HTTP(S) URL")
            showToast("تعذر تشغيل هذا المحتوى: رابط البث غير صالح")
            return
        }

        runCatching {
            startActivity(Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", item.channel.name)
                .putExtra("STREAM_TYPE", item.channel.streamType)
                .putExtra(PlayerActivity.EXTRA_RESUME_POSITION_MS, item.resumePositionMs))
        }.onFailure { error ->
            Log.e(TAG, "Unable to open PlayerActivity", error)
            showToast("تعذر فتح المشغل. حاول مجدداً")
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        val isDown = keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN
        val isUp = keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP
        if (isDown || isUp) {
            val zone = when {
                binding.heroWatchNow.hasFocus() -> DashboardFocusPolicy.Zone.HERO
                binding.categoriesRecyclerView.hasFocus() -> DashboardFocusPolicy.Zone.CATEGORIES
                binding.continueWatchingRecyclerView.hasFocus() -> DashboardFocusPolicy.Zone.CONTINUE_WATCHING
                else -> DashboardFocusPolicy.Zone.OTHER
            }
            val destination = DashboardFocusPolicy.verticalDestination(
                zone = zone,
                moveDown = isDown,
                hasContinueWatching = (binding.continueWatchingRecyclerView.adapter?.itemCount ?: 0) > 0
            )
            when (destination) {
                DashboardFocusPolicy.Destination.HERO -> {
                    binding.heroWatchNow.requestFocus()
                    binding.dashboardScrollView.smoothScrollTo(0, 0)
                    return true
                }
                DashboardFocusPolicy.Destination.CATEGORIES -> {
                    focusFirstCategory()
                    return true
                }
                DashboardFocusPolicy.Destination.CONTINUE_WATCHING -> {
                    focusFirstContinueWatching()
                    return true
                }
                DashboardFocusPolicy.Destination.BLOCK -> return true
                DashboardFocusPolicy.Destination.DEFAULT -> Unit
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun focusFirstCategory() {
        binding.categoriesRecyclerView.post {
            binding.categoriesRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                ?: binding.categoriesRecyclerView.requestFocus()
            binding.dashboardScrollView.smoothScrollTo(0, binding.categoriesRecyclerView.top)
        }
    }

    private fun focusFirstContinueWatching() {
        if ((binding.continueWatchingRecyclerView.adapter?.itemCount ?: 0) <= 0) return
        binding.continueWatchingRecyclerView.post {
            binding.continueWatchingRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                ?: binding.continueWatchingRecyclerView.requestFocus()
            binding.dashboardScrollView.smoothScrollTo(0, binding.continueWatchingRecyclerView.top)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, if (prefs.isFeatureEnabled(FeatureCatalog.LONG_NOTIFICATIONS)) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    private fun com.alaa.iptv.data.models.Movie.toChannel() = Channel(
        streamId = streamId, num = streamId, name = name, streamType = "movie",
        streamIcon = streamIcon, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null,
        containerExtension = containerExtension, isFavorite = isFavorite
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
