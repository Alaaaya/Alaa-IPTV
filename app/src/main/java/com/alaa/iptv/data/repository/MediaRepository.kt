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

    private var cachedCategories: List<Category>? = null
    private var cachedCategoriesTime = 0L

    private var cachedMovies: List<Movie>? = null
    private var cachedMoviesTime = 0L

    private var cachedSeries: List<Series>? = null
    private var cachedSeriesTime = 0L

    // ================= HELPERS =================

    private suspend fun request(url: String): String =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Request: $url")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} for $url")
            }
            response.body?.string() ?: throw IOException("Empty response body")
        }

    private fun normalizeHost(host: String): String {
        var h = host.trim()
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }
        return h.removeSuffix("/")
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
        return "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=$action$extra"
    }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            // استخدم الكاش إذا كان حديثاً
            cachedChannels?.let { cached ->
                if (System.currentTimeMillis() - cachedChannelsTime < CACHE_DURATION) {
                    val filtered = if (categoryId != null && categoryId != "all")
                        cached.filter { it.categoryId == categoryId }
                    else cached
                    return@withContext Result.success(filtered)
                }
            }

            try {
                if (isM3U()) {
                    return@withContext loadM3U(prefs.serverUrl)
                }

                val url = buildApiUrl("get_live_streams")
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

                val filtered = if (categoryId != null && categoryId != "all")
                    channels.filter { it.categoryId == categoryId }
                else channels

                Result.success(filtered)

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
                    val filtered = if (categoryId != null && categoryId != "all")
                        cached.filter { it.categoryId == categoryId }
                    else cached
                    return@withContext Result.success(filtered)
                }
            }

            try {
                val url = buildApiUrl("get_vod_streams")
                val body = request(url)
                val array = JSONArray(body)

                val movies = mutableListOf<Movie>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    movies.add(
                        Movie(
                            streamId = obj.optString("stream_id"),
                            name = obj.optString("name"),
                            streamIcon = obj.optString("stream_icon"),
                            rating = obj.optString("rating"),
                            categoryId = obj.optString("category_id"),
                            containerExtension = obj.optString("container_extension", "mp4"),
                            isFavorite = false
                        )
                    )
                }

                cachedMovies = movies
                cachedMoviesTime = System.currentTimeMillis()

                val filtered = if (categoryId != null && categoryId != "all")
                    movies.filter { it.categoryId == categoryId }
                else movies

                Result.success(filtered)

            } catch (e: Exception) {
                Log.e(TAG, "getMovies error", e)
                Result.failure(e)
            }
        }

    // ================= SERIES =================

    suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {

            cachedSeries?.let { cached ->
                if (System.currentTimeMillis() - cachedSeriesTime < CACHE_DURATION) {
                    val filtered = if (categoryId != null && categoryId != "all")
                        cached.filter { it.categoryId == categoryId }
                    else cached
                    return@withContext Result.success(filtered)
                }
            }

            try {
                val url = buildApiUrl("get_series")
                val body = request(url)
                val array = JSONArray(body)

                val seriesList = mutableListOf<Series>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    seriesList.add(
                        Series(
                            seriesId = obj.optString("series_id"),
                            name = obj.optString("name"),
                            cover = obj.optString("cover"),
                            plot = obj.optString("plot"),
                            genre = obj.optString("genre"),
                            categoryId = obj.optString("category_id"),
                            rating = obj.optString("rating"),
                            isFavorite = false
                        )
                    )
                }

                cachedSeries = seriesList
                cachedSeriesTime = System.currentTimeMillis()

                val filtered = if (categoryId != null && categoryId != "all")
                    seriesList.filter { it.categoryId == categoryId }
                else seriesList

                Result.success(filtered)

            } catch (e: Exception) {
                Log.e(TAG, "getSeries error", e)
                Result.failure(e)
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
                val body = request(url)
                val channels = mutableListOf<Channel>()
                var name = "Channel"
                var logo: String? = null
                var group: String? = null

                for (line in body.lineSequence()) {
                    val l = line.trim()

                    if (l.startsWith("#EXTINF")) {
                        name = Regex(",(.+)$").find(l)?.groupValues?.get(1)?.trim() ?: "Channel"
                        logo = Regex("""tvg-logo="([^"]*?)"""").find(l)?.groupValues?.get(1)
                        group = Regex("""group-title="([^"]*?)"""").find(l)?.groupValues?.get(1)
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
        cachedCategories = null
        cachedCategoriesTime = 0L
        cachedMovies = null
        cachedMoviesTime = 0L
        cachedSeries = null
        cachedSeriesTime = 0L
    }
}
