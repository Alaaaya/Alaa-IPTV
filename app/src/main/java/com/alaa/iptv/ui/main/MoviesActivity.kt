package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMoviesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class MoviesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoviesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var movies: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        setupSidebar()
        setupMoviesGrid()
        loadMovies()
    }

    private fun setupSidebar() {
        val items = listOf(
            SidebarItem(getString(R.string.menu_home), R.drawable.ic_logo, false) { finish() },
            SidebarItem(getString(R.string.menu_live), R.drawable.ic_live_tv, false) { openMain(MainActivity.MODE_LIVE) },
            SidebarItem(getString(R.string.menu_movies), R.drawable.ic_movies, true) { /* Already here */ },
            SidebarItem(getString(R.string.menu_series), R.drawable.ic_series, false) { openMain(MainActivity.MODE_SERIES) },
            SidebarItem(getString(R.string.menu_favorites), R.drawable.ic_favorite, false) { },
            SidebarItem(getString(R.string.menu_settings), R.drawable.ic_settings, false) { }
        )

        binding.sidebarRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoviesActivity)
            adapter = SidebarAdapter(items) { it.action.invoke() }
        }
    }

    private fun setupMoviesGrid() {
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MoviesActivity, 5) // 5 columns as in image
        }
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                val result = repository.getMovies(null)
                if (result.isSuccess) {
                    movies = result.getOrDefault(emptyList()).map { it.toChannel() }
                    binding.moviesCount.text = "${movies.size} فيلم"
                    // Use a dedicated MovieAdapter or update existing one
                    // For now, let's assume we'll create MovieAdapter
                    binding.moviesRecyclerView.adapter = MovieAdapter(movies) { movie ->
                        playMovie(movie)
                    }
                }
            } catch (e: Exception) {
                Log.e("MoviesActivity", "Error loading movies", e)
            }
        }
    }

    private fun playMovie(movie: Channel) {
        val url = movie.directSource ?: movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isNullOrBlank()) return
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", movie.name)
            .putExtra("STREAM_TYPE", "movie"))
    }

    private fun openMain(mode: String) {
        startActivity(Intent(this, MainActivity::class.java).apply { putExtra(MainActivity.EXTRA_MODE, mode) })
        finish()
    }

    private fun com.alaa.iptv.data.models.Movie.toChannel() = Channel(
        streamId = streamId, num = streamId, name = name, streamType = "movie",
        streamIcon = streamIcon, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )
}
