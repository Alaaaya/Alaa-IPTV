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

/**
 * Implementation of IMediaRepository
 * Integrates Xtream API, M3U data sources with local Room database cache
 */
class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {
    
    private val database = AppDatabase.getInstance(context)
    private val channelDao = database.channelDao()
    private val categoryDao = database.categoryDao()
    private val movieDao = database.movieDao()
    private val seriesDao = database.seriesDao()
    private val episodeDao = database.episodeDao()
    private val favoriteDao = database.favoriteDao()
    private val recentDao = database.recentDao()
    
    private val apiService by lazy {
        ApiClient.getXtreamApiService(prefs.serverUrl)
    }
    
    // ==================== Authentication ====================
    
    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val service = ApiClient.getXtreamApiService(serverUrl)
                val response = service.authenticate(username, password)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Authentication failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // ==================== Live TV ====================
    
    override suspend fun getLiveCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLiveCategories(prefs.username, prefs.password)
                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map { 
                        Category(it.categoryId, it.categoryName, it.parentId)
                    }
                    
                    // Cache categories
                    val entities = categories.map { it.toEntity("live") }
                    categoryDao.insertCategories(entities)
                    
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed to load categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (categoryId != null) {
                    apiService.getLiveStreamsByCategory(prefs.username, prefs.password, categoryId = categoryId)
                } else {
                    apiService.getLiveStreams(prefs.username, prefs.password)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                    val channels = response.body()!!.mapNotNull { stream ->
                        stream.streamId?.let { id ->
                            Channel(
                                streamId = id.toString(),
                                num = (stream.num ?: id).toString(),
                                name = stream.name ?: "Unknown",
                                streamType = stream.streamType ?: "live",
                                streamIcon = stream.streamIcon,
                                epgChannelId = stream.epgChannelId,
                                added = stream.added,
                                categoryId = stream.categoryId,
                                categoryName = null,
                                customSid = stream.customSid,
                                tvArchive = stream.tvArchive ?: 0,
                                directSource = stream.directSource,
                                tvArchiveDuration = stream.tvArchiveDuration ?: 0,
                                isFavorite = favorites.contains(id.toString())
                            )
                        }
                    }
                    
                    // Cache channels
                    val entities = channels.map { it.toEntity() }
                    channelDao.insertChannels(entities)
                    
                    Result.success(channels)
                } else {
                    Result.failure(Exception("Failed to load channels"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getLiveStreamsFromCache(categoryId: String?): List<Channel> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    channelDao.getChannelsByCategory(categoryId)
                } else {
                    channelDao.getAllChannels()
                }
                entities.map { it.toChannel(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // ==================== Movies ====================
    
    override suspend fun getMovieCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVodCategories(prefs.username, prefs.password)
                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map { 
                        Category(it.categoryId, it.categoryName, it.parentId)
                    }
                    
                    // Cache categories
                    val entities = categories.map { it.toEntity("movie") }
                    categoryDao.insertCategories(entities)
                    
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed to load categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (categoryId != null) {
                    apiService.getVodStreamsByCategory(prefs.username, prefs.password, categoryId = categoryId)
                } else {
                    apiService.getVodStreams(prefs.username, prefs.password)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                    val movies = response.body()!!.mapNotNull { movie ->
                        movie.streamId?.let { id ->
                            Movie(
                                streamId = id.toString(),
                                name = movie.name ?: "Unknown",
                                streamIcon = movie.streamIcon,
                                rating = movie.rating,
                                year = movie.year,
                                plot = movie.plot,
                                cast = movie.cast,
                                director = movie.director,
                                genre = movie.genre,
                                releaseDate = movie.releaseDate,
                                durationSecs = movie.durationSecs,
                                duration = movie.duration,
                                containerExtension = movie.containerExtension,
                                categoryId = movie.categoryId,
                                isFavorite = favorites.contains(id.toString())
                            )
                        }
                    }
                    
                    // Cache movies
                    val entities = movies.map { it.toEntity() }
                    movieDao.insertMovies(entities)
                    
                    Result.success(movies)
                } else {
                    Result.failure(Exception("Failed to load movies"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getMoviesFromCache(categoryId: String?): List<Movie> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    movieDao.getMoviesByCategory(categoryId)
                } else {
                    movieDao.getAllMovies()
                }
                entities.map { it.toMovie(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // ==================== Series ====================
    
    override suspend fun getSeriesCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeriesCategories(prefs.username, prefs.password)
                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map { 
                        Category(it.categoryId, it.categoryName, it.parentId)
                    }
                    
                    // Cache categories
                    val entities = categories.map { it.toEntity("series") }
                    categoryDao.insertCategories(entities)
                    
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed to load categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getSeries(categoryId: String?): Result<List<Series>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (categoryId != null) {
                    apiService.getSeriesByCategory(prefs.username, prefs.password, categoryId = categoryId)
                } else {
                    apiService.getSeries(prefs.username, prefs.password)
                }
                
                if (response.isSuccessful && response.body() != null) {
                    val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                    val series = response.body()!!.mapNotNull { s ->
                        s.seriesId?.let { id ->
                            Series(
                                seriesId = id.toString(),
                                name = s.name ?: "Unknown",
                                cover = s.cover,
                                plot = s.plot,
                                cast = s.cast,
                                director = s.director,
                                genre = s.genre,
                                releaseDate = s.releaseDate,
                                rating = s.rating,
                                categoryId = s.categoryId,
                                isFavorite = favorites.contains(id.toString())
                            )
                        }
                    }
                    
                    // Cache series
                    val entities = series.map { it.toEntity() }
                    seriesDao.insertMultipleSeries(entities)
                    
                    Result.success(series)
                } else {
                    Result.failure(Exception("Failed to load series"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getSeriesFromCache(categoryId: String?): List<Series> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    seriesDao.getSeriesByCategory(categoryId)
                } else {
                    seriesDao.getAllSeries()
                }
                entities.map { it.toSeries(favorites.contains(it.seriesId)) }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    override suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeriesInfo(prefs.username, prefs.password, seriesId = seriesId)
                if (response.isSuccessful && response.body() != null) {
                    val seriesInfo = response.body()!!
                    val episodes = mutableListOf<Episode>()
                    
                    seriesInfo.episodes?.forEach { (seasonNum, episodeList) ->
                        episodeList.forEach { ep ->
                            episodes.add(
                                Episode(
                                    id = ep.id,
                                    episodeNum = ep.episodeNum,
                                    title = ep.title ?: "Episode ${ep.episodeNum}",
                                    containerExtension = ep.containerExtension,
                                    info = ep.info?.let {
                                        EpisodeInfo(it.plot, it.duration, it.rating)
                                    },
                                    seasonNumber = seasonNum.toIntOrNull() ?: 0
                                )
                            )
                        }
                    }
                    
                    // Cache episodes
                    val entities = episodes.map { it.toEntity(seriesId) }
                    episodeDao.insertEpisodes(entities)
                    
                    Result.success(episodes)
                } else {
                    Result.failure(Exception("Failed to load series info"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getEpisodesFromCache(seriesId: String): List<Episode> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = episodeDao.getEpisodesBySeriesId(seriesId)
                entities.map { it.toEpisode() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    // ==================== Favorites ====================
    
    override suspend fun getFavorites(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                favoriteDao.getAllFavorites().map { it.itemId }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting favorites", e)
                emptyList()
            }
        }
    }
    
    override suspend fun addFavorite(itemId: String) {
        withContext(Dispatchers.IO) {
            try {
                // Determine item type based on existing data
                val itemType = when {
                    channelDao.getChannelById(itemId) != null -> "channel"
                    movieDao.getMovieById(itemId) != null -> "movie"
                    seriesDao.getSeriesById(itemId) != null -> "series"
                    else -> "unknown"
                }
                val favorite = FavoriteEntity(itemId = itemId, itemType = itemType)
                favoriteDao.insertFavorite(favorite)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error adding favorite: $itemId", e)
            }
        }
    }
    
    override suspend fun removeFavorite(itemId: String) {
        withContext(Dispatchers.IO) {
            try {
                favoriteDao.deleteFavoriteById(itemId)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error removing favorite: $itemId", e)
            }
        }
    }
    
    override suspend fun isFavorite(itemId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                favoriteDao.isFavorite(itemId)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error checking favorite: $itemId", e)
                false
            }
        }
    }
    
    // ==================== Recent ====================
    
    override suspend fun addRecent(itemId: String, itemType: String) {
        withContext(Dispatchers.IO) {
            try {
                val recent = RecentEntity(itemId = itemId, itemType = itemType)
                recentDao.insertRecent(recent)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error adding recent: $itemId", e)
            }
        }
    }
    
    override suspend fun getRecents(): List<Recent> {
        return withContext(Dispatchers.IO) {
            try {
                recentDao.getAllRecents().map { it.toRecent() }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting recents", e)
                emptyList()
            }
        }
    }
    
    // ==================== Reordering ====================
    
    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {
        withContext(Dispatchers.IO) {
            try {
                channelDao.updateChannelPosition(channelId, newPosition)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error updating channel position: $channelId", e)
            }
        }
    }
    
    override suspend fun getChannelsOrdered(categoryId: String?): List<Channel> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    channelDao.getChannelsByCategory(categoryId)
                } else {
                    channelDao.getAllChannels()
                }
                entities.map { it.toChannel(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting ordered channels", e)
                emptyList()
            }
        }
    }
}
