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
    ): Result<XtreamAuthResponse> = withContext(Dispatchers.IO) {
        try {
            val service = ApiClient.getXtreamApiService(serverUrl)
            val response = service.authenticate(username, password)
            if (response.isSuccessful && response.body() != null)
                Result.success(response.body()!!)
            else
                Result.failure(Exception("Authentication failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== Favorites (basic) ====================

    override suspend fun getFavorites(): List<String> = withContext(Dispatchers.IO) {
        try {
            favoriteDao.getAllFavorites().map { it.itemId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addFavorite(itemId: String) = withContext(Dispatchers.IO) {
        try {
            val type = when {
                channelDao.getChannelById(itemId) != null -> "channel"
                movieDao.getMovieById(itemId) != null -> "movie"
                seriesDao.getSeriesById(itemId) != null -> "series"
                else -> "unknown"
            }
            favoriteDao.insertFavorite(FavoriteEntity(itemId = itemId, itemType = type))
        } catch (_: Exception) {
        }
    }

    /** ✅ الاسم الجديد */
    override suspend fun removeBasicFavorite(itemId: String) = withContext(Dispatchers.IO) {
        try {
            favoriteDao.deleteFavoriteById(itemId)
        } catch (_: Exception) {
        }
    }

    override suspend fun isFavorite(itemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            favoriteDao.isFavorite(itemId)
        } catch (e: Exception) {
            false
        }
    }

    // ==================== Favorites (extended) ====================

    override suspend fun addFavorite(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    itemId = contentId,
                    itemType = type,
                    name = name,
                    icon = icon,
                    categoryId = categoryId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFavorite(contentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                favoriteDao.deleteFavoriteById(contentId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getFavoritesWithDetails(): Result<List<FavoriteItem>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(favoriteDao.getAllFavorites().map { it.toFavoriteItem() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ==================== Recent ====================

    override suspend fun addRecent(itemId: String, itemType: String) =
        withContext(Dispatchers.IO) {
            try {
                recentDao.insertRecent(RecentEntity(itemId = itemId, itemType = itemType))
            } catch (_: Exception) {
            }
        }

    override suspend fun getRecents(): List<Recent> = withContext(Dispatchers.IO) {
        try {
            recentDao.getAllRecents().map { it.toRecent() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addRecentView(
        contentId: String,
        name: String,
        type: String,
        icon: String?,
        categoryId: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            recentDao.insertRecent(
                RecentEntity(
                    itemId = contentId,
                    itemType = type,
                    name = name,
                    icon = icon,
                    categoryId = categoryId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRecentViews(): Result<List<RecentItem>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(recentDao.getAllRecents().map { it.toRecentItem() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 🔹 باقي الملف (Live / Movies / Series / EPG / Search / Sync)
    // 🔹 لا يحتاج أي تعديل – الكود تبعك صحيح 100٪
}
