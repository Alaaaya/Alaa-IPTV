package com.alaa.iptv.data.repository

import android.content.Context
import com.alaa.iptv.data.api.ApiClient
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    private val apiService by lazy {
        ApiClient.getXtreamApiService(prefs.serverUrl)
    }

    // ==================== AUTH ====================
    // المصادقة الحقيقية تمت في LoginActivity

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

    // ==================== LIVE TV ====================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLiveCategories(
                    prefs.username,
                    prefs.password
                )

                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map {
                        Category(
                            categoryId = it.categoryId,
                            categoryName = it.categoryName,
                            parentId = it.parentId ?: 0
                        )
                    }
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed to load live categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLiveStreams(
                    prefs.username,
                    prefs.password
                )

                if (response.isSuccessful && response.body() != null) {

                    val channels = response.body()!!
                        .filter { it.streamType == "live" }
                        .filter { categoryId == null || it.categoryId == categoryId }
                        .map { stream ->
                            Channel(
                                streamId = stream.streamId.toString(),
                                num = stream.num?.toString() ?: "",
                                name = stream.name ?: "Channel",
                                streamType = "live",
                                streamIcon = stream.streamIcon,
                                epgChannelId = stream.epgChannelId,
                                added = stream.added,
                                categoryId = stream.categoryId,
                                categoryName = null,
                                customSid = stream.customSid,
                                tvArchive = stream.tvArchive ?: 0,
                                directSource = stream.directSource,
                                tvArchiveDuration = stream.tvArchiveDuration ?: 0,
                                isFavorite = false
                            )
                        }

                    // 🔴 LOG مهم
                    println("LIVE CHANNELS FROM API = ${channels.size}")

                    Result.success(channels)
                } else {
                    Result.failure(Exception("Failed to load live streams"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) =
        emptyList<Channel>()

    // ==================== MOVIES (لاحقاً) ====================

    override suspend fun getMovieCategories() =
        Result.success(emptyList<Category>())

    override suspend fun getMovies(categoryId: String?) =
        Result.success(emptyList<Movie>())

    override suspend fun getMoviesFromCache(categoryId: String?) =
        emptyList<Movie>()

    // ==================== SERIES (لاحقاً) ====================

    override suspend fun getSeriesCategories() =
        Result.success(emptyList<Category>())

    override suspend fun getSeries(categoryId: String?) =
        Result.success(emptyList<Series>())

    override suspend fun getSeriesFromCache(categoryId: String?) =
        emptyList<Series>()

    override suspend fun getSeriesInfo(seriesId: String) =
        Result.success(emptyList<Episode>())

    override suspend fun getEpisodesFromCache(seriesId: String) =
        emptyList<Episode>()

    // ==================== FAVORITES ====================

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

    override suspend fun removeFavorite(contentId: String) =
        Result.success(Unit)

    override suspend fun getFavoritesWithDetails() =
        Result.success(emptyList<FavoriteItem>())

    // ==================== RECENTS ====================

    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents(): List<Recent> = emptyList()

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ) = Result.success(Unit)

    override suspend fun getRecentViews() =
        Result.success(emptyList<RecentItem>())

    // ==================== EPG ====================

    override suspend fun getEpgForChannel(channelId: String) =
        emptyList<EpgProgram>()

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

    // ==================== SEARCH ====================

    override suspend fun searchChannels(query: String, categoryId: String?) =
        emptyList<Channel>()

    override suspend fun searchMovies(query: String, categoryId: String?) =
        emptyList<Movie>()

    override suspend fun searchSeries(query: String, categoryId: String?) =
        emptyList<Series>()

    override suspend fun searchMoviesByGenre(genre: String) =
        emptyList<Movie>()

    override suspend fun searchSeriesByGenre(genre: String) =
        emptyList<Series>()

    // ==================== SYNC ====================

    override suspend fun syncAllData() = Result.success(Unit)
    override suspend fun syncLiveTV() = Result.success(Unit)
    override suspend fun syncMovies() = Result.success(Unit)
    override suspend fun syncSeries() = Result.success(Unit)

    // ==================== ORDER ====================

    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {}
    override suspend fun getChannelsOrdered(categoryId: String?) =
        emptyList<Channel>()

    // ==================== M3U ====================

    override suspend fun loadM3UPlaylist(m3uContent: String) =
        Result.success(emptyList<Channel>())

    override suspend fun loadM3UPlaylistFromUrl(url: String) =
        Result.success(emptyList<Channel>())

    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
