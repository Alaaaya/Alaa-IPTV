package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    companion object {
        private const val TAG = "MediaRepository"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // ================= CACHE =================

    private var cachedLiveChannels: List<Channel>? = null
    private var cachedLiveCategories: List<Category>? = null

    fun forceRefresh() {
        // استدعاء يدوي لمسح الكاش (يمكن استدعاؤه من ViewModel أو UI)
        cachedLiveChannels = null
        cachedLiveCategories = null
    }

    private fun clearCache() {
        cachedLiveChannels = null
        cachedLiveCategories = null
    }

    // ================= AUTH =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                prefs.serverUrl = serverUrl.trim().removeSuffix("/")
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true
                clearCache()
                XtreamAuthResponse(null, null)
            }
        }

    // ================= HTTP helper with retry =================

    private suspend fun executeRequestWithRetry(request: Request, maxAttempts: Int = 3): String =
        withContext(Dispatchers.IO) {
            var attempt = 0
            var lastEx: Exception? = null

            while (attempt < maxAttempts) {
                try {
                    val response = client.newCall(request).execute()
                    val code = response.code
                    val body = response.body?.string() ?: ""

                    if (!response.isSuccessful) {
                        val snippet = if (body.length > 500) body.substring(0, 500) + "..." else body
                        val msg = "HTTP $code: $snippet"
                        Log.e(TAG, "Request failed: $msg | URL=${request.url}")
                        throw Exception(msg)
                    }

                    return@withContext body
                } catch (ex: Exception) {
                    lastEx = ex
                    attempt++
                    Log.w(TAG, "Request attempt $attempt failed: ${ex.message}")
                    // backoff بسيط
                    try {
                        Thread.sleep((attempt * 500).toLong())
                    } catch (_: InterruptedException) { /* ignore */ }
                }
            }

            val err = Exception("Request failed after $maxAttempts attempts. Last error: ${lastEx?.message}")
            Log.e(TAG, err.message ?: "Unknown error")
            throw err
        }

    // ================== JSON LIVE (Xtream Codes) ==================

    private suspend fun loadJsonLiveCategories(): List<Category> =
        withContext(Dispatchers.IO) {
            val url =
                "${prefs.serverUrl}/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_categories"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTV-Client")
                .header("Accept", "application/json")
                .build()

            val body = executeRequestWithRetry(request)
            val arr = JSONArray(body)

            val list = mutableListOf<Category>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("category_id", obj.optString("category_id", ""))
                val name = obj.optString("category_name", "Other")
                list.add(
                    Category(
                        categoryId = id,
                        categoryName = name,
                        parentId = 0
                    )
                )
            }
            Log.i(TAG, "Loaded JSON categories: ${list.size}")
            list
        }

    private suspend fun loadJsonLiveStreams(): List<Channel> =
        withContext(Dispatchers.IO) {
            val url =
                "${prefs.serverUrl}/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_streams"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTV-Client")
                .header("Accept", "application/json")
                .build()

            val body = executeRequestWithRetry(request)
            val arr = JSONArray(body)

            val list = mutableListOf<Channel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)

                val streamId = obj.optString("stream_id", obj.optString("stream_id", ""))
                val num = obj.optString("num", "")
                val name = obj.optString("name", "Channel")
                val streamType = obj.optString("stream_type", "live")
                val icon = obj.optString("stream_icon", null)
                val categoryId = obj.optString("category_id", "")
                val categoryName = obj.optString("category_name", null) ?: categoryId

                val directSource = buildLiveStreamUrl(streamId)

                list.add(
                    Channel(
                        streamId = streamId,
                        num = num,
                        name = name,
                        streamType = streamType,
                        streamIcon = icon,
                        epgChannelId = null,
                        added = null,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        customSid = null,
                        tvArchive = 0,
                        directSource = directSource,
                        tvArchiveDuration = 0
                    )
                )
            }
            Log.i(TAG, "Loaded JSON streams: ${list.size}")
            list
        }

    private fun buildLiveStreamUrl(streamId: String): String {
        // صيغة ستريم Xtream شائعة؛ عدّل إذا سيرفرك يحتاج صيغة مختلفة
        // مثال بديل: "${prefs.serverUrl}/live/${prefs.username}/${prefs.password}/${streamId}.m3u8"
        return "${prefs.serverUrl}/${prefs.username}/${prefs.password}/$streamId"
    }

    // ================= M3U FALLBACK =================

    private suspend fun loadM3U(): List<Channel> =
        withContext(Dispatchers.IO) {

            val url =
                "${prefs.serverUrl}/get.php?username=${prefs.username}&password=${prefs.password}&type=m3u_plus&output=ts"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "VLC/3.0.18 LibVLC/3.0.18")
                .header("Accept", "*/*")
                .build()

            val body = executeRequestWithRetry(request)
            parseM3U(body)
        }

    private fun parseM3U(content: String): List<Channel> {

        val channels = mutableListOf<Channel>()
        val lines = content.split("\n")

        var name = ""
        var logo: String? = null
        var group: String? = null

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF", ignoreCase = true)) {

                val nameMatch = Regex(",(.*)").find(line)
                name = nameMatch?.groupValues?.get(1)?.trim() ?: "Channel"

                val logoMatch = Regex("""tvg-logo="([^"]*)"""").find(line)
                logo = logoMatch?.groupValues?.get(1)

                val groupMatch = Regex("""group-title="([^"]*)"""").find(line)
                group = groupMatch?.groupValues?.get(1)

                if (group.isNullOrBlank()) group = "Other"
            }

            // next non-empty line that looks like URL
            if (line.startsWith("http", ignoreCase = true) || line.startsWith("rtmp", ignoreCase = true)) {
                channels.add(
                    Channel(
                        streamId = line.hashCode().toString(),
                        num = "",
                        name = name,
                        streamType = "live",
                        streamIcon = logo,
                        epgChannelId = null,
                        added = null,
                        categoryId = group,
                        categoryName = group,
                        customSid = null,
                        tvArchive = 0,
                        directSource = line.trim(),
                        tvArchiveDuration = 0
                    )
                )
            }
        }

        Log.i(TAG, "Parsed M3U channels: ${channels.size}")
        return channels
    }

    // ================= LIVE (UNIFIED) =================

    private suspend fun ensureLiveDataLoaded() {
        if (cachedLiveChannels != null && cachedLiveCategories != null) return

        // جرّب JSON أولاً (أفضل)
        try {
            val categories = loadJsonLiveCategories()
            val channels = loadJsonLiveStreams()

            cachedLiveCategories = categories
            cachedLiveChannels = channels
            Log.i(TAG, "Using JSON endpoints for live data")
            return
        } catch (ex: Exception) {
            Log.w(TAG, "JSON endpoints failed, falling back to M3U: ${ex.message}")
        }

        // فولباك إلى M3U
        val m3uChannels = loadM3U()
        cachedLiveChannels = m3uChannels

        val cats = m3uChannels.mapNotNull { it.categoryName }
            .distinct()
            .map { cat ->
                Category(
                    categoryId = cat,
                    categoryName = cat,
                    parentId = 0
                )
            }

        cachedLiveCategories = cats
        Log.i(TAG, "Using M3U fallback for live data")
    }

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureLiveDataLoaded()
                cachedLiveCategories ?: emptyList()
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                ensureLiveDataLoaded()
                val channels = cachedLiveChannels ?: emptyList()
                if (categoryId.isNullOrEmpty()) channels
                else channels.filter {
                    it.categoryId == categoryId || it.categoryName == categoryId
                }
            }
        }

    // ================= REQUIRED OVERRIDES =================

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()
    override suspend fun getFavorites() = emptyList<String>()
    override suspend fun isFavorite(itemId: String) = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}

    override suspend fun addFavorite(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ) = Result.success(Unit)

    override suspend fun removeFavorite(contentId: String) = Result.success(Unit)
    override suspend fun getFavoritesWithDetails() = Result.success(emptyList<FavoriteItem>())
    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents() = emptyList<Recent>()
    override suspend fun addRecentView(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
    override suspend fun getRecentViews() = Result.success(emptyList<RecentItem>())
    override suspend fun getMovieCategories() = Result.success(emptyList<Category>())
    override suspend fun getMovies(categoryId: String?) = Result.success(emptyList<Movie>())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()
    override suspend fun getSeriesCategories() = Result.success(emptyList<Category>())
    override suspend fun getSeries(categoryId: String?) = Result.success(emptyList<Series>())
    override suspend fun getSeriesFromCache(seriesId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
    override suspend fun getEpgForChannel(channelId: String) = emptyList<EpgProgram>()
    override suspend fun getEpgForChannelInTimeRange(channelId: String, startTime: Long, endTime: Long) = emptyList<EpgProgram>()
    override suspend fun getCurrentProgram(channelId: String) = null
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int) = emptyList<EpgProgram>()
    override suspend fun cacheEpgPrograms(programs: List<EpgProgram>) {}
    override suspend fun cleanupOldEpgData(cutoffTime: Long) {}
    override suspend fun searchChannels(query: String, categoryId: String?) = emptyList<Channel>()
    override suspend fun searchMovies(query: String, categoryId: String?) = emptyList<Movie>()
    override suspend fun searchSeries(query: String, categoryId: String?) = emptyList<Series>()
    override suspend fun searchMoviesByGenre(genre: String) = emptyList<Movie>()
    override suspend fun searchSeriesByGenre(genre: String) = emptyList<Series>()
    override suspend fun syncAllData() = Result.success(Unit)
    override suspend fun syncLiveTV() = Result.success(Unit)
    override suspend fun syncMovies() = Result.success(Unit)
    override suspend fun syncSeries() = Result.success(Unit)
    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {}
    override suspend fun getChannelsOrdered(categoryId: String?) = emptyList<Channel>()
    override suspend fun loadM3UPlaylist(m3uContent: String) = Result.success(emptyList<Channel>())
    override suspend fun loadM3UPlaylistFromUrl(url: String) = Result.success(emptyList<Channel>())
    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
