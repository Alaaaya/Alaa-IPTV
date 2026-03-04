package com.alaa.iptv.data.repository

import android.content.Context
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

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

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
                XtreamAuthResponse(null, null)
            }
        }

    // ================= M3U =================

    private suspend fun loadM3U(): List<Channel> =
        withContext(Dispatchers.IO) {

            val url =
                "${prefs.serverUrl}/get.php?username=${prefs.username}&password=${prefs.password}&type=m3u_plus&output=ts"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "VLC/3.0.18 LibVLC/3.0.18")
                .header("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("M3U HTTP ${response.code}")
            }

            val body = response.body?.string() ?: ""
            parseM3U(body)
        }

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

        return channels
    }

    // ================= LIVE =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val channels = loadM3U()
                channels.mapNotNull { it.categoryName }
                    .distinct()
                    .map {
                        Category(it, it, 0)
                    }
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val channels = loadM3U()
                if (categoryId == null) channels
                else channels.filter { it.categoryName == categoryId }
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
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
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
