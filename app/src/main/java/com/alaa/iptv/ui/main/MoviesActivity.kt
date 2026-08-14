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
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMoviesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.theme.DisplayTheme
import kotlinx.coroutines.launch

class MoviesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoviesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var movies: List<Movie> = emptyList()
    private var categories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        DisplayTheme.applyMovies(binding, prefs)

        setupSidebar()
        setupMoviesGrid()
        binding.movieCategorySelector.setOnClickListener { showCategorySelector() }
        loadCategories()
    }

    private fun setupSidebar() {
        val items = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, false) { finish() },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, false) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem(getString(R.string.menu_movies), R.drawable.ic_movies, true) { /* Already here */ },
            SidebarItem(getString(R.string.menu_series), R.drawable.ic_series, false) { openMain(MainActivity.MODE_SERIES) },
            SidebarItem(getString(R.string.menu_favorites), R.drawable.ic_favorite, false) { },
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { startActivity(Intent(this, SettingsActivity::class.java)) }
        )

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoviesActivity)
            adapter = SidebarAdapter(items, prefs.isHotPlayerTheme) { it.action.invoke() }
        }
    }

    private fun setupMoviesGrid() {
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MoviesActivity, 5) // 5 columns as in image
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val result = repository.getMovieCategories()
            result.onSuccess { loadedCategories ->
                categories = loadedCategories
                val selected = categories.firstOrNull { it.categoryId == prefs.lastMovieCategoryId }
                    ?: categories.firstOrNull()
                if (selected == null) {
                    binding.moviesCount.text = "لا توجد فئات أفلام"
                } else {
                    selectCategory(selected)
                }
            }.onFailure { error ->
                Log.e(TAG, "Unable to load movie categories", error)
                binding.moviesCount.text = "تعذر تحميل فئات الأفلام"
            }
        }
    }

    private fun showCategorySelector() {
        if (categories.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("فئات الأفلام")
            .setItems(categories.map { it.categoryName }.toTypedArray()) { _, index ->
                selectCategory(categories[index])
            }
            .show()
    }

    private fun selectCategory(category: Category) {
        prefs.lastMovieCategoryId = category.categoryId
        binding.movieCategorySelector.text = category.categoryName
        loadMovies(category.categoryId)
    }

    private fun loadMovies(categoryId: String) {
        lifecycleScope.launch {
            runCatching { repository.getMovies(categoryId) }
                .onSuccess { result ->
                    result.onSuccess { loadedMovies ->
                        movies = loadedMovies
                        binding.moviesCount.text = "${movies.size} فيلم"
                        binding.moviesRecyclerView.adapter = MovieAdapter(movies, prefs.isHotPlayerTheme, ::playMovie)
                    }.onFailure { error ->
                        Log.e(TAG, "Unable to load movies", error)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Unable to request movies", error)
                }
        }
    }

    private fun playMovie(movie: Movie) {
        val url = movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", movie.name)
            .putExtra("STREAM_TYPE", "movie"))
    }

    private fun openMain(mode: String) {
        val intent = if (mode == MainActivity.MODE_SERIES) {
            Intent(this, SeriesActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_MODE, mode)
            }
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "MoviesActivity"
    }
}
