package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaRepository(
    private val prefs: AppPreferences,
    private val context: Context
) {

    companion object {
        private const val TAG = "MediaRepository"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .cache(Cache(File(context.cacheDir, "http_cache"), 25L * 1024 * 1024))
            .build()
    }

    // ================= REQUEST =================

    private suspend fun request(url: String): String =
        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "IPTV Smarters Pro")
                .header("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            response.body?.string() ?: ""
        }

    private fun normalizeHost(host: String): String {

        var h = host.trim()

        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }

        return h.removeSuffix("/")
    }

    // ================= LIVE CATEGORIES =================

    suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {

            try {

                val base = normalizeHost(prefs.serverUrl)

                val url =
                    "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_categories"

                val body = request(url)

                val array = JSONArray(body)

                val categories = mutableListOf<Category>()

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id")
                        )
                    )
                }

                Result.success(categories)

            } catch (e: Exception) {

                Log.e(TAG, "Categories error", e)

                Result.failure(e)
            }
        }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            try {

                val base = normalizeHost(prefs.serverUrl)

                val url =
                    if (categoryId.isNullOrBlank() || categoryId == "0") {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_streams"

                    } else {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_streams&category_id=$categoryId"
                    }

                val body = request(url)

                val array = JSONArray(body)

                val channels = mutableListOf<Channel>()

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    val id = obj.optString("stream_id")

                    val direct =
                        "$base/live/${prefs.username}/${prefs.password}/$id"

                    channels.add(
                        Channel(
                            streamId = id,
                            num = obj.optString("num"),
                            name = obj.optString("name"),
                            streamType = "live",
                            streamIcon = obj.optString("stream_icon"),
                            epgChannelId = obj.optString("epg_channel_id"),
                            added = obj.optString("added"),
                            categoryId = obj.optString("category_id"),
                            categoryName = null,
                            customSid = null,
                            tvArchive = obj.optInt("tv_archive"),
                            directSource = direct,
                            tvArchiveDuration = obj.optInt("tv_archive_duration")
                        )
                    )
                }

                Result.success(channels)

            } catch (e: Exception) {

                Log.e(TAG, "Streams error", e)

                Result.failure(e)
            }
        }

    // ================= MOVIES =================

    suspend fun getMovies(categoryId: String?): Result<List<Movie>> =
        withContext(Dispatchers.IO) {

            try {

                val base = normalizeHost(prefs.serverUrl)

                val url =
                    if (categoryId.isNullOrBlank() || categoryId == "0") {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_vod_streams"

                    } else {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_vod_streams&category_id=$categoryId"
                    }

                val body = request(url)

                val array = JSONArray(body)

                val movies = mutableListOf<Movie>()

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    movies.add(
                        Movie(
                            streamId = obj.optString("stream_id"),
                            name = obj.optString("name"),
                            streamIcon = obj.optString("stream_icon"),
                            rating = obj.optString("rating"),
                            year = obj.optString("year"),
                            plot = obj.optString("plot"),
                            cast = obj.optString("cast"),
                            director = obj.optString("director"),
                            genre = obj.optString("genre"),
                            releaseDate = obj.optString("releaseDate"),
                            durationSecs = null,
                            duration = obj.optString("duration"),
                            containerExtension = obj.optString("container_extension"),
                            categoryId = obj.optString("category_id"),
                            isFavorite = false
                        )
                    )
                }

                Result.success(movies)

            } catch (e: Exception) {

                Log.e(TAG, "Movies error", e)

                Result.failure(e)
            }
        }

    // ================= SERIES =================

    suspend fun getSeries(categoryId: String?): Result<List<Series>> =
        withContext(Dispatchers.IO) {

            try {

                val base = normalizeHost(prefs.serverUrl)

                val url =
                    if (categoryId.isNullOrBlank() || categoryId == "0") {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_series"

                    } else {

                        "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_series&category_id=$categoryId"
                    }

                val body = request(url)

                val array = JSONArray(body)

                val seriesList = mutableListOf<Series>()

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    seriesList.add(
                        Series(
                            seriesId = obj.optString("series_id"),
                            name = obj.optString("name"),
                            cover = obj.optString("cover"),
                            plot = obj.optString("plot"),
                            cast = obj.optString("cast"),
                            director = obj.optString("director"),
                            genre = obj.optString("genre"),
                            releaseDate = obj.optString("releaseDate"),
                            rating = obj.optString("rating"),
                            categoryId = obj.optString("category_id"),
                            isFavorite = false
                        )
                    )
                }

                Result.success(seriesList)

            } catch (e: Exception) {

                Log.e(TAG, "Series error", e)

                Result.failure(e)
            }
        }
}
