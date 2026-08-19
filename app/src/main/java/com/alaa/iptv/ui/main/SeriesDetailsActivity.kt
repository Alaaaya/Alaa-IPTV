package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
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
    private var allEpisodes: List<Episode> = emptyList()
    private var selectedSeasonNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(binding.root)

        series = readSeriesExtra() ?: run {
            finish()
            return
        }
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)
        DisplayTheme.applySeriesDetails(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) {
            window.attributes = window.attributes.apply { screenBrightness = 0.82f }
        }

        bindSeries()
        binding.backButton.setOnClickListener { finish() }
        binding.episodesRecyclerView.layoutManager = GridLayoutManager(this, EpisodeAdapter.EPISODE_GRID_COLUMNS)
        binding.playFirstEpisodeButton.setOnClickListener {
            allEpisodes.firstOrNull { it.seasonNumber == selectedSeasonNumber }?.let { episode ->
                playEpisode(episode, allEpisodes.indexOf(episode))
            }
        }
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
        Glide.with(this)
            .load(series.cover)
            .placeholder(R.drawable.bg_dark_pattern)
            .error(R.drawable.bg_dark_pattern)
            .into(binding.seriesBackdrop)
    }

    private fun loadEpisodes() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                repository.getSeriesEpisodes(series.seriesId)
                    .onSuccess { episodes ->
                        if (episodes.isEmpty()) {
                            showError(getString(R.string.no_episodes))
                            return@onSuccess
                        }
                        allEpisodes = episodes
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
                        renderSeasons(completedTitles)
                    }
                    .onFailure { error ->
                        showError(error.message ?: getString(R.string.episodes_load_error))
                    }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun renderSeasons(completedTitles: Set<String>) {
        val seasons = SeriesSeasonPolicy.seasonsOf(allEpisodes)
        selectedSeasonNumber = seasons.firstOrNull() ?: 0
        binding.seasonTabsContainer.removeAllViews()
        seasons.forEach { season ->
            val episodeCount = allEpisodes.count { it.seasonNumber == season }
            binding.seasonTabsContainer.addView(android.widget.TextView(this).apply {
                text = "الموسم $season\n$episodeCount حلقات"
                gravity = android.view.Gravity.CENTER
                isFocusable = true
                isClickable = true
                setTextColor(android.graphics.Color.WHITE)
                textSize = 13f
                setPadding(dp(18), dp(8), dp(18), dp(8))
                layoutParams = LinearLayout.LayoutParams(dp(128), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    marginEnd = dp(10)
                }
                setOnClickListener {
                    selectedSeasonNumber = season
                    renderSeasonTabs()
                    renderEpisodesForSelectedSeason(completedTitles)
                }
            })
        }
        renderSeasonTabs()
        renderEpisodesForSelectedSeason(completedTitles)
    }

    private fun renderSeasonTabs() {
        val seasons = SeriesSeasonPolicy.seasonsOf(allEpisodes)
        for (index in 0 until binding.seasonTabsContainer.childCount) {
            val tab = binding.seasonTabsContainer.getChildAt(index) as? android.widget.TextView ?: continue
            tab.setBackgroundResource(
                if (seasons.getOrNull(index) == selectedSeasonNumber) {
                    R.drawable.bg_button_red
                } else {
                    R.drawable.bg_login_secondary_button
                }
            )
        }
    }

    private fun renderEpisodesForSelectedSeason(completedTitles: Set<String>) {
        val episodes = SeriesSeasonPolicy.episodesForSeason(allEpisodes, selectedSeasonNumber)
        binding.episodesTitle.text = "الموسم $selectedSeasonNumber  •  ${episodes.size} حلقات"
        val seasons = SeriesSeasonPolicy.seasonsOf(allEpisodes)
        binding.seasonCountFooter.text = "الموسم ${seasons.indexOf(selectedSeasonNumber) + 1} من ${seasons.size}"
        binding.episodesRecyclerView.adapter = EpisodeAdapter(
            episodes = episodes,
            onEpisodeClick = ::playEpisode,
            completedEpisodeTitles = completedTitles,
            seriesName = series.name,
            fallbackImageUrl = series.cover
        )
    }

    private fun playEpisode(episode: Episode, episodeIndex: Int) {
        val streamUrl = episode.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (streamUrl.isBlank()) {
            showError(getString(R.string.player_error))
            return
        }
        val fullIndex = allEpisodes.indexOfFirst { it.id == episode.id }.takeIf { it >= 0 } ?: episodeIndex
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", streamUrl)
                .putExtra("CHANNEL_NAME", "${series.name} - ${episode.title}")
                .putExtra("STREAM_TYPE", "series")
                .putExtra(PlayerActivity.EXTRA_EPISODE_INDEX, fullIndex)
        )
    }

    private fun setLoading(loading: Boolean) {
        binding.loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

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
