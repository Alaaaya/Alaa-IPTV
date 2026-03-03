package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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

    // ================= AUTH (M3U MODE) =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching {

                val cleanUrl = serverUrl.trim().removeSuffix("/")

                prefs.serverUrl = cleanUrl
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true

                Log.e(TAG, "M3U MODE LOGIN SUCCESS")

                XtreamAuthResponse(null, null)
            }
        }

    // ================= LOAD M3U =================

    private suspend fun loadM3U(): List<Channel> =
        withContext(Dispatchers.IO) {

            val m3uUrl =
                "${prefs.serverUrl}/get.php?username=${prefs.username}&password=${prefs.password}&type=m3u_plus&output=ts"

            Log.e(TAG, "Loading M3U: $m3uUrl")

            val request = Request.Builder()
                .url(m3uUrl)
                .header(
                    "User-Agent",
                    "Dalvik/2.1.0 (Linux; U; Android 9; Android TV Build/PPR1.180610.011)"
                )
                .header("Accept", "*/*")
                .header("Connection", "Keep-Alive")
                .build()

            val response = client.newCall(request).execute()

            Log.e(TAG, "M3U HTTP CODE: ${response.code}")

            if (!response.isSuccessful) {
                val errorText = response.body?.string()
                Log.e(TAG, "M3U ERROR BODY: $errorText")
                throw Exception("M3U HTTP ${response.code}")
            }

            val body = response.body?.string() ?: ""

            parseM3U(body)
        }

    // ================= PARSER =================

    private fun parseM3U(content: String): List<Channel> {

        val channels = mutableListOf<Channel>()
        val lines = content.split("\n")

        var name = ""
        var logo: String? = null
        var group: String? = null

        for (line in lines) {

            if (line.startsWith("#EXTINF")) {

                val nameMatch = Regex(",(.*)").find(line)
                name = nameMatch?.groupValues?.get(1)?.trim() ?: "Channel"

                val logoMatch = Regex("""tvg-logo="(.*?)"""").find(line)
                logo = logoMatch?.groupValues?.get(1)

                val groupMatch = Regex("""group-title="(.*?)"""").find(line)
                group = groupMatch?.groupValues?.get(1)
            }

            if (line.startsWith("http")) {

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

        Log.e(TAG, "Parsed channels: ${channels.size}")

        return channels
    }

    // ================= LIVE CATEGORIES =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {

                val channels = loadM3U()

                val groups = channels
                    .mapNotNull { it.categoryName }
                    .distinct()

                groups.map {
                    Category(
                        categoryId = it,
                        categoryName = it,
                        parentId = 0
                    )
                }
            }
        }

    // ================= LIVE STREAMS =================

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {

                val channels = loadM3U()

                if (categoryId == null) {
                    channels
                } else {
                    channels.filter { it.categoryName == categoryId }
                }
            }
        }

    // ================= STUBS =================

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()
    override suspend fun getFavorites() = emptyList<String>()
    override suspend fun isFavorite(itemId: String) = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}
    override suspend fun addFavorite(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
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
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
}
