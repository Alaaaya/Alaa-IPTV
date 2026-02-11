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

    // ================= AUTH =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.getXtreamApiService(serverUrl)
                val response = api.authenticate(username, password)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Authentication failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ================= LIVE CATEGORIES =================

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
                            parentId = it.parentId
                        )
                    }

                    Result.success(categories)

                } else {
                    Result.failure(Exception("Failed to load categories"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ================= LIVE STREAMS =================

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {

                val response =
                    if (categoryId == null || categoryId == "0") {
                        apiService.getLiveStreams(
                            prefs.username,
                            prefs.password
                        )
                    } else {
                        apiService.getLiveStreamsByCategory(
                            prefs.username,
                            prefs.password,
                            categoryId = categoryId
                        )
                    }

                if (response.isSuccessful && response.body() != null) {

                    val channels = response.body()!!.map { stream ->
                        Channel(
                            streamId = stream.streamId?.toString() ?: "",
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
                            directSource = null,
                            tvArchiveDuration = stream.tvArchiveDuration ?: 0,
                            isFavorite = false
                        )
                    }

                    Result.success(channels)

                } else {
                    Result.failure(Exception("Failed to load streams"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) =
        emptyList<Channel>()

    // ================= MOVIES =================

    override suspend fun getMovieCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVodCategories(
                    prefs.username,
                    prefs.password
                )

                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map {
                        Category(it.categoryId, it.categoryName, it.parentId)
                    }
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed movies categories"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        withContext(Dispatchers.IO) {
            try {

                val response =
                    if (categoryId == null || categoryId == "0") {
                        apiService.getVodStreams(
                            prefs.username,
                            prefs.password
                        )
                    } else {
                        apiService.getVodStreamsByCategory(
                            prefs.username,
                            prefs.password,
                            categoryId = categoryId
                        )
                    }

                if (response.isSuccessful && response.body() != null) {

                    val movies = response.body()!!.map {
                        Movie(
                            streamId = it.streamId?.toString() ?: "",
                            name = it.name ?: "",
                            streamIcon = it.streamIcon,
                            rating = it.rating,
                            year = it.year,
                            plot = it.plot,
                            cast = it.cast,
                            director = it.director,
                            genre = it.genre,
                            releaseDate = it.releaseDate,
                            durationSecs = it.durationSecs,
                            duration = it.duration,
                            containerExtension = it.containerExtension,
                            categoryId = it.categoryId
                        )
                    }

                    Result.success(movies)

                } else {
                    Result.failure(Exception("Failed movies"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ================= SERIES =================

    override suspend fun getSeriesCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSeriesCategories(
                    prefs.username,
                    prefs.password
                )

                if (response.isSuccessful && response.body() != null) {
                    val categories = response.body()!!.map {
                        Category(it.categoryId, it.categoryName, it.parentId)
                    }
                    Result.success(categories)
                } else {
                    Result.failure(Exception("Failed series categories"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {
            try {

                val response =
                    if (categoryId == null || categoryId == "0") {
                        apiService.getSeries(
                            prefs.username,
                            prefs.password
                        )
                    } else {
                        apiService.getSeriesByCategory(
                            prefs.username,
                            prefs.password,
                            categoryId = categoryId
                        )
                    }

                if (response.isSuccessful && response.body() != null) {

                    val series = response.body()!!.map {
                        Series(
                            seriesId = it.seriesId?.toString() ?: "",
                            name = it.name ?: "",
                            cover = it.cover,
                            plot = it.plot,
                            cast = it.cast,
                            director = it.director,
                            genre = it.genre,
                            releaseDate = it.releaseDate,
                            rating = it.rating,
                            categoryId = it.categoryId
                        )
                    }

                    Result.success(series)

                } else {
                    Result.failure(Exception("Failed series"))
                }

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ===== باقي الدوال غير مستخدمة حالياً =====

    override suspend fun getFavorites(): List<String> = emptyList()
    override suspend fun isFavorite(itemId: String): Boolean = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}
    override suspend fun addFavorite(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
    override suspend fun removeFavorite(contentId: String) = Result.success(Unit)
    override suspend fun getFavoritesWithDetails() = Result.success(emptyList<FavoriteItem>())
    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents(): List<Recent> = emptyList()
    override suspend fun addRecentView(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
    override suspend fun getRecentViews() = Result.success(emptyList<RecentItem>())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
    override suspend fun getEpgForChannel(channelId: String) = emptyList<EpgProgram>()
    override suspend fun getEpgForChannelInTimeRange(channelId: String, startTime: Long, endTime: Long) = emptyList<EpgProgram>()
    override suspend fun getCurrentProgram(channelId: String): EpgProgram? = null
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int) = emptyList<EpgProgram>()
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
    override suspend fun loadM3UPlaylistFromUrl(url: String) = Result.success(emptyList<Channel>())
    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
