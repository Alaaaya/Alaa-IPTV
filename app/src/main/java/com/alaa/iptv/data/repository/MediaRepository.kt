package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

class MediaRepository(
    private val prefs: AppPreferences,
    private val context: Context
) {

    companion object {
        private const val TAG = "MediaRepository"
    }

    // ================= HTTP CLIENT =================

    private val client: OkHttpClient by lazy {

        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
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

    // ================= UTIL =================

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

            runCatching {

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

                categories
            }
        }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            runCatching {

                val base = normalizeHost(prefs.serverUrl)

                val url =
                    if (categoryId == null || categoryId == "0") {
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

                    channels.add(
                        Channel(
                            streamId = id,
                            num = obj.optString("num"),
                            name = obj.optString("name"),
                            streamType = obj.optString("stream_type"),
                            streamIcon = obj.optString("stream_icon"),
                            epgChannelId = obj.optString("epg_channel_id"),
                            added = obj.optString("added"),
                            categoryId = obj.optString("category_id"),
                            categoryName = null,
                            customSid = obj.optString("custom_sid"),
                            tvArchive = obj.optInt("tv_archive"),
                            directSource = "$base/live/${prefs.username}/${prefs.password}/$id",
                            tvArchiveDuration = obj.optInt("tv_archive_duration")
                        )
                    )
                }

                channels
            }
        }

    // ================= MOVIES =================

    suspend fun getMovies(categoryId: String?): Result<List<Movie>> {

        return Result.success(emptyList())
    }

    // ================= SERIES =================

    suspend fun getSeries(categoryId: String?): Result<List<Series>> {

        return Result.success(emptyList())
    }
}
