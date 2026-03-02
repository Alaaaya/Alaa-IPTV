package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
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

                Log.e(TAG, "==== AUTH REQUEST ====")
                Log.e(TAG, "SERVER: $cleanUrl")
                Log.e(TAG, "USER: $username")

                val api = ApiClient.getXtreamApiService(cleanUrl)
                val response = api.authenticate(username, password)

                Log.e(TAG, "AUTH HTTP: ${response.code()}")

                if (!response.isSuccessful) {
                    val error = response.errorBody()?.string()
                    Log.e(TAG, "AUTH ERROR: $error")
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: throw Exception("Empty body")

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

                Log.e(TAG, "====== LIVE CATEGORIES REQUEST ======")
                Log.e(TAG, "SERVER: ${prefs.serverUrl}")
                Log.e(TAG, "USER: ${prefs.username}")
                Log.e(TAG, "PASS: ${prefs.password}")

                if (prefs.serverUrl.isBlank()) {
                    Log.e(TAG, "SERVER URL EMPTY")
                    throw Exception("Server URL empty")
                }

                val api = ApiClient.getXtreamApiService(prefs.serverUrl)

                val response = api.getLiveCategories(
                    prefs.username,
                    prefs.password
                )

                Log.e(TAG, "HTTP CODE: ${response.code()}")
                Log.e(TAG, "IS SUCCESS: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    val errorText = response.errorBody()?.string()
                    Log.e(TAG, "ERROR BODY: $errorText")
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body()

                Log.e(TAG, "BODY NULL: ${body == null}")
                Log.e(TAG, "BODY SIZE: ${body?.size ?: 0}")

                body?.map {
                    Category(
                        categoryId = it.categoryId,
                        categoryName = it.categoryName,
                        parentId = it.parentId
                    )
                } ?: emptyList()
            }
        }

    // ================= LIVE STREAMS =================

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {

                Log.e(TAG, "====== LIVE STREAMS REQUEST ======")
                Log.e(TAG, "CATEGORY: $categoryId")

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

                Log.e(TAG, "HTTP CODE: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorText = response.errorBody()?.string()
                    Log.e(TAG, "ERROR BODY: $errorText")
                    throw Exception("HTTP ${response.code()}")
                }

                val body = response.body() ?: emptyList()

                Log.e(TAG, "STREAM COUNT: ${body.size}")

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

    // ================= STUBS =================

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()
    override suspend fun getFavorites() = emptyList<String>()
    override suspend fun isFavorite(itemId: String) = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}
    override suspend fun addFavorite(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
    override suspend fun removeFavorite(contentId: String) = Result.success(Unit)
    override suspend fun getFavoritesWithDetails() = Result.success(emptyList<FavoriteItem>())
    override suspend fun addRecent(itemId: String, itemType: String) {}
    override suspend fun getRecents() = emptyList<Recent>()
    override suspend fun addRecentView(contentId: String, name: String, type: String, icon: String?, categoryId: String?) = Result.success(Unit)
    override suspend fun getRecentViews() = Result.success(emptyList<RecentItem>())
    override suspend fun getMovieCategories() = Result.success(emptyList<Category>())
    override suspend fun getMovies(categoryId: String?) = Result.success(emptyList<Movie>())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()
    override suspend fun getSeriesCategories() = Result.success(emptyList<Category>())
    override suspend fun getSeries(categoryId: String?) = Result.success(emptyList<Series>())
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
    override suspend fun getEpgForChannel(channelId: String) = emptyList<EpgProgram>()
    override suspend fun getEpgForChannelInTimeRange(channelId: String, startTime: Long, endTime: Long) = emptyList<EpgProgram>()
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
