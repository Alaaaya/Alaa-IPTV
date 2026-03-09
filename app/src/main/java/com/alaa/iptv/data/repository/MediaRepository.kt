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

    private var cachedChannels: List<Channel>? = null
    private var cacheTime = 0L

    // ================= REQUEST =================

    private suspend fun request(url: String): String =
        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
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

    private fun isM3U(url: String): Boolean {
        return url.contains("get.php") || url.contains("m3u")
    }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            try {

                val server = prefs.serverUrl

                if (isM3U(server)) {
                    return@withContext loadM3U(server)
                }

                val base = normalizeHost(server)

                val url =
                    "$base/player_api.php?username=${prefs.username}&password=${prefs.password}&action=get_live_streams"

                val body = request(url)

                val array = JSONArray(body)

                val channels = mutableListOf<Channel>()

                for (i in 0 until array.length()) {

                    val obj = array.getJSONObject(i)

                    val id = obj.optString("stream_id")

                    val direct =
                        "$base/live/${prefs.username}/${prefs.password}/$id.m3u8"

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

    // ================= LOAD M3U =================

    suspend fun loadM3U(url: String): Result<List<Channel>> =
        withContext(Dispatchers.IO) {

            if (cachedChannels != null && System.currentTimeMillis() - cacheTime < 300000) {
                return@withContext Result.success(cachedChannels!!)
            }

            try {

                val body = request(url)

                val channels = mutableListOf<Channel>()

                var name = ""
                var logo: String? = null
                var group: String? = null

                for (line in body.lineSequence()) {

                    val l = line.trim()

                    if (l.startsWith("#EXTINF")) {

                        val nameMatch = Regex(",(.*)").find(l)
                        name = nameMatch?.groupValues?.get(1) ?: "Channel"

                        val logoMatch = Regex("""tvg-logo="(.*?)"""").find(l)
                        logo = logoMatch?.groupValues?.get(1)

                        val groupMatch = Regex("""group-title="(.*?)"""").find(l)
                        group = groupMatch?.groupValues?.get(1)
                    }

                    if (l.startsWith("http")) {

                        channels.add(
                            Channel(
                                streamId = l.hashCode().toString(),
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
                                directSource = l,
                                tvArchiveDuration = 0
                            )
                        )
                    }
                }

                cachedChannels = channels
                cacheTime = System.currentTimeMillis()

                Result.success(channels)

            } catch (e: Exception) {

                Log.e(TAG, "M3U error", e)

                Result.failure(e)
            }
        }
}
