package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import kotlinx.coroutines.launch

class SeriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var seriesList: List<Series> = emptyList()
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

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
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { }
        )

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SeriesActivity)
            adapter = SidebarAdapter(items) { it.action.invoke() }
        }
    }

    private fun setupSeriesGrid() {
        binding.seriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@SeriesActivity, 5)
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
        AlertDialog.Builder(this)
            .setTitle("فئات المسلسلات")
            .setItems(categories.map { it.categoryName }.toTypedArray()) { _, index ->
                selectCategory(categories[index])
            }
            .show()
    }

    private fun selectCategory(category: Category) {
        prefs.lastSeriesCategoryId = category.categoryId
        binding.seriesCategorySelector.text = category.categoryName
        loadSeries(category.categoryId)
    }

    private fun loadSeries(categoryId: String) {
        lifecycleScope.launch {
            try {
                val result = repository.getSeries(categoryId)
                if (result.isSuccess) {
                    seriesList = result.getOrDefault(emptyList())
                    binding.seriesCount.text = "${seriesList.size} مسلسل"
                    binding.seriesRecyclerView.adapter = SeriesAdapter(seriesList) { series ->
                        openSeriesDetails(series)
                    }
                }
            } catch (e: Exception) {
                Log.e("SeriesActivity", "Error loading series", e)
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
