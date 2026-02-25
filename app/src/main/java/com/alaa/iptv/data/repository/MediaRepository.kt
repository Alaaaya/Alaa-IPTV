package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.api.ApiClient
import com.alaa.iptv.data.api.XtreamApiService
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    companion object {
        private const val TAG = "MediaRepository"
    }

    // ================= AUTH =================

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching {

                val cleanUrl = serverUrl.trim().removeSuffix("/")

                // ✅ الطريقة البسيطة
                val api = ApiClient.getXtreamApiService(cleanUrl)
                val response = api.authenticate(username, password)
                
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }
                
                val body = response.body() ?: throw Exception("Empty response")

                prefs.serverUrl = cleanUrl
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true

                body
            }
        }

    // ================= LIVE CATEGORIES =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                
                Log.d(TAG, "Fetching live categories...")

                // ✅ الطريقة البسيطة
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getLiveCategories(
                    prefs.username,
                    prefs.password
                )

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: emptyList()

                body.map {
                    Category(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        parentId = it.parentId
                    )
                }
            }
        }

    // ================= LIVE STREAMS =================

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {

                Log.d(TAG, "Fetching live streams for category: $categoryId")

                // ✅ الطريقة البسيطة
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

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: emptyList()

                body.map { stream ->
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
                        directSource = stream.directSource,
                        tvArchiveDuration = stream.tvArchiveDuration ?: 0
                    )
                }
            }
        }

    // ================= VOD CATEGORIES =================

    override suspend fun getMovieCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                
                // ✅ الطريقة البسيطة
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getVodCategories(prefs.username, prefs.password)
                
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }
                
                val body = response.body() ?: emptyList()

                body.map {
                    Category(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        parentId = it.parentId
                    )
                }
            }
        }

    // ================= VOD MOVIES =================

    override suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        withContext(Dispatchers.IO) {
            runCatching {

                // ✅ الطريقة البسيطة
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

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: emptyList()

                body.map { stream ->
                    Movie(
                        streamId = stream.streamId?.toString() ?: "",
                        name = stream.name ?: "Movie",
                        streamIcon = stream.streamIcon,
                        rating = stream.rating,
                        year = stream.year,
                        plot = stream.plot,
                        cast = stream.cast,
                        director = stream.director,
                        genre = stream.genre,
                        releaseDate = stream.releaseDate,
                        durationSecs = stream.durationSecs,
                        duration = stream.duration,
                        containerExtension = stream.containerExtension,
                        categoryId = stream.categoryId,
                        isFavorite = false
                    )
                }
            }
        }

    // ================= SERIES CATEGORIES =================

    override suspend fun getSeriesCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {
                
                // ✅ الطريقة البسيطة
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getSeriesCategories(prefs.username, prefs.password)
                
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }
                
                val body = response.body() ?: emptyList()

                body.map {
                    Category(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        parentId = it.parentId
                    )
                }
            }
        }

    // ================= SERIES =================

    override suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {
            runCatching {

                // ✅ الطريقة البسيطة
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

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: emptyList()

                body.map { series ->
                    Series(
                        seriesId = series.seriesId?.toString() ?: "",
                        name = series.name ?: "Series",
                        cover = series.cover,
                        plot = series.plot,
                        cast = series.cast,
                        director = series.director,
                        genre = series.genre,
                        rating = series.rating,
                        categoryId = series.categoryId,
                        releaseDate = series.releaseDate
                    )
                }
            }
        }

    // ================= SERIES INFO =================

    override suspend fun getSeriesInfo(seriesId: String): Result<List<Episode>> =
        withContext(Dispatchers.IO) {
            runCatching {

                // ✅ الطريقة البسيطة
                val api = ApiClient.getXtreamApiService(prefs.serverUrl)
                val response = api.getSeriesInfo(
                    prefs.username,
                    prefs.password,
                    seriesId = seriesId
                )

                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code()}")
                }

                val seriesInfo = response.body()
                
                // تحويل XtreamEpisode إلى Episode
                val episodes = mutableListOf<Episode>()
                
                seriesInfo?.episodes?.forEach { (seasonNum, episodeList) ->
                    episodeList?.forEach { xtreamEpisode ->
                        episodes.add(
                            Episode(
                                id = xtreamEpisode.id,
                                episodeNum = xtreamEpisode.episodeNum,
                                title = xtreamEpisode.title ?: "Episode ${xtreamEpisode.episodeNum}",
                                containerExtension = xtreamEpisode.containerExtension,
                                info = EpisodeInfo(
                                    plot = xtreamEpisode.info?.plot,
                                    duration = xtreamEpisode.info?.duration,
                                    rating = xtreamEpisode.info?.rating
                                ),
                                seasonNumber = seasonNum.toIntOrNull() ?: 1
                            )
                        )
                    }
                }
                
                episodes
            }
        }

    // ================= STUB METHODS =================

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()
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

    override suspend fun removeFavorite(contentId: String) = Result.success(Unit)
    override suspend fun getFavoritesWithDetails() = Result.success(emptyList<FavoriteItem>())
    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents() = emptyList<Recent>()

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ) = Result.success(Unit)

    override suspend fun getRecentViews() = Result.success(emptyList<RecentItem>())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
    override suspend fun getEpgForChannel(channelId: String) = emptyList<EpgProgram>()
    
    override suspend fun getEpgForChannelInTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ) = emptyList<EpgProgram>()

    override suspend fun getCurrentProgram(channelId: String) = null
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
