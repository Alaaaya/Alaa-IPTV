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

    // ==================== AUTH ====================
    // المصادقة تمت فعليًا عبر إدخال M3U
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

    // ==================== LIVE TV (M3U) ====================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val channels = loadM3U()
                val categories = channels
                    .map { it.categoryId }
                    .distinct()
                    .map {
                        Category(
                            categoryId = it ?: "other",
                            categoryName = it ?: "Other",
                            parentId = 0
                        )
                    }

                Result.success(categories)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {
                val channels = loadM3U()
                Result.success(
                    if (categoryId == null)
                        channels
                    else
                        channels.filter { it.categoryId == categoryId }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) =
        emptyList<Channel>()

    // ==================== M3U PARSER ====================

    private suspend fun loadM3U(): List<Channel> =
        withContext(Dispatchers.IO) {

            val url = prefs.serverUrl
            val lines = URL(url).readText().lines()

            val channels = mutableListOf<Channel>()

            var name = ""
            var logo: String? = null
            var group: String? = null

            lines.forEach { line ->
                when {
                    line.startsWith("#EXTINF") -> {
                        name = Regex(",(.+)")
                            .find(line)?.groupValues?.get(1) ?: "Channel"

                        logo = Regex("tvg-logo=\"(.*?)\"")
                            .find(line)?.groupValues?.get(1)

                        group = Regex("group-title=\"(.*?)\"")
                            .find(line)?.groupValues?.get(1)
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

            println("✅ M3U CHANNELS LOADED = ${channels.size}")
            channels
        }

    // ==================== STUBS (Not used now) ====================

    override suspend fun getMovieCategories() = Result.success(emptyList<Category>())
    override suspend fun getMovies(categoryId: String?) = Result.success(emptyList<Movie>())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()

    override suspend fun getSeriesCategories() = Result.success(emptyList<Category>())
    override suspend fun getSeries(categoryId: String?) = Result.success(emptyList<Series>())
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()

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
    ) = Result.success(Unit)
    override suspend fun removeFavorite(contentId: String) = Result.success(Unit)
    override suspend fun getFavoritesWithDetails() = Result.success(emptyList<FavoriteItem>())

    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents(): List<Recent> = emptyList()
    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ) = Result.success(Unit)
    override suspend fun getRecentViews() = Result.success(emptyList<RecentItem>())

    override suspend fun getEpgForChannel(channelId: String) = emptyList<EpgProgram>()
    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ) = emptyList<EpgProgram>()
    override suspend fun getCurrentProgram(channelId: String): EpgProgram? = null
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int) =
        emptyList<EpgProgram>()

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
    override suspend fun loadM3UPlaylistFromUrl(url: String) =
        Result.success(emptyList<Channel>())
    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
