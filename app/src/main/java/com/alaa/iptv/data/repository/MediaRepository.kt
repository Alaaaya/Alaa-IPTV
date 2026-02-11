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

    // ================= AUTH =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val api = ApiClient.getXtreamApiService(serverUrl)
            val response = api.authenticate(username, password)

            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Authentication failed")
            }

            response.body()!!
        }
    }

    // ================= LIVE =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getLiveCategories(prefs.username, prefs.password)

                response.body()?.map {
                    Category(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        parentId = it.parentId
                    )
                } ?: emptyList()
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)

                val response = if (categoryId == null || categoryId == "0") {
                    api.getLiveStreams(prefs.username, prefs.password)
                } else {
                    api.getLiveStreamsByCategory(
                        prefs.username,
                        prefs.password,
                        categoryId = categoryId
                    )
                }

                response.body()?.map {
                    Channel(
                        streamId = it.streamId?.toString() ?: "",
                        num = it.num?.toString() ?: "",
                        name = it.name ?: "",
                        streamType = "live",
                        streamIcon = it.streamIcon,
                        epgChannelId = it.epgChannelId,
                        added = it.added,
                        categoryId = it.categoryId,
                        categoryName = null,
                        customSid = it.customSid,
                        tvArchive = it.tvArchive ?: 0,
                        directSource = null,
                        tvArchiveDuration = it.tvArchiveDuration ?: 0
                    )
                } ?: emptyList()
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) =
        emptyList<Channel>()

    // ================= MOVIES =================

    override suspend fun getMovieCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getVodCategories(prefs.username, prefs.password)

                response.body()?.map {
                    Category(it.categoryId, it.categoryName, it.parentId)
                } ?: emptyList()
            }
        }

    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)

                val response = if (categoryId == null || categoryId == "0") {
                    api.getVodStreams(prefs.username, prefs.password)
                } else {
                    api.getVodStreamsByCategory(
                        prefs.username,
                        prefs.password,
                        categoryId = categoryId
                    )
                }

                response.body()?.map {
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
                } ?: emptyList()
            }
        }

    override suspend fun getMoviesFromCache(categoryId: String?) =
        emptyList<Movie>()

    // ================= SERIES =================

    override suspend fun getSeriesCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getSeriesCategories(prefs.username, prefs.password)

                response.body()?.map {
                    Category(it.categoryId, it.categoryName, it.parentId)
                } ?: emptyList()
            }
        }

    override suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)

                val response = if (categoryId == null || categoryId == "0") {
                    api.getSeries(prefs.username, prefs.password)
                } else {
                    api.getSeriesByCategory(
                        prefs.username,
                        prefs.password,
                        categoryId = categoryId
                    )
                }

                response.body()?.map {
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
                } ?: emptyList()
            }
        }

    override suspend fun getSeriesFromCache(categoryId: String?) =
        emptyList<Series>()

    override suspend fun getSeriesInfo(seriesId: String) =
        Result.success(emptyList<Episode>())

    override suspend fun getEpisodesFromCache(seriesId: String) =
        emptyList<Episode>()

    // ================= STUBS =================

    override suspend fun getFavorites() = emptyList<String>()
    override suspend fun isFavorite(itemId: String) = false
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

    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents() = emptyList<Recent>()
    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ) = Result.success(Unit)

    override suspend fun getRecentViews() =
        Result.success(emptyList<RecentItem>())

    override suspend fun getEpgForChannel(channelId: String) =
        emptyList<EpgProgram>()

    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ) = emptyList<EpgProgram>()

    override suspend fun getCurrentProgram(channelId: String) = null
    override suspend fun getUpcomingPrograms(channelId: String, limit: Int) =
        emptyList<EpgProgram>()

    override suspend fun cacheEpgPrograms(programs: List<EpgProgram>) {}
    override suspend fun cleanupOldEpgData(cutoffTime: Long) {}
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
    override suspend fun updateChannelPosition(channelId: String, newPosition: Int) {}
    override suspend fun getChannelsOrdered(categoryId: String?) =
        emptyList<Channel>()

    override suspend fun loadM3UPlaylist(m3uContent: String) =
        Result.success(emptyList<Channel>())

    override suspend fun loadM3UPlaylistFromUrl(url: String) =
        Result.success(emptyList<Channel>())

    override suspend fun mergeM3UChannels(channels: List<Channel>) {}
}
