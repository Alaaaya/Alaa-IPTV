package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.preferences.MediaLibraryEntry
import com.alaa.iptv.databinding.ActivityMovieDetailsBinding
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.player.PlaybackUrlPolicy
import com.alaa.iptv.ui.settings.ContentShareActivity
import com.bumptech.glide.Glide

class MovieDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailsBinding
    private lateinit var prefs: AppPreferences
    private lateinit var movie: Movie

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(binding.root)
        movie = readMovieExtra() ?: run {
            finish()
            return
        }
        prefs = AppPreferences(this)
        bindMovie()
        binding.backButton.setOnClickListener { finish() }
        binding.playMovieButton.setOnClickListener { playMovie() }
        binding.watchLaterButton.setOnClickListener { toggleWatchLater() }
        binding.shareContentButton.visibility = if (prefs.isFeatureEnabled(FeatureCatalog.CONTENT_QR_SHARE)) View.VISIBLE else View.GONE
        binding.shareContentButton.setOnClickListener { shareForPhone() }
    }

    private fun bindMovie() {
        binding.movieTitle.text = movie.name
        binding.movieMeta.text = listOfNotNull(
            movie.year?.takeIf { it.isNotBlank() },
            movie.genre?.takeIf { it.isNotBlank() },
            movie.rating?.takeIf { it.isNotBlank() }?.let { "★ $it" },
            movie.duration?.takeIf { it.isNotBlank() }
        ).joinToString("  •  ")
        binding.movieDescription.text = movie.plot?.takeIf { it.isNotBlank() }
            ?: getString(R.string.no_description)
        Glide.with(this)
            .load(movie.streamIcon)
            .placeholder(R.drawable.bg_dark_pattern)
            .error(R.drawable.bg_dark_pattern)
            .into(binding.moviePoster)
        Glide.with(this)
            .load(movie.streamIcon)
            .placeholder(R.drawable.bg_dark_pattern)
            .error(R.drawable.bg_dark_pattern)
            .into(binding.movieBackdrop)
        updateWatchLaterLabel()
    }

    private fun playMovie() {
        val url = PlaybackUrlPolicy.normalizedHttpUrlOrNull(
            movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        )
        if (url == null) {
            Toast.makeText(this, "تعذر إنشاء رابط التشغيل لهذا الفيلم", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", movie.name)
            .putExtra("STREAM_TYPE", "movie"))
    }

    private fun toggleWatchLater() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.WATCHLIST)) {
            Toast.makeText(this, "فعّل المشاهدة لاحقاً من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        val added = prefs.toggleWatchlist(
            MediaLibraryEntry(
                id = movie.streamId,
                title = movie.name,
                streamUrl = movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password),
                streamType = "movie",
                imageUrl = movie.streamIcon
            )
        )
        Toast.makeText(this, if (added) "أُضيف إلى المشاهدة لاحقاً" else "أُزيل من المشاهدة لاحقاً", Toast.LENGTH_SHORT).show()
        updateWatchLaterLabel()
    }

    private fun shareForPhone() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.CONTENT_QR_SHARE)) {
            Toast.makeText(this, "فعّل مشاركة المحتوى عبر QR من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, ContentShareActivity::class.java)
            .putExtra(ContentShareActivity.EXTRA_CONTENT_TYPE, "movie")
            .putExtra(ContentShareActivity.EXTRA_CONTENT_KEY, movie.streamId)
            .putExtra(ContentShareActivity.EXTRA_TITLE, movie.name)
            .putExtra(ContentShareActivity.EXTRA_POSTER_URL, movie.streamIcon))
    }

    private fun updateWatchLaterLabel() {
        val saved = prefs.getWatchlist().any { it.id == movie.streamId && it.streamType == "movie" }
        binding.watchLaterButton.text = if (saved) "✓ محفوظ للمشاهدة لاحقاً" else "♡ المشاهدة لاحقاً"
    }

    private fun readMovieExtra(): Movie? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        intent.getParcelableExtra(EXTRA_MOVIE, Movie::class.java)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelableExtra(EXTRA_MOVIE)
    }

    companion object {
        const val EXTRA_MOVIE = "EXTRA_MOVIE"
    }
}
