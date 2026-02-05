package com.alaa.iptv.data.repository

import android.content.Context
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    // ==================== AUTH ====================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> = withContext(Dispatchers.IO) {
        Result.failure(Exception("Auth handled elsewhere"))
    }

    // ==================== LIVE TV ====================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        Result.success(emptyList())

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        Result.success(emptyList())

    override suspend fun getLiveStreamsFromCache(categoryId: String?) =
        emptyList<Channel>()

    // ==================== MOVIES ====================

    override suspend fun getMovieCategories(): Result<List<Category>> =
        Result.success(emptyList())

    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        Result.success(emptyList())

    override suspend fun getMoviesFromCache(categoryId: String?) =
        emptyList<Movie>()

    // ==================== SERIES ====================

    override suspend fun getSeriesCategories(): Result<List<Category>> =
        Result.success(emptyList())

    override suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        Result.success(emptyList())

    override suspend fun getSeriesFromCache(categoryId: String?) =
        emptyList<Series>()

    override suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>> =
        Result.success(emptyList())

    override suspend fun getEpisodesFromCache(seriesId: String) =
        emptyList<Episode>()

    // ==================== FAVORITES ====================

    override suspend fun getFavorites(): List<String> =
        emptyList()

    override suspend fun isFavorite(itemId: String): Boolean =
        false

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

    // ==================== RECENTS ====================

    override suspend fun addRecent(itemId: String, itemType: String) {}

    override suspend fun getRecents(): List<Recent> =
        emptyList()

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = Result.success(Unit)

    override suspend fun getRecentViews(): Result<List<RecentItem>> =
        Result.success(emptyList())

    // ==================== EPG ====================

    override suspend fun getEpgForChannel(channelId: String) =
        emptyList<EpgProgram>()

    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ) = emptyList<EpgProgram>()

    override suspend fun getCurrentProgram(channelId: String): EpgProgram? =
        null

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
