package com.alaa.iptv.domain.repository

import com.alaa.iptv.data.models.*

/**
 * Repository interface for media operations.
 * Defines contract for accessing Live TV, Movies, Series, and Favorites.
 */
interface IMediaRepository {
    
    // Authentication
    suspend fun authenticate(serverUrl: String, username: String, password: String): Result<XtreamAuthResponse>
    
    // Live TV
    suspend fun getLiveCategories(): Result<List<Category>>
    suspend fun getLiveStreams(categoryId: String? = null): Result<List<Channel>>
    suspend fun getLiveStreamsFromCache(categoryId: String? = null): List<Channel>
    
    // Movies
    suspend fun getMovieCategories(): Result<List<Category>>
    suspend fun getMovies(categoryId: String? = null): Result<List<Movie>>
    suspend fun getMoviesFromCache(categoryId: String? = null): List<Movie>
    
    // Series
    suspend fun getSeriesCategories(): Result<List<Category>>
    suspend fun getSeries(categoryId: String? = null): Result<List<Series>>
    suspend fun getSeriesFromCache(categoryId: String? = null): List<Series>
    suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>>
    suspend fun getEpisodesFromCache(seriesId: String): List<Episode>
    
    // Favorites
    suspend fun getFavorites(): List<String>
    suspend fun addFavorite(itemId: String)
    suspend fun removeFavorite(itemId: String)
    suspend fun isFavorite(itemId: String): Boolean
    
    // Recent
    suspend fun addRecent(itemId: String, itemType: String)
    suspend fun getRecents(): List<Recent>
    
    // Reordering
    suspend fun updateChannelPosition(channelId: String, newPosition: Int)
    suspend fun getChannelsOrdered(categoryId: String? = null): List<Channel>
}
