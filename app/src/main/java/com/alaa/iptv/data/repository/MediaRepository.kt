package com.alaa.iptv.data.repository

import android.content.Context
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    // ================= AUTH =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        Result.success(
            XtreamAuthResponse(
                userInfo = UserInfo(
                    auth = 1,
                    status = "Active",
                    message = "OK",
                    username = username,
                    password = password,
                    expDate = null,
                    isTrial = null,
                    activeCons = null,
                    createdAt = null,
                    maxConnections = null
                ),
                serverInfo = null
            )
        )

    // ================= LIVE TV (M3U) =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val channels = loadM3U()
                channels
                    .map { it.categoryId ?: "Other" }
                    .distinct()
                    .map {
                        Category(
                            categoryId = it,
                            categoryName = it,
                            parentId = 0
                        )
                    }
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val channels = loadM3U()
                if (categoryId == null || categoryId == "0")
                    channels
                else
                    channels.filter { it.categoryId == categoryId }
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?): List<Channel> =
        emptyList()

    // ================= M3U PARSER =================

    private fun loadM3U(): List<Channel> {
        val url = prefs.serverUrl.trim()
        if (!url.startsWith("http")) return emptyList()

        val lines = try {
            URL(url).readText().lines()
        } catch (e: Exception) {
            return emptyList()
        }

        val channels = mutableListOf<Channel>()

        var name = "Channel"
        var logo: String? = null
        var group: String? = "Other"

        lines.forEach { line ->
            when {
                line.startsWith("#EXTINF") -> {
                    name = Regex(",(.+)").find(line)?.groupValues?.get(1) ?: "Channel"
                    logo = Regex("tvg-logo=\"(.*?)\"")
                        .find(line)?.groupValues?.get(1)
                    group = Regex("group-title=\"(.*?)\"")
                        .find(line)?.groupValues?.get(1) ?: "Other"
                }

                line.startsWith("http") -> {
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
                            directSource = line,
                            tvArchiveDuration = 0,
                            isFavorite = false
                        )
                    )
                }
            }
        }

        return channels
    }

    // ================= REQUIRED STUBS =================

    override suspend fun getFavorites(): List<String> = emptyList()
    override suspend fun isFavorite(itemId: String): Boolean = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}

    override suspend fun addFavorite(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = Result.success(Unit)

    override suspend fun removeFavorite(contentId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun getFavoritesWithDetails(): Result<List<FavoriteItem>> =
        Result.success(emptyList())

    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents(): List<Recent> = emptyList()

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getRecentViews(): Result<List<RecentItem>> =
        Result.success(emptyList())

    override suspend fun getMovieCategories(): Result<List<Category>> =
        Result.success(emptyList())

    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        Result.success(emptyList())

    override suspend fun getMoviesFromCache(categoryId: String?): List<Movie> =
        emptyList()

    override suspend fun getSeriesCategories(): Result<List<Category>> =
        Result.success(emptyList())

    override suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        Result.success(emptyList())

    override suspend fun getSeriesFromCache(categoryId: String?): List<Series> =
        emptyList()

    override suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>> =
        Result.success(emptyList())

    override suspend fun getEpisodesFromCache(seriesId: String): List<Episode> =
        emptyList()

    override suspend fun getEpgForChannel(channelId: String): List<EpgProgram> =
        emptyList()

    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): List<EpgProgram> = emptyList()

    override suspend fun getCurrentProgram(channelId: String): EpgProgram? = null
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int): List<EpgProgram> =
        emptyList()

    override suspend fun cacheEpgPrograms(programs: List<EpgProgram>) {}
    override suspend fun cleanupOldEpgData(cutoffTime: Long) {}

    override suspend fun searchChannels(query: String, categoryId: String?): List<Channel> =
        emptyList()

    override suspend fun searchMovies(query: String, categoryId: String?): List<Movie> =
        emptyList()

    override suspend fun searchSeries(query: String, categoryId: String?): List<Series> =
        emptyList()

    override suspend fun searchMoviesByGenre(genre: String): List<Movie> =
        emptyList()

    override suspend fun searchSeriesByGenre(genre: String): List<Series> =
        emptyList()

    override suspend fun syncAllData(): Result<Unit> = Result.success(Unit)
    override suspend fun syncLiveTV(): Result<Unit> = Result.success(Unit)
    override suspend fun syncMovies(): Result<Unit> = Result.success(Unit)
    override suspend fun syncSeries(): Result<Unit> = Result.success(Unit)

    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {}
    override suspend fun getChannelsOrdered(categoryId: String?): List<Channel> =
        emptyList()

    override suspend fun loadM3UPlaylist(m3uContent: String): Result<List<Channel>> =
        Result.success(emptyList())

    override suspend fun loadM3UPlaylistFromUrl(url: String): Result<List<Channel>> =
        Result.success(emptyList())

    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
