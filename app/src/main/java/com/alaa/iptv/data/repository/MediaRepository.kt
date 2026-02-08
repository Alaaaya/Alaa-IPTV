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

    private val api by lazy {
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
                val response = api.authenticate(username, password)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ================= LIVE TV =================

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getLiveCategories(prefs.username, prefs.password)
                if (res.isSuccessful && res.body() != null) {
                    Result.success(
                        res.body()!!.map {
                            Category(
                                categoryId = it.categoryId,
                                categoryName = it.categoryName,
                                parentId = it.parentId ?: 0
                            )
                        }
                    )
                } else {
                    Result.failure(Exception("No live categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            try {
                val res = api.getLiveStreams(prefs.username, prefs.password)
                if (res.isSuccessful && res.body() != null) {
                    val channels = res.body()!!
                        .filter { categoryId == null || it.categoryId == categoryId }
                        .map {
                            Channel(
                                streamId = it.streamId.toString(),
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
                                directSource = it.directSource,
                                tvArchiveDuration = it.tvArchiveDuration ?: 0,
                                isFavorite = false
                            )
                        }

                    Result.success(channels)
                } else {
                    Result.failure(Exception("No channels"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()

    // ================= STUBS =================
    override suspend fun getMovieCategories() = Result.success(emptyList())
    override suspend fun getMovies(categoryId: String?) = Result.success(emptyList())
    override suspend fun getMoviesFromCache(categoryId: String?) = emptyList<Movie>()
    override suspend fun getSeriesCategories() = Result.success(emptyList())
    override suspend fun getSeries(categoryId: String?) = Result.success(emptyList())
    override suspend fun getSeriesFromCache(categoryId: String?) = emptyList<Series>()
    override suspend fun getSeriesInfo(seriesId: String) = Result.success(emptyList<Episode>())
    override suspend fun getEpisodesFromCache(seriesId: String) = emptyList<Episode>()
}
