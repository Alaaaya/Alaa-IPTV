package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class MediaRepository(
    private val prefs: AppPreferences,
    private val context: Context
) {

    companion object {
        private const val TAG = "MediaRepository"
        private const val CACHE_DURATION = 5 * 60 * 1000L // 5 دقائق
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .cache(Cache(File(context.cacheDir, "http_cache"), 25L * 1024 * 1024))
            .build()
    }

    // ================= CACHE =================

    private var cachedChannels: List<Channel>? = null
    private var cachedChannelsTime = 0L
    private var cachedChannelsCategoryId: String? = null

    private var cachedCategories: List<Category>? = null
    private var cachedCategoriesTime = 0L

    private var cachedMovies: List<Movie>? = null
    private var cachedMoviesTime = 0L

    private var cachedSeries: List<Series>? = null
    private var cachedSeriesTime = 0L

    // ================= HELPERS =================

    private suspend fun request(url: String): String =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Requesting IPTV endpoint")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}")
                }
                response.body?.string() ?: throw IOException("Empty response body")
            }
        }

    private suspend fun readPlaylist(url: String): String =
        withContext(Dispatchers.IO) {
            if (url.startsWith("file:", ignoreCase = true)) {
                val file = File(java.net.URI(url))
                if (!file.exists() || !file.isFile) throw IOException("ملف القائمة غير موجود")
                file.readText()
            } else {
                request(url)
            }
        }

    private fun normalizeHost(host: String): String {
        var h = host.trim().substringBefore("?").removeSuffix("/")
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }
        return h.removeSuffix("/player_api.php").removeSuffix("/")
    }

    fun isM3U(url: String = prefs.serverUrl): Boolean {
        val lower = url.lowercase()
        return lower.contains("get.php") ||
               lower.contains(".m3u") ||
               lower.contains("m3u_plus") ||
               lower.contains("type=m3u")
    }

    private fun buildApiUrl(action: String, extra: String = ""): String {
        val base = normalizeHost(prefs.serverUrl)
        val username = encodeQueryParameter(prefs.username)
        val password = encodeQueryParameter(prefs.password)
        return "$base/player_api.php?username=$username&password=$password&action=$action$extra"
    }

    private fun encodeQueryParameter(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    /**
     * Verifies an Xtream account before credentials are persisted. For M3U playlists,
     * it verifies that the remote resource is reachable and contains a valid playlist header.
     */
    suspend fun validateLogin(serverUrl: String, username: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (isM3U(serverUrl)) {
                    val playlist = readPlaylist(serverUrl)
                    if (playlist.contains("#EXTM3U")) {
                        Result.success(Unit)
                    } else {
                        Result.failure(IOException("الرابط لا يحتوي على قائمة M3U صالحة"))
                    }
                } else {
                    if (username.isBlank() || password.isBlank()) {
                        return@withContext Result.failure(IllegalArgumentException("اسم المستخدم وكلمة المرور مطلوبان"))
                    }
                    val base = normalizeHost(serverUrl)
                    val url = "$base/player_api.php?username=${encodeQueryParameter(username)}&password=${encodeQueryParameter(password)}"
                    val response = JSONObject(request(url))
                    val userInfo = response.optJSONObject("user_info")
                    val authenticated = userInfo?.optInt("auth", 0) == 1
                    if (authenticated) {
                        Result.success(Unit)
                    } else {
                        val message = userInfo?.optString("message")?.takeIf { it.isNotBlank() }
                            ?: "تعذر التحقق من بيانات الدخول"
                        Result.failure(IOException(message))
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Login validation failed", error)
                Result.failure(error)
            }
        }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            val requestedCategory = categoryId?.takeIf { it != "all" }
            val effectiveCategory = requestedCategory ?: getLiveCategories()
                .getOrDefault(emptyList())
                .firstOrNull()
                ?.categoryId

            // استخدم الكاش إذا كان حديثاً
            cachedChannels?.let { cached ->
                if (System.currentTimeMillis() - cachedChannelsTime < CACHE_DURATION &&
                    cachedChannelsCategoryId == effectiveCategory) {
                    return@withContext Result.success(cached)
                }
            }

            try {
                if (isM3U()) {
                    return@withContext loadM3U(prefs.serverUrl)
                }

                val categoryExtra = effectiveCategory?.let { "&category_id=${encodeQueryParameter(it)}" }.orEmpty()
                val url = buildApiUrl("get_live_streams", categoryExtra)
                val body = request(url)
                val array = JSONArray(body)
                val base = normalizeHost(prefs.serverUrl)

                val channels = mutableListOf<Channel>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("stream_id")
                    val direct = "$base/live/${prefs.username}/${prefs.password}/$id.m3u8"

                    channels.add(
                        Channel(
                            streamId = id,
                            num = obj.optString("num"),
                            name = obj.optString("name"),
                            streamType = "live",
                            streamIcon = obj.optString("stream_icon"),
                            epgChannelId = obj.optString("epg_channel_id"),
                            added = obj.optString("added"),
                            categoryId = obj.optString("category_id"),
                            categoryName = null,
                            customSid = null,
                            tvArchive = obj.optInt("tv_archive"),
                            directSource = direct,
                            tvArchiveDuration = obj.optInt("tv_archive_duration")
                        )
                    )
                }

                // حفظ في الكاش
                cachedChannels = channels
                cachedChannelsTime = System.currentTimeMillis()
                cachedChannelsCategoryId = effectiveCategory

                Result.success(channels)

            } catch (e: Exception) {
                Log.e(TAG, "getLiveStreams error", e)
                Result.failure(e)
            }
        }

    // ================= LIVE CATEGORIES =================

    suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {

            // كاش
            cachedCategories?.let { cached ->
                if (System.currentTimeMillis() - cachedCategoriesTime < CACHE_DURATION) {
                    return@withContext Result.success(cached)
                }
            }

            try {
                val url = buildApiUrl("get_live_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }

                cachedCategories = categories
                cachedCategoriesTime = System.currentTimeMillis()

                Result.success(categories)

            } catch (e: Exception) {
                Log.e(TAG, "getLiveCategories error", e)
                Result.failure(e)
            }
        }

    // ================= VOD CATEGORIES =================

    suspend fun getMovieCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val url = buildApiUrl("get_vod_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }
                Result.success(categories)
            } catch (e: Exception) {
                Log.e(TAG, "getMovieCategories error", e)
                Result.failure(e)
            }
        }

    // ================= SERIES CATEGORIES =================

    suspend fun getSeriesCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val url = buildApiUrl("get_series_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }
                Result.success(categories)
            } catch (e: Exception) {
                Log.e(TAG, "getSeriesCategories error", e)
                Result.failure(e)
            }
        }

    // ================= MOVIES (VOD) =================

    suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        withContext(Dispatchers.IO) {
            cachedMovies?.let { cached ->
                if (System.currentTimeMillis() - cachedMoviesTime < CACHE_DURATION) {
                    val filtered = if (categoryId != null && categoryId != "all") cached.filter { it.categoryId == categoryId } else cached
                    return@withContext Result.success(filtered)
                }
            }

            try {
                val requestedCategory = categoryId?.takeIf { it != "all" }
                val allResponse = if (requestedCategory == null) fetchMovies(null) else emptyList()
                val movies = when {
                    requestedCategory != null -> fetchMovies(requestedCategory)
                    allResponse.isNotEmpty() -> allResponse
                    else -> fetchFeaturedMovies()
                }
                cachedMovies = movies
                cachedMoviesTime = System.currentTimeMillis()
                Result.success(movies)
            } catch (e: Exception) {
                Log.e(TAG, "getMovies error", e)
                Result.failure(e)
            }
        }

    private suspend fun fetchMovies(categoryId: String?): List<Movie> {
        val extra = categoryId?.let { "&category_id=${encodeQueryParameter(it)}" }.orEmpty()
        val array = JSONArray(request(buildApiUrl("get_vod_streams", extra)))
        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            Movie(
                streamId = obj.optString("stream_id"), name = obj.optString("name"), streamIcon = obj.optString("stream_icon"),
                rating = obj.optString("rating"), year = obj.optString("year", ""), plot = obj.optString("plot", ""),
                cast = obj.optString("cast", ""), director = obj.optString("director", ""), genre = obj.optString("genre", ""),
                releaseDate = obj.optString("release_date", ""), durationSecs = obj.optString("duration_secs", ""),
                duration = obj.optString("duration", ""), categoryId = obj.optString("category_id"),
                containerExtension = obj.optString("container_extension", "mp4"), isFavorite = false
            )
        }
    }

    private suspend fun fetchFeaturedMovies(): List<Movie> {
        val categories = getMovieCategories().getOrDefault(emptyList())
        val featured = linkedMapOf<String, Movie>()
        for (category in categories) {
            if (featured.size >= 500) break
            fetchMovies(category.categoryId).forEach { movie ->
                if (featured.size < 500) featured.putIfAbsent(movie.streamId, movie)
            }
        }
        return featured.values.toList()
    }

    // ================= SERIES =================

    suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {
            cachedSeries?.let { cached ->
                if (System.currentTimeMillis() - cachedSeriesTime < CACHE_DURATION) {
                    val filtered = if (categoryId != null && categoryId != "all") cached.filter { it.categoryId == categoryId } else cached
                    return@withContext Result.success(filtered)
                }
            }

            try {
                val requestedCategory = categoryId?.takeIf { it != "all" }
                val series = if (requestedCategory == null) fetchFeaturedSeries() else fetchSeries(requestedCategory)
                cachedSeries = series
                cachedSeriesTime = System.currentTimeMillis()
                Result.success(series)
            } catch (e: Exception) {
                Log.e(TAG, "getSeries error", e)
                Result.failure(e)
            }
        }

    private suspend fun fetchSeries(categoryId: String): List<Series> {
        val array = JSONArray(request(buildApiUrl("get_series", "&category_id=${encodeQueryParameter(categoryId)}")))
        return List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            Series(
                seriesId = obj.optString("series_id"), name = obj.optString("name"), cover = obj.optString("cover"),
                plot = obj.optString("plot"), cast = obj.optString("cast", ""), director = obj.optString("director", ""),
                genre = obj.optString("genre"), releaseDate = obj.optString("release_date", ""),
                categoryId = obj.optString("category_id"), rating = obj.optString("rating"), isFavorite = false
            )
        }
    }

    private suspend fun fetchFeaturedSeries(): List<Series> {
        val categories = getSeriesCategories().getOrDefault(emptyList())
        val featured = linkedMapOf<String, Series>()
        for (category in categories) {
            if (featured.size >= 500) break
            fetchSeries(category.categoryId).forEach { series ->
                if (featured.size < 500) featured.putIfAbsent(series.seriesId, series)
            }
        }
        return featured.values.toList()
    }

    // ================= SERIES EPISODES =================

    suspend fun getSeriesEpisodes(seriesId: String): Result<List<Episode>> =
        withContext(Dispatchers.IO) {
            try {
                if (isM3U()) {
                    return@withContext Result.failure(IOException("قوائم M3U لا تحتوي على بيانات حلقات المسلسلات"))
                }
                val url = buildApiUrl("get_series_info", "&series_id=${encodeQueryParameter(seriesId)}")
                val body = request(url)
                val episodesObject = JSONObject(body).optJSONObject("episodes")
                    ?: return@withContext Result.success(emptyList())

                val episodes = mutableListOf<Episode>()
                val seasons = episodesObject.keys()
                while (seasons.hasNext()) {
                    val seasonKey = seasons.next()
                    val seasonNumber = seasonKey.toIntOrNull() ?: 0
                    val array = episodesObject.optJSONArray(seasonKey) ?: continue
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val info = item.optJSONObject("info")
                        episodes += Episode(
                            id = item.optString("id"),
                            episodeNum = item.optInt("episode_num", index + 1),
                            title = item.optString("title").ifBlank { "الحلقة ${index + 1}" },
                            containerExtension = item.optString("container_extension", "mp4"),
                            info = EpisodeInfo(
                                plot = info?.optString("plot")?.takeIf { it.isNotBlank() },
                                duration = info?.optString("duration")?.takeIf { it.isNotBlank() },
                                rating = info?.optString("rating")?.takeIf { it.isNotBlank() }
                            ),
                            seasonNumber = seasonNumber
                        )
                    }
                }
                Result.success(episodes.sortedWith(compareBy<Episode> { it.seasonNumber }.thenBy { it.episodeNum }))
            } catch (error: Exception) {
                Log.e(TAG, "getSeriesEpisodes error", error)
                Result.failure(error)
            }
        }

    // ================= LOAD M3U =================

    suspend fun loadM3U(url: String): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            cachedChannels?.let { cached ->
                if (System.currentTimeMillis() - cachedChannelsTime < CACHE_DURATION) {
                    return@withContext Result.success(cached)
                }
            }

            try {
                val body = readPlaylist(url)
                val channels = mutableListOf<Channel>()
                var name = "Channel"
                var logo: String? = null
                var group: String? = null

                for (line in body.lineSequence()) {
                    val l = line.trim()

                    if (l.startsWith("#EXTINF")) {
                        name = Regex(",(.+)$").find(l)?.groupValues?.get(1)?.trim() ?: "Channel"
                        logo = Regex("""tvg-logo="([^"]*?)""").find(l)?.groupValues?.get(1)
                        group = Regex("""group-title="([^"]*?)""").find(l)?.groupValues?.get(1)
                    }

                    if (l.startsWith("http://") || l.startsWith("https://") || l.startsWith("rtmp")) {
                        channels.add(
                            Channel(
                                streamId = l.hashCode().toString(),
                                num = "",
                                name = name,
                                streamType = "live",
                                streamIcon = logo,
                                epgChannelId = null,
                                added = null,
                                categoryId = group ?: "Uncategorized",
                                categoryName = group ?: "Uncategorized",
                                customSid = null,
                                tvArchive = 0,
                                directSource = l,
                                tvArchiveDuration = 0
                            )
                        )
                        name = "Channel"
                        logo = null
                        group = null
                    }
                }

                cachedChannels = channels
                cachedChannelsTime = System.currentTimeMillis()

                Result.success(channels)

            } catch (e: Exception) {
                Log.e(TAG, "loadM3U error", e)
                Result.failure(e)
            }
        }

    // ================= CLEAR CACHE =================

    fun clearCache() {
        cachedChannels = null
        cachedChannelsTime = 0L
        cachedChannelsCategoryId = null
        cachedCategories = null
        cachedCategoriesTime = 0L
        cachedMovies = null
        cachedMoviesTime = 0L
        cachedSeries = null
        cachedSeriesTime = 0L
    }
}
