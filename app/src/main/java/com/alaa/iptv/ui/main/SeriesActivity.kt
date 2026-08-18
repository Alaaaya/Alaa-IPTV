package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.preferences.MediaLibraryEntry
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import kotlinx.coroutines.launch

class SeriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var seriesList: List<Series> = emptyList()
    private var categories: List<Category> = emptyList()
    private var currentSeriesPage = 0
    private var hasMoreSeriesPages = false
    private var isLoadingSeries = false
    private var selectedSeriesTotal = 0
    private lateinit var seriesCategoryAdapter: LiveCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)
        DisplayTheme.applySeries(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) window.attributes = window.attributes.apply { screenBrightness = 0.82f }
        prefs.lastVisitedSection = MainActivity.MODE_SERIES

        setupSidebar()
        setupSeriesGrid()
        setupSeriesCategoriesList()
        lifecycleScope.launch {
            if (ControlPlaneActivityGuard.refreshAndEnforce(this@SeriesActivity, prefs, force = true)) loadCategories()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            ControlPlaneActivityGuard.refreshAndEnforce(this@SeriesActivity, prefs)
        }
    }

    private fun setupSidebar() {
        val items = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, false) { finish() },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, false) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem(getString(R.string.menu_movies), R.drawable.ic_movies, false) { openMain(MainActivity.MODE_MOVIES) },
            SidebarItem(getString(R.string.menu_series), R.drawable.ic_series, true) { /* Already here */ },
            SidebarItem(getString(R.string.menu_favorites), R.drawable.ic_favorite, false) { },
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { startActivity(Intent(this, SettingsActivity::class.java)) }
        )

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SeriesActivity)
            adapter = SidebarAdapter(items, prefs.displayTheme) { it.action.invoke() }
        }
    }

    private fun setupSeriesGrid() {
        binding.seriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SeriesActivity, posterGridSpan())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0 || isLoadingSeries || !hasMoreSeriesPages) return
                    val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                    if (layoutManager.findLastVisibleItemPosition() >= seriesList.size - 15) {
                        loadSeries(prefs.lastSeriesCategoryId, currentSeriesPage + 1, append = true)
                    }
                }
            })
        }
    }

    private fun setupSeriesCategoriesList() {
        seriesCategoryAdapter = LiveCategoryAdapter(prefs.displayTheme, ::selectCategory)
        binding.seriesCategoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SeriesActivity)
            adapter = seriesCategoryAdapter
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val result = repository.getSeriesCategories()
            val loadedCategories = result.getOrDefault(emptyList())
            categories = loadedCategories
            seriesCategoryAdapter.submit(categories, prefs.lastSeriesCategoryId)
            val selected = categories.firstOrNull { it.categoryId == prefs.lastSeriesCategoryId }
                ?: categories.firstOrNull()
            if (selected == null) {
                binding.seriesCount.text = "لا توجد فئات مسلسلات متاحة من الاشتراك"
            } else {
                selectCategory(selected)
            }
        }
    }

    private fun selectCategory(category: Category) {
        prefs.lastSeriesCategoryId = category.categoryId
        binding.seriesTitle.text = "المسلسلات / ${category.categoryName}"
        binding.seriesCount.text = "جارٍ تحميل عدد المسلسلات…"
        seriesCategoryAdapter.submit(categories, category.categoryId)
        currentSeriesPage = 0
        hasMoreSeriesPages = false
        loadSeries(category.categoryId, page = 0, append = false)
    }

    private fun loadSeries(categoryId: String, page: Int, append: Boolean) {
        if (isLoadingSeries) return
        isLoadingSeries = true
        lifecycleScope.launch {
            try {
                val result = repository.getSeriesContentPage(categoryId, page)
                if (result.isSuccess) {
                    val contentPage = result.getOrThrow()
                    val loadedSeries = contentPage.items
                    seriesList = if (append) seriesList + loadedSeries else loadedSeries
                    currentSeriesPage = page
                    selectedSeriesTotal = contentPage.totalCount
                    hasMoreSeriesPages = contentPage.hasMore
                    updateSeriesCategoryCount(categoryId, selectedSeriesTotal)
                    binding.seriesCount.text = seriesCountText()
                    binding.seriesRecyclerView.adapter = SeriesAdapter(
                        seriesList,
                        prefs.displayTheme,
                        ::openSeriesDetails,
                        ::showSeriesSummary,
                        ::showPreview,
                        isPosterDataSaver = usePosterDataSaver(),
                        gridSpan = posterGridSpan()
                    )
                } else {
                    binding.seriesCount.text = "تعذر تحميل المسلسلات. تحقق من الشبكة أو الفئة المختارة"
                }
            } catch (e: Exception) {
                Log.e("SeriesActivity", "Error loading series", e)
                binding.seriesCount.text = "تعذر تحميل المسلسلات. حاول مرة أخرى"
            } finally {
                isLoadingSeries = false
            }
        }
    }

    private fun openSeriesDetails(series: Series) {
        startActivity(Intent(this, SeriesDetailsActivity::class.java).apply {
            putExtra(SeriesDetailsActivity.EXTRA_SERIES, series)
        })
    }

    private fun toggleWatchlist(series: Series) {
        if (!prefs.isFeatureEnabled(FeatureCatalog.WATCHLIST)) {
            Toast.makeText(this, "فعّل المشاهدة لاحقاً من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        val added = prefs.toggleWatchlist(
            MediaLibraryEntry(
                id = series.seriesId,
                title = series.name,
                streamUrl = "series://${series.seriesId}",
                streamType = "series",
                imageUrl = series.cover
            )
        )
        Toast.makeText(this, if (added) "أُضيف إلى المشاهدة لاحقاً" else "أُزيل من المشاهدة لاحقاً", Toast.LENGTH_SHORT).show()
    }

    private fun showSeriesSummary(series: Series) {
        val details = listOfNotNull(
            series.releaseDate?.takeIf { it.isNotBlank() }?.let { "تاريخ الإصدار: $it" },
            series.rating?.takeIf { it.isNotBlank() }?.let { "التقييم: ★ $it" },
            series.genre?.takeIf { it.isNotBlank() }?.let { "النوع: $it" },
            series.director?.takeIf { it.isNotBlank() }?.let { "الإخراج: $it" },
            series.cast?.takeIf { it.isNotBlank() }?.let { "الطاقم: $it" },
            series.plot?.takeIf { it.isNotBlank() }
        ).joinToString("\n\n")
        AlertDialog.Builder(this)
            .setTitle(series.name)
            .setMessage(details.ifBlank { "لا تتوفر تفاصيل إضافية لهذا المسلسل من المصدر." })
            .setPositiveButton("الحلقات والتفاصيل") { _, _ -> openSeriesDetails(series) }
            .setNeutralButton("المشاهدة لاحقاً") { _, _ -> toggleWatchlist(series) }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && binding.seriesRecyclerView.hasFocus() && isAtLeadingSeriesColumn()) {
            focusSelectedSeriesCategory()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && binding.seriesCategoriesRecyclerView.hasFocus()) {
            focusFirstSeries()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && prefs.isFeatureEnabled(FeatureCatalog.LIBRARY_FILTERS)) {
            showGenreFilter()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun focusFirstSeries() {
        binding.seriesRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
            ?: binding.seriesRecyclerView.requestFocus()
    }

    private fun focusSelectedSeriesCategory() {
        val position = categories.indexOfFirst { it.categoryId == prefs.lastSeriesCategoryId }
            .takeIf { it >= 0 } ?: 0
        binding.seriesCategoriesRecyclerView.scrollToPosition(position)
        binding.seriesCategoriesRecyclerView.post {
            binding.seriesCategoriesRecyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
                ?: binding.seriesCategoriesRecyclerView.requestFocus()
        }
    }

    private fun isAtLeadingSeriesColumn(): Boolean {
        val focusedChild = binding.seriesRecyclerView.focusedChild ?: return false
        val position = binding.seriesRecyclerView.getChildAdapterPosition(focusedChild)
        return position != RecyclerView.NO_POSITION && position % posterGridSpan() == 0
    }

    private fun showGenreFilter() {
        val genres = seriesList.flatMap { it.genre.orEmpty().split(",") }
            .map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
        val options = listOf("الكل") + genres
        AlertDialog.Builder(this)
            .setTitle("فلترة المسلسلات")
            .setItems(options.toTypedArray()) { _, index ->
                val filtered = if (index == 0) seriesList else seriesList.filter { it.genre.orEmpty().contains(options[index], true) }
                binding.seriesRecyclerView.adapter = SeriesAdapter(
                    filtered,
                    prefs.displayTheme,
                    ::openSeriesDetails,
                    ::showSeriesSummary,
                    ::showPreview,
                    isPosterDataSaver = usePosterDataSaver(),
                    gridSpan = posterGridSpan()
                )
                binding.seriesCount.text = "${filtered.size} نتيجة من أصل ${seriesList.size} مسلسل محمّل • ${seriesCountText()}"
            }
            .show()
    }

    private fun showPreview(series: Series) {
        if (!prefs.isFeatureEnabled(FeatureCatalog.FOCUS_PREVIEW)) return
        val meta = listOfNotNull(series.genre?.takeIf { it.isNotBlank() }, series.rating?.takeIf { it.isNotBlank() }?.let { "★ $it" })
        binding.seriesCount.text = listOf(series.name, meta.joinToString(" • "), seriesCountText())
            .filter { it.isNotBlank() }
            .joinToString(" — ")
    }

    private fun updateSeriesCategoryCount(categoryId: String, totalCount: Int) {
        categories = categories.map { category ->
            if (category.categoryId == categoryId) category.copy(channelCount = totalCount) else category
        }
        seriesCategoryAdapter.submit(categories, categoryId)
    }

    private fun seriesCountText(): String {
        val visible = seriesList.size
        val base = if (selectedSeriesTotal > 0) {
            "$visible من أصل $selectedSeriesTotal مسلسل"
        } else {
            "$visible مسلسل"
        }
        return if (hasMoreSeriesPages) "$base • تابع للأسفل لتحميل المزيد" else base
    }

    private fun openMain(mode: String) {
        val intent = if (mode == MainActivity.MODE_MOVIES) {
            Intent(this, MoviesActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java).apply { putExtra(MainActivity.EXTRA_MODE, mode) }
        }
        startActivity(intent)
        finish()
    }

    private fun usePosterDataSaver(): Boolean =
        prefs.isFeatureEnabled(FeatureCatalog.DATA_SAVER) || prefs.isFeatureEnabled(FeatureCatalog.LOW_BANDWIDTH_POSTERS)

    private fun posterGridSpan(): Int = DisplayTheme.mediaGridSpan(
        prefs.displayTheme,
        prefs.isFeatureEnabled(FeatureCatalog.ROOMY_POSTERS)
    )

}
