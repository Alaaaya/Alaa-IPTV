package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Episode
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivitySeriesDetailsBinding
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.player.PlayableEpisode
import com.alaa.iptv.ui.player.PlayerEpisodeNavigator
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class SeriesDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeriesDetailsBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private lateinit var series: Series

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(binding.root)

        series = readSeriesExtra()
            ?: run {
                finish()
                return
            }
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)
        DisplayTheme.applySeriesDetails(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) window.attributes = window.attributes.apply { screenBrightness = 0.82f }

        bindSeries()
        binding.backButton.setOnClickListener { finish() }
        binding.episodesRecyclerView.layoutManager = GridLayoutManager(this, EpisodeAdapter.EPISODE_GRID_COLUMNS)
        loadEpisodes()
    }

    private fun bindSeries() {
        binding.seriesName.text = series.name
        binding.seriesDescription.text = series.plot?.takeIf { it.isNotBlank() }
            ?: getString(R.string.no_description)
        binding.seriesMeta.text = listOfNotNull(
            series.genre?.takeIf { it.isNotBlank() },
            series.releaseDate?.takeIf { it.isNotBlank() },
            series.rating?.takeIf { it.isNotBlank() }?.let { "★ $it" }
        ).joinToString("  •  ")
        Glide.with(this)
            .load(series.cover)
            .placeholder(R.drawable.bg_dark_pattern)
            .error(R.drawable.bg_dark_pattern)
            .into(binding.seriesPoster)
    }

    private fun loadEpisodes() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                repository.getSeriesEpisodes(series.seriesId)
                    .onSuccess { episodes ->
                        if (episodes.isEmpty()) {
                            showError(getString(R.string.no_episodes))
                        } else {
                            binding.episodesTitle.text = "الحلقات (${episodes.size})"
                            binding.errorText.visibility = View.GONE
                            PlayerEpisodeNavigator.setEpisodes(episodes.map { episode ->
                                PlayableEpisode(
                                    name = "${series.name} - ${episode.title}",
                                    streamUrl = episode.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
                                )
                            })
                            val completedTitles = prefs.getPlaybackHistory()
                                .filter { entry ->
                                    entry.streamType.equals("series", ignoreCase = true) &&
                                        entry.durationMs > 0L && entry.positionMs * 100L >= entry.durationMs * 90L
                                }
                                .map { it.title }
                                .toSet()
                            binding.episodesRecyclerView.adapter = EpisodeAdapter(
                                episodes = episodes,
                                onEpisodeClick = ::playEpisode,
                                completedEpisodeTitles = completedTitles,
                                seriesName = series.name
                            )
                        }
                    }
                    .onFailure { error ->
                        showError(error.message ?: getString(R.string.episodes_load_error))
                    }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun playEpisode(episode: Episode, episodeIndex: Int) {
        val streamUrl = episode.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (streamUrl.isBlank()) {
            showError(getString(R.string.player_error))
            return
        }
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", streamUrl)
                .putExtra("CHANNEL_NAME", "${series.name} - ${episode.title}")
                .putExtra("STREAM_TYPE", "series")
                .putExtra(PlayerActivity.EXTRA_EPISODE_INDEX, episodeIndex)
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun readSeriesExtra(): Series? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(EXTRA_SERIES, Series::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(EXTRA_SERIES)
    }

    companion object {
        const val EXTRA_SERIES = "EXTRA_SERIES"
    }
}
