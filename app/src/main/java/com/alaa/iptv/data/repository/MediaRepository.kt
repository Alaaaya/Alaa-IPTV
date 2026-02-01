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
    private val epgDao = database.epgDao()
    
    private val apiService by lazy {
        ApiClient.getXtreamApiService(prefs.serverUrl)
    }
    
    private val httpClient by lazy {
        okhttp3.OkHttpClient()
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
    
    // ==================== Phase 2: EPG ====================
    
    override suspend fun getEpgForChannel(channelId: String): List<EpgProgram> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = epgDao.getProgramsByChannel(channelId)
                entities.map { it.toEpgProgram() }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting EPG for channel", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): List<EpgProgram> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = epgDao.getProgramsByChannelAndTimeRange(channelId, startTime, endTime)
                entities.map { it.toEpgProgram() }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting EPG for time range", e)
                emptyList()
            }
        }
    }
    
    override suspend fun getCurrentProgram(channelId: String): EpgProgram? {
        return withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                epgDao.getCurrentProgram(channelId, currentTime)?.toEpgProgram()
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting current program", e)
                null
            }
        }
    }
    
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int): List<EpgProgram> {
        return withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                val entities = epgDao.getUpcomingPrograms(channelId, currentTime, limit)
                entities.map { it.toEpgProgram() }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error getting upcoming programs", e)
                emptyList()
            }
        }
    }
    
    override suspend fun cacheEpgPrograms(programs: List<EpgProgram>) {
        withContext(Dispatchers.IO) {
            try {
                val entities = programs.map { it.toEntity() }
                epgDao.insertPrograms(entities)
                android.util.Log.d("MediaRepository", "Cached ${programs.size} EPG programs")
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error caching EPG programs", e)
            }
        }
    }
    
    override suspend fun cleanupOldEpgData(cutoffTime: Long) {
        withContext(Dispatchers.IO) {
            try {
                epgDao.deleteOldPrograms(cutoffTime)
                android.util.Log.d("MediaRepository", "Cleaned up old EPG data")
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error cleaning up EPG data", e)
            }
        }
    }
    
    // ==================== Phase 2: M3U Support ====================
    
    override suspend fun loadM3UPlaylist(m3uContent: String): Result<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val channels = com.alaa.iptv.utils.M3UParser.parseFromString(m3uContent)
                android.util.Log.d("MediaRepository", "Parsed ${channels.size} channels from M3U")
                Result.success(channels)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error parsing M3U content", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun loadM3UPlaylistFromUrl(url: String): Result<List<Channel>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .build()
                
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val content = response.body?.string() ?: ""
                    val channels = com.alaa.iptv.utils.M3UParser.parseFromString(content)
                    android.util.Log.d("MediaRepository", "Loaded ${channels.size} channels from M3U URL")
                    Result.success(channels)
                } else {
                    Result.failure(Exception("Failed to load M3U from URL: ${response.code}"))
                }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error loading M3U from URL", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun mergeM3UChannels(channels: List<Channel>) {
        withContext(Dispatchers.IO) {
            try {
                val entities = channels.map { it.toEntity() }
                channelDao.insertChannels(entities)
                
                // Extract and cache unique categories from M3U channels
                val categories = channels
                    .mapNotNull { ch -> 
                        ch.categoryId?.let { id -> 
                            ch.categoryName?.let { name -> 
                                Category(id, name) 
                            }
                        }
                    }
                    .distinctBy { it.categoryId }
                    .map { it.toEntity("live") }
                
                if (categories.isNotEmpty()) {
                    categoryDao.insertCategories(categories)
                }
                
                android.util.Log.d("MediaRepository", "Merged ${channels.size} M3U channels into database")
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error merging M3U channels", e)
            }
        }
    }
    
    // ==================== Phase 2: Search ====================
    
    override suspend fun searchChannels(query: String, categoryId: String?): List<Channel> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    channelDao.searchChannelsByNameInCategory(query, categoryId)
                } else {
                    channelDao.searchChannelsByName(query)
                }
                entities.map { it.toChannel(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error searching channels", e)
                emptyList()
            }
        }
    }
    
    override suspend fun searchMovies(query: String, categoryId: String?): List<Movie> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    movieDao.searchMoviesByTitleInCategory(query, categoryId)
                } else {
                    movieDao.searchMoviesByTitle(query)
                }
                entities.map { it.toMovie(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error searching movies", e)
                emptyList()
            }
        }
    }
    
    override suspend fun searchSeries(query: String, categoryId: String?): List<Series> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = if (categoryId != null) {
                    seriesDao.searchSeriesByTitleInCategory(query, categoryId)
                } else {
                    seriesDao.searchSeriesByTitle(query)
                }
                entities.map { it.toSeries(favorites.contains(it.seriesId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error searching series", e)
                emptyList()
            }
        }
    }
    
    override suspend fun searchMoviesByGenre(genre: String): List<Movie> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = movieDao.searchMoviesByGenre(genre)
                entities.map { it.toMovie(favorites.contains(it.streamId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error searching movies by genre", e)
                emptyList()
            }
        }
    }
    
    override suspend fun searchSeriesByGenre(genre: String): List<Series> {
        return withContext(Dispatchers.IO) {
            try {
                val favorites = favoriteDao.getAllFavorites().map { it.itemId }.toSet()
                val entities = seriesDao.searchSeriesByGenre(genre)
                entities.map { it.toSeries(favorites.contains(it.seriesId)) }
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error searching series by genre", e)
                emptyList()
            }
        }
    }
    
    // ==================== Phase 2: Sync Pipeline ====================
    
    override suspend fun syncAllData(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("MediaRepository", "Starting full data sync...")
                
                val liveResult = syncLiveTV()
                if (liveResult.isFailure) {
                    android.util.Log.e("MediaRepository", "Live TV sync failed", liveResult.exceptionOrNull())
                }
                
                val moviesResult = syncMovies()
                if (moviesResult.isFailure) {
                    android.util.Log.e("MediaRepository", "Movies sync failed", moviesResult.exceptionOrNull())
                }
                
                val seriesResult = syncSeries()
                if (seriesResult.isFailure) {
                    android.util.Log.e("MediaRepository", "Series sync failed", seriesResult.exceptionOrNull())
                }
                
                // Cleanup old EPG data (older than 24 hours)
                val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                cleanupOldEpgData(cutoffTime)
                
                android.util.Log.d("MediaRepository", "Full data sync completed")
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error during full sync", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun syncLiveTV(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("MediaRepository", "Syncing Live TV...")
                
                // Sync categories
                val categoriesResult = getLiveCategories()
                if (categoriesResult.isFailure) {
                    return@withContext Result.failure(categoriesResult.exceptionOrNull() ?: Exception("Failed to sync categories"))
                }
                
                // Sync channels
                val channelsResult = getLiveStreams()
                if (channelsResult.isFailure) {
                    return@withContext Result.failure(channelsResult.exceptionOrNull() ?: Exception("Failed to sync channels"))
                }
                
                android.util.Log.d("MediaRepository", "Live TV sync completed")
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error syncing Live TV", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun syncMovies(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("MediaRepository", "Syncing Movies...")
                
                // Sync categories
                val categoriesResult = getMovieCategories()
                if (categoriesResult.isFailure) {
                    return@withContext Result.failure(categoriesResult.exceptionOrNull() ?: Exception("Failed to sync movie categories"))
                }
                
                // Sync movies
                val moviesResult = getMovies()
                if (moviesResult.isFailure) {
                    return@withContext Result.failure(moviesResult.exceptionOrNull() ?: Exception("Failed to sync movies"))
                }
                
                android.util.Log.d("MediaRepository", "Movies sync completed")
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error syncing Movies", e)
                Result.failure(e)
            }
        }
    }
    
    override suspend fun syncSeries(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("MediaRepository", "Syncing Series...")
                
                // Sync categories
                val categoriesResult = getSeriesCategories()
                if (categoriesResult.isFailure) {
                    return@withContext Result.failure(categoriesResult.exceptionOrNull() ?: Exception("Failed to sync series categories"))
                }
                
                // Sync series
                val seriesResult = getSeries()
                if (seriesResult.isFailure) {
                    return@withContext Result.failure(seriesResult.exceptionOrNull() ?: Exception("Failed to sync series"))
                }
                
                android.util.Log.d("MediaRepository", "Series sync completed")
                Result.success(Unit)
            } catch (e: Exception) {
                android.util.Log.e("MediaRepository", "Error syncing Series", e)
                Result.failure(e)
            }
        }
    }
}
