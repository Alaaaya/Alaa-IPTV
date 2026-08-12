package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class SeriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var seriesList: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        setupSidebar()
        setupSeriesGrid()
        loadSeries()
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

    private fun loadSeries() {
        lifecycleScope.launch {
            try {
                val result = repository.getSeries(null)
                if (result.isSuccess) {
                    seriesList = result.getOrDefault(emptyList()).map { it.toChannel() }
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

    private fun openSeriesDetails(series: Channel) {
        // Implementation for series details / seasons
        showToast("فتح تفاصيل: ${series.name}")
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

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun com.alaa.iptv.data.models.Series.toChannel() = Channel(
        streamId = seriesId, num = seriesId, name = name, streamType = "series",
        streamIcon = cover, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )
}
