package com.alaaaya.iptv.data

import com.alaaaya.iptv.data.api.*
import com.alaaaya.iptv.data.db.*
import com.alaaaya.iptv.data.models.*
import com.alaaaya.iptv.utils.Result
import com.alaaaya.iptv.utils.StreamUrlBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class IptvRepository(
    private val api: XtreamCodesApi,
    private val database: AppDatabase,
    private val serverUrl: String,
    private val username: String,
    private val password: String
) {
    private val channelDao = database.channelDao()
    private val movieDao = database.movieDao()
    private val seriesDao = database.seriesDao()
    private val episodeDao = database.episodeDao()
    private val categoryDao = database.categoryDao()
    private val watchHistoryDao = database.watchHistoryDao()

    // Authentication
    suspend fun authenticate(): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.authenticate(username, password)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception("Authentication failed"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Live Channels
    suspend fun fetchAndStoreLiveChannels(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Fetch categories
            val categoriesResponse = api.getLiveCategories(username, password)
            if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                val categories = categoriesResponse.body()!!.map { cat ->
                    Category(
                        id = cat.category_id ?: "",
                        name = cat.category_name ?: "",
                        type = "live"
                    )
                }
                categoryDao.insertCategories(categories)
            }

            // Fetch channels
            val channelsResponse = api.getLiveStreams(username, password)
            if (channelsResponse.isSuccessful && channelsResponse.body() != null) {
                val channels = channelsResponse.body()!!.mapNotNull { stream ->
                    val streamId = stream.stream_id?.toString() ?: return@mapNotNull null
                    val streamUrl = StreamUrlBuilder.buildLiveUrl(
                        serverUrl, username, password, streamId
                    )
                    Channel(
                        id = streamId,
                        name = stream.name ?: "",
                        streamUrl = streamUrl,
                        streamType = "live",
                        categoryId = stream.category_id ?: "",
                        iconUrl = stream.stream_icon ?: "",
                        epgChannelId = stream.epg_channel_id ?: "",
                        tvArchive = stream.tv_archive ?: 0,
                        tvArchiveDuration = stream.tv_archive_duration ?: 0
                    )
                }
                channelDao.insertChannels(channels)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to fetch channels"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getAllChannels(): Flow<List<Channel>> = channelDao.getAllChannels()
    fun getChannelsByCategory(categoryId: String): Flow<List<Channel>> = 
        channelDao.getChannelsByCategory(categoryId)
    fun getFavoriteChannels(): Flow<List<Channel>> = channelDao.getFavoriteChannels()
    fun searchChannels(query: String): Flow<List<Channel>> = channelDao.searchChannels(query)
    
    suspend fun toggleChannelFavorite(channelId: String, isFavorite: Boolean) {
        channelDao.updateFavoriteStatus(channelId, isFavorite)
    }

    // Movies
    suspend fun fetchAndStoreMovies(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Fetch categories
            val categoriesResponse = api.getVodCategories(username, password)
            if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                val categories = categoriesResponse.body()!!.map { cat ->
                    Category(
                        id = cat.category_id ?: "",
                        name = cat.category_name ?: "",
                        type = "movie"
                    )
                }
                categoryDao.insertCategories(categories)
            }

            // Fetch movies
            val moviesResponse = api.getVodStreams(username, password)
            if (moviesResponse.isSuccessful && moviesResponse.body() != null) {
                val movies = moviesResponse.body()!!.mapNotNull { stream ->
                    val streamId = stream.stream_id?.toString() ?: return@mapNotNull null
                    val extension = stream.container_extension ?: "mp4"
                    val streamUrl = StreamUrlBuilder.buildVodUrl(
                        serverUrl, username, password, streamId, extension
                    )
                    Movie(
                        id = streamId,
                        name = stream.name ?: "",
                        streamUrl = streamUrl,
                        categoryId = stream.category_id ?: "",
                        iconUrl = stream.stream_icon ?: "",
                        rating = stream.rating_5based ?: 0f,
                        containerExtension = extension
                    )
                }
                movieDao.insertMovies(movies)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to fetch movies"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getAllMovies(): Flow<List<Movie>> = movieDao.getAllMovies()
    fun getMoviesByCategory(categoryId: String): Flow<List<Movie>> = 
        movieDao.getMoviesByCategory(categoryId)
    fun getFavoriteMovies(): Flow<List<Movie>> = movieDao.getFavoriteMovies()
    fun searchMovies(query: String): Flow<List<Movie>> = movieDao.searchMovies(query)
    
    suspend fun toggleMovieFavorite(movieId: String, isFavorite: Boolean) {
        movieDao.updateFavoriteStatus(movieId, isFavorite)
    }

    // Series
    suspend fun fetchAndStoreSeries(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Fetch categories
            val categoriesResponse = api.getSeriesCategories(username, password)
            if (categoriesResponse.isSuccessful && categoriesResponse.body() != null) {
                val categories = categoriesResponse.body()!!.map { cat ->
                    Category(
                        id = cat.category_id ?: "",
                        name = cat.category_name ?: "",
                        type = "series"
                    )
                }
                categoryDao.insertCategories(categories)
            }

            // Fetch series
            val seriesResponse = api.getSeries(username, password)
            if (seriesResponse.isSuccessful && seriesResponse.body() != null) {
                val seriesList = seriesResponse.body()!!.mapNotNull { series ->
                    val seriesId = series.series_id?.toString() ?: return@mapNotNull null
                    Series(
                        id = seriesId,
                        name = series.name ?: "",
                        categoryId = series.category_id ?: "",
                        iconUrl = series.cover ?: "",
                        plot = series.plot ?: "",
                        cast = series.cast ?: "",
                        director = series.director ?: "",
                        genre = series.genre ?: "",
                        releaseDate = series.releaseDate ?: "",
                        rating = series.rating_5based ?: 0f,
                        episodeRunTime = series.episode_run_time ?: ""
                    )
                }
                seriesDao.insertMultipleSeries(seriesList)
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to fetch series"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getAllSeries(): Flow<List<Series>> = seriesDao.getAllSeries()
    fun getSeriesByCategory(categoryId: String): Flow<List<Series>> = 
        seriesDao.getSeriesByCategory(categoryId)
    fun getFavoriteSeries(): Flow<List<Series>> = seriesDao.getFavoriteSeries()
    fun searchSeries(query: String): Flow<List<Series>> = seriesDao.searchSeries(query)
    
    suspend fun toggleSeriesFavorite(seriesId: String, isFavorite: Boolean) {
        seriesDao.updateFavoriteStatus(seriesId, isFavorite)
    }

    // Categories
    fun getCategoriesByType(type: String): Flow<List<Category>> = 
        categoryDao.getCategoriesByType(type)

    // Watch History
    suspend fun saveWatchHistory(contentId: String, contentType: String, contentName: String, position: Long = 0) {
        val history = WatchHistory(
            contentId = contentId,
            contentType = contentType,
            contentName = contentName,
            position = position
        )
        watchHistoryDao.insertHistory(history)
    }

    fun getRecentHistory(): Flow<List<WatchHistory>> = watchHistoryDao.getRecentHistory()
}
