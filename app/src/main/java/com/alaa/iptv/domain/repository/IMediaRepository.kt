package com.alaa.iptv.domain.repository

import com.alaa.iptv.data.models.*

/**
 * Repository interface for media operations.
 * Defines contract for accessing Live TV, Movies, Series, Favorites, EPG, and Search.
 */
interface IMediaRepository {

    // ==================== Authentication ====================
    suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse>

    // ==================== Live TV ====================
    suspend fun getLiveCategories(): Result<List<Category>>
    suspend fun getLiveStreams(categoryId: String? = null): Result<List<Channel>>
    suspend fun getLiveStreamsFromCache(categoryId: String? = null): List<Channel>

    // ==================== Movies ====================
    suspend fun getMovieCategories(): Result<List<Category>>
    suspend fun getMovies(categoryId: String? = null): Result<List<Movie>>
    suspend fun getMoviesFromCache(categoryId: String? = null): List<Movie>

    // ==================== Series ====================
    suspend fun getSeriesCategories(): Result<List<Category>>
    suspend fun getSeries(categoryId: String? = null): Result<List<Series>>
    suspend fun getSeriesFromCache(categoryId: String? = null): List<Series>
    suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>>
    suspend fun getEpisodesFromCache(seriesId: String): List<Episode>

    // ==================== Favorites (basic) ====================
    suspend fun getFavorites(): List<String>
    suspend fun addFavorite(itemId: String)
    suspend fun removeBasicFavorite(itemId: String)
    suspend fun isFavorite(itemId: String): Boolean

    // ==================== Favorites (extended with details) ====================
    suspend fun addFavorite(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit>

    suspend fun removeFavorite(contentId: String): Result<Unit>
    suspend fun getFavoritesWithDetails(): Result<List<FavoriteItem>>

    // ==================== Recent (basic) ====================
    suspend fun addRecent(itemId: String, itemType: String)
    suspend fun getRecents(): List<Recent>

    // ==================== Recent (extended with details) ====================
    suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit>

    suspend fun getRecentViews(): Result<List<RecentItem>>

    // ==================== Reordering ====================
    suspend fun updateChannelPosition(channelId: String, newPosition: Int)
    suspend fun getChannelsOrdered(categoryId: String? = null): List<Channel>

    // ==================== EPG ====================
    suspend fun getEpgForChannel(channelId: String): List<EpgProgram>
    suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): List<EpgProgram>

    suspend fun getCurrentProgram(channelId: String): EpgProgram?
    suspend fun getUpcomingPrograms(channelId: String, limit: Int = 10): List<EpgProgram>
    suspend fun cacheEpgPrograms(programs: List<EpgProgram>)
    suspend fun cleanupOldEpgData(cutoffTime: Long)

    // ==================== M3U Support ====================
    suspend fun loadM3UPlaylist(m3uContent: String): Result<List<Channel>>
    suspend fun loadM3UPlaylistFromUrl(url: String): Result<List<Channel>>
    suspend fun mergeM3UChannels(channels: List<Channel>)

    // ==================== Search ====================
    suspend fun searchChannels(query: String, categoryId: String? = null): List<Channel>
    suspend fun searchMovies(query: String, categoryId: String? = null): List<Movie>
    suspend fun searchSeries(query: String, categoryId: String? = null): List<Series>
    suspend fun searchMoviesByGenre(genre: String): List<Movie>
    suspend fun searchSeriesByGenre(genre: String): List<Series>

    // ==================== Sync ====================
    suspend fun syncAllData(): Result<Unit>
    suspend fun syncLiveTV(): Result<Unit>
    suspend fun syncMovies(): Result<Unit>
    suspend fun syncSeries(): Result<Unit>
}
