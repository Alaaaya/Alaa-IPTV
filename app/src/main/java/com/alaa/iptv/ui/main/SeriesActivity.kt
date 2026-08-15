package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
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
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.categories.CategoryPickerActivity
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.theme.DisplayTheme
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
    private val seriesCategoryPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val categoryId = result.data?.getStringExtra(CategoryPickerActivity.EXTRA_CATEGORY_ID) ?: return@registerForActivityResult
        categories.firstOrNull { it.categoryId == categoryId }?.let(::selectCategory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        DisplayTheme.applySeries(binding, prefs)

        setupSidebar()
        setupSeriesGrid()
        binding.seriesCategorySelector.setOnClickListener { showCategorySelector() }
        loadCategories()
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
            layoutManager = GridLayoutManager(this@SeriesActivity, DisplayTheme.mediaGridSpan(prefs.displayTheme))
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

    private fun loadCategories() {
        lifecycleScope.launch {
            val result = repository.getSeriesCategories()
            result.onSuccess { loadedCategories ->
                categories = loadedCategories
                val selected = categories.firstOrNull { it.categoryId == prefs.lastSeriesCategoryId }
                    ?: categories.firstOrNull()
                if (selected == null) {
                    binding.seriesCount.text = "لا توجد فئات مسلسلات"
                } else {
                    selectCategory(selected)
                }
            }.onFailure { error ->
                Log.e("SeriesActivity", "Unable to load series categories", error)
                binding.seriesCount.text = "تعذر تحميل فئات المسلسلات"
            }
        }
    }

    private fun showCategorySelector() {
        if (categories.isEmpty()) return
        seriesCategoryPicker.launch(
            CategoryPickerActivity.createIntent(this, "فئات المسلسلات", categories)
        )
    }

    private fun selectCategory(category: Category) {
        prefs.lastSeriesCategoryId = category.categoryId
        binding.seriesCategorySelector.text = category.categoryName
        currentSeriesPage = 0
        hasMoreSeriesPages = false
        loadSeries(category.categoryId, page = 0, append = false)
    }

    private fun loadSeries(categoryId: String, page: Int, append: Boolean) {
        if (isLoadingSeries) return
        isLoadingSeries = true
        lifecycleScope.launch {
            try {
                val result = repository.getSeries(categoryId, page)
                if (result.isSuccess) {
                    val loadedSeries = result.getOrDefault(emptyList())
                    seriesList = if (append) seriesList + loadedSeries else loadedSeries
                    currentSeriesPage = page
                    hasMoreSeriesPages = loadedSeries.size >= 120
                    binding.seriesCount.text = if (hasMoreSeriesPages) {
                        "${seriesList.size} مسلسل • تابع للأسفل لتحميل المزيد"
                    } else {
                        "${seriesList.size} مسلسل"
                    }
                    binding.seriesRecyclerView.adapter = SeriesAdapter(seriesList, prefs.displayTheme) { series ->
                        openSeriesDetails(series)
                    }
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

    private fun openMain(mode: String) {
        val intent = if (mode == MainActivity.MODE_MOVIES) {
            Intent(this, MoviesActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java).apply { putExtra(MainActivity.EXTRA_MODE, mode) }
        }
        startActivity(intent)
        finish()
    }

}
