package com.alaa.iptv.data.repository

import android.content.Context
import com.alaa.iptv.data.api.ApiClient
import com.alaa.iptv.data.local.AppDatabase
import com.alaa.iptv.data.local.entity.FavoriteEntity
import com.alaa.iptv.data.local.entity.RecentEntity
import com.alaa.iptv.data.local.mapper.*
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    private val database = AppDatabase.getInstance(context)
    private val favoriteDao = database.favoriteDao()
    private val recentDao = database.recentDao()

    private val apiService by lazy {
        ApiClient.getXtreamApiService(prefs.serverUrl)
    }

    // ==================== AUTH ====================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val service = ApiClient.getXtreamApiService(serverUrl)
            val response = service.authenticate(username, password)
            if (response.isSuccessful && response.body() != null)
                Result.success(response.body()!!)
            else
                Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== LIVE TV ====================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLiveCategories()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.map { it.toCategory() })
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
                val response = apiService.getLiveStreams()
                if (response.isSuccessful && response.body() != null) {

                    val channels = response.body()!!
                        .filter { it.streamType == "live" }
                        .filter { categoryId == null || it.categoryId == categoryId }
                        .map { it.toChannel() }

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

    // ==================== MOVIES (جاهز لاحقاً) ====================

    override suspend fun getMovieCategories() =
        Result.success(emptyList<Category>())

    override suspend fun getMovies(categoryId: String?) =
        Result.success(emptyList<Movie>())

    override suspend fun getMoviesFromCache(categoryId: String?) =
        emptyList<Movie>()

    // ==================== SERIES (جاهز لاحقاً) ====================

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

    override suspend fun addFavorite(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    itemId = contentId,
                    itemType = type,
                    name = name,
                    icon = icon,
                    categoryId = categoryId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(contentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                favoriteDao.deleteFavoriteById(contentId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getFavoritesWithDetails(): Result<List<FavoriteItem>> =
        withContext(Dispatchers.IO) {
            Result.success(favoriteDao.getAllFavorites().map { it.toFavoriteItem() })
        }

    override suspend fun getFavorites(): List<String> =
        favoriteDao.getAllFavorites().map { it.itemId }

    override suspend fun isFavorite(itemId: String): Boolean =
        favoriteDao.isFavorite(itemId)

    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}

    // ==================== RECENTS ====================

    override suspend fun addRecent(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            recentDao.insertRecent(
                RecentEntity(itemId = itemId, itemType = itemType)
            )
        }

    override suspend fun getRecents(): List<Recent> =
        recentDao.getAllRecents().map { it.toRecent() }

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recentDao.insertRecent(
                RecentEntity(
                    itemId = contentId,
                    itemType = type,
                    name = name,
                    icon = icon,
                    categoryId = categoryId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentViews(): Result<List<RecentItem>> =
        withContext(Dispatchers.IO) {
            Result.success(recentDao.getAllRecents().map { it.toRecentItem() })
        }

    // ==================== STUBS (لاحقاً) ====================

    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {}
    override suspend fun getChannelsOrdered(categoryId: String?) = emptyList<Channel>()

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

    override suspend fun loadM3UPlaylist(m3uContent: String) =
        Result.success(emptyList<Channel>())

    override suspend fun loadM3UPlaylistFromUrl(url: String) =
        Result.success(emptyList<Channel>())

    override suspend fun mergeM3UChannels(channels: List<Channel>) {}

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

    override suspend fun syncAllData() = Result.success(Unit)
    override suspend fun syncLiveTV() = Result.success(Unit)
    override suspend fun syncMovies() = Result.success(Unit)
    override suspend fun syncSeries() = Result.success(Unit)
}
