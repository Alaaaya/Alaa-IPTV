package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.domain.repository.IMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MediaRepository(
    private val prefs: AppPreferences,
    context: Context
) : IMediaRepository {

    companion object {
        private const val TAG = "MediaRepository"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    override suspend fun authenticate(
        serverUrl: String,
        username: String,
        password: String
    ): Result<XtreamAuthResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val cleanUrl = serverUrl.trim().removeSuffix("/")
                prefs.serverUrl = cleanUrl
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true
                XtreamAuthResponse(null, null)
            }
        }

    private suspend fun loadM3U(): List<Channel> =
        withContext(Dispatchers.IO) {

            val m3uUrl =
                "${prefs.serverUrl}/get.php?username=${prefs.username}&password=${prefs.password}&type=m3u_plus&output=ts"

            Log.e(TAG, "Loading M3U: $m3uUrl")

            val request = Request.Builder()
                .url(m3uUrl)
                .header("User-Agent", "VLC/3.0.18 LibVLC/3.0.18")
                .header("Accept", "*/*")
                .build()

            val response = client.newCall(request).execute()

            Log.e(TAG, "M3U HTTP CODE: ${response.code}")

            if (!response.isSuccessful) {
                throw Exception("M3U HTTP ${response.code}")
            }

            val body = response.body?.string() ?: ""

            parseM3U(body)
        }

    private fun parseM3U(content: String): List<Channel> {

        val channels = mutableListOf<Channel>()
        val lines = content.split("\n")

        var name = ""
        var logo: String? = null
        var group: String? = null

        for (line in lines) {

            if (line.startsWith("#EXTINF")) {

                val nameMatch = Regex(",(.*)").find(line)
                name = nameMatch?.groupValues?.get(1)?.trim() ?: "Channel"

                val logoMatch = Regex("""tvg-logo="(.*?)"""").find(line)
                logo = logoMatch?.groupValues?.get(1)

                val groupMatch = Regex("""group-title="(.*?)"""").find(line)
                group = groupMatch?.groupValues?.get(1)
            }

            if (line.startsWith("http")) {

                channels.add(
                    Channel(
                        streamId = line.hashCode().toString(),
                        num = "",
                        name = name,
                        streamType = "live",
                        streamIcon = logo,
                        epgChannelId = null,
                        added = null,
                        categoryId = group,
                        categoryName = group,
                        customSid = null,
                        tvArchive = 0,
                        directSource = line.trim(),
                        tvArchiveDuration = 0
                    )
                )
            }
        }

        return channels
    }

    override suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            runCatching {

                val channels = loadM3U()

                val groups = channels
                    .mapNotNull { it.categoryName }
                    .distinct()

                groups.map {
                    Category(
                        categoryId = it,
                        categoryName = it,
                        parentId = 0
                    )
                }
            }
        }

    override suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {

                val channels = loadM3U()

                if (categoryId == null) channels
                else channels.filter { it.categoryName == categoryId }
            }
        }

    override suspend fun getLiveStreamsFromCache(categoryId: String?) = emptyList<Channel>()
    override suspend fun getFavorites() = emptyList<String>()
    override suspend fun isFavorite(itemId: String) = false
    override suspend fun addFavorite(itemId: String) {}
    override suspend fun removeBasicFavorite(itemId: String) {}
}
