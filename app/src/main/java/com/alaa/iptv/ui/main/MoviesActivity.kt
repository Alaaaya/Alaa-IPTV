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
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.preferences.MediaLibraryEntry
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMoviesBinding
import com.alaa.iptv.ui.dashboard.SidebarAdapter
import com.alaa.iptv.ui.dashboard.SidebarItem
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.settings.SettingsActivity
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import kotlinx.coroutines.launch

class MoviesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoviesBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var movies: List<Movie> = emptyList()
    private var categories: List<Category> = emptyList()
    private var currentMoviePage = 0
    private var hasMoreMoviePages = false
    private var isLoadingMovies = false
    private lateinit var movieCategoryAdapter: LiveCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoviesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        DisplayTheme.applyMovies(binding, prefs)
        DisplayTheme.applyViewingPreferences(binding.root, prefs)
        binding.root.isSoundEffectsEnabled = prefs.isFeatureEnabled(FeatureCatalog.NAVIGATION_SOUNDS)
        if (prefs.isFeatureEnabled(FeatureCatalog.EYE_COMFORT)) window.attributes = window.attributes.apply { screenBrightness = 0.82f }
        prefs.lastVisitedSection = MainActivity.MODE_MOVIES

        setupSidebar()
        setupMoviesGrid()
        setupMovieCategoriesList()
        lifecycleScope.launch {
            if (ControlPlaneActivityGuard.refreshAndEnforce(this@MoviesActivity, prefs, force = true)) loadCategories()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            ControlPlaneActivityGuard.refreshAndEnforce(this@MoviesActivity, prefs)
        }
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
            adapter = SidebarAdapter(items, prefs.displayTheme) { it.action.invoke() }
        }
    }

    private fun setupMoviesGrid() {
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MoviesActivity, posterGridSpan())
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0 || isLoadingMovies || !hasMoreMoviePages) return
                    val grid = recyclerView.layoutManager as? GridLayoutManager ?: return
                    if (grid.findLastVisibleItemPosition() >= movies.size - 15) {
                        loadMovies(prefs.lastMovieCategoryId, currentMoviePage + 1, append = true)
                    }
                }
            })
        }
    }

    private fun setupMovieCategoriesList() {
        movieCategoryAdapter = LiveCategoryAdapter(prefs.displayTheme, ::selectCategory)
        binding.movieCategoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MoviesActivity)
            adapter = movieCategoryAdapter
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            val result = repository.getMovieCategories()
            val loadedCategories = result.getOrDefault(emptyList())
            categories = loadedCategories
            movieCategoryAdapter.submit(categories, prefs.lastMovieCategoryId)
            val selected = categories.firstOrNull { it.categoryId == prefs.lastMovieCategoryId }
                ?: categories.firstOrNull()
            if (selected == null) {
                binding.moviesCount.text = "لا توجد فئات أفلام متاحة من الاشتراك"
            } else {
                selectCategory(selected)
            }
        }
    }

    private fun selectCategory(category: Category) {
        prefs.lastMovieCategoryId = category.categoryId
        binding.moviesTitle.text = "الأفلام / ${category.categoryName}"
        movieCategoryAdapter.submit(categories, category.categoryId)
        currentMoviePage = 0
        hasMoreMoviePages = false
        loadMovies(category.categoryId, page = 0, append = false)
    }

    private fun loadMovies(categoryId: String, page: Int, append: Boolean) {
        if (isLoadingMovies) return
        isLoadingMovies = true
        lifecycleScope.launch {
            runCatching { repository.getMovies(categoryId, page) }
                .onSuccess { result ->
                    result.onSuccess { loadedMovies ->
                        movies = if (append) movies + loadedMovies else loadedMovies
                        currentMoviePage = page
                        hasMoreMoviePages = loadedMovies.size >= 120
                        binding.moviesCount.text = if (hasMoreMoviePages) {
                            "${movies.size} فيلم • تابع للأسفل لتحميل المزيد"
                        } else {
                            "${movies.size} فيلم"
                        }
                        binding.moviesRecyclerView.adapter = MovieAdapter(
                            movies,
                            prefs.displayTheme,
                            ::playMovie,
                            ::showMovieDetails,
                            ::showPreview,
                            isPosterDataSaver = usePosterDataSaver(),
                            gridSpan = posterGridSpan()
                        )
                    }.onFailure { error ->
                        Log.e(TAG, "Unable to load movies", error)
                        binding.moviesCount.text = "تعذر تحميل الأفلام. تحقق من الشبكة أو الفئة المختارة"
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Unable to request movies", error)
                    binding.moviesCount.text = "تعذر تحميل الأفلام. حاول مرة أخرى"
                }
            isLoadingMovies = false
        }
    }

    private fun playMovie(movie: Movie) {
        val url = movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isBlank()) {
            binding.moviesCount.text = "تعذر فتح هذا الفيلم لأن رابط التشغيل غير متاح"
            return
        }
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", movie.name)
            .putExtra("STREAM_TYPE", "movie"))
    }

    private fun toggleWatchlist(movie: Movie) {
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
    }

    private fun showMovieDetails(movie: Movie) {
        val details = listOfNotNull(
            movie.year?.takeIf { it.isNotBlank() }?.let { "السنة: $it" },
            movie.rating?.takeIf { it.isNotBlank() }?.let { "التقييم: ★ $it" },
            movie.genre?.takeIf { it.isNotBlank() }?.let { "النوع: $it" },
            movie.duration?.takeIf { it.isNotBlank() }?.let { "المدة: $it" },
            movie.director?.takeIf { it.isNotBlank() }?.let { "الإخراج: $it" },
            movie.cast?.takeIf { it.isNotBlank() }?.let { "الطاقم: $it" },
            movie.plot?.takeIf { it.isNotBlank() }
        ).joinToString("\n\n")
        AlertDialog.Builder(this)
            .setTitle(movie.name)
            .setMessage(details.ifBlank { "لا تتوفر تفاصيل إضافية لهذا الفيلم من المصدر." })
            .setPositiveButton("تشغيل") { _, _ -> playMovie(movie) }
            .setNeutralButton("المشاهدة لاحقاً") { _, _ -> toggleWatchlist(movie) }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && binding.moviesRecyclerView.hasFocus() && isAtLeadingMovieColumn()) {
            focusSelectedMovieCategory()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && binding.movieCategoriesRecyclerView.hasFocus()) {
            focusFirstMovie()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && prefs.isFeatureEnabled(FeatureCatalog.LIBRARY_FILTERS)) {
            showGenreFilter()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun focusFirstMovie() {
        binding.moviesRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
            ?: binding.moviesRecyclerView.requestFocus()
    }

    private fun focusSelectedMovieCategory() {
        val position = categories.indexOfFirst { it.categoryId == prefs.lastMovieCategoryId }
            .takeIf { it >= 0 } ?: 0
        binding.movieCategoriesRecyclerView.scrollToPosition(position)
        binding.movieCategoriesRecyclerView.post {
            binding.movieCategoriesRecyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
                ?: binding.movieCategoriesRecyclerView.requestFocus()
        }
    }

    private fun isAtLeadingMovieColumn(): Boolean {
        val focusedChild = binding.moviesRecyclerView.focusedChild ?: return false
        val position = binding.moviesRecyclerView.getChildAdapterPosition(focusedChild)
        return position != RecyclerView.NO_POSITION && position % posterGridSpan() == 0
    }

    private fun showGenreFilter() {
        val genres = movies.flatMap { it.genre.orEmpty().split(",") }
            .map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
        val options = listOf("الكل") + genres
        AlertDialog.Builder(this)
            .setTitle("فلترة الأفلام")
            .setItems(options.toTypedArray()) { _, index ->
                val filtered = if (index == 0) movies else movies.filter { it.genre.orEmpty().contains(options[index], true) }
                binding.moviesRecyclerView.adapter = MovieAdapter(
                    filtered,
                    prefs.displayTheme,
                    ::playMovie,
                    ::showMovieDetails,
                    ::showPreview,
                    isPosterDataSaver = usePosterDataSaver(),
                    gridSpan = posterGridSpan()
                )
                binding.moviesCount.text = "${filtered.size} فيلم"
            }
            .show()
    }

    private fun showPreview(movie: Movie) {
        if (!prefs.isFeatureEnabled(FeatureCatalog.FOCUS_PREVIEW)) return
        val meta = listOfNotNull(movie.year?.takeIf { it.isNotBlank() }, movie.rating?.takeIf { it.isNotBlank() }?.let { "★ $it" })
        binding.moviesCount.text = listOf(movie.name, meta.joinToString(" • ")).filter { it.isNotBlank() }.joinToString(" — ")
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

    private fun usePosterDataSaver(): Boolean =
        prefs.isFeatureEnabled(FeatureCatalog.DATA_SAVER) || prefs.isFeatureEnabled(FeatureCatalog.LOW_BANDWIDTH_POSTERS)

    private fun posterGridSpan(): Int = DisplayTheme.mediaGridSpan(
        prefs.displayTheme,
        prefs.isFeatureEnabled(FeatureCatalog.ROOMY_POSTERS)
    )

    companion object {
        private const val TAG = "MoviesActivity"
    }
}
