package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.alaa.iptv.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class IPTVRepository(
    private val context: Context
) {

    companion object {
        private const val TAG = "IPTVRepository"
    }

    // ================= HTTP CLIENT =================

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

    private val gson = Gson()

    // ================= MEMORY CACHE =================

    private var cachedChannels: List<Channel>? = null
    private var cachedCategories: List<Category>? = null
    private var cacheTime: Long = 0

    private val cacheLifetime = 5 * 60 * 1000L

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

    // ================= XTREAM =================

    suspend fun loadXtream(
        host: String,
        username: String,
        password: String
    ): List<Channel> =
        withContext(Dispatchers.IO) {

            val now = System.currentTimeMillis()

            if (cachedChannels != null && now - cacheTime < cacheLifetime) {
                return@withContext cachedChannels!!
            }

            try {

                val base = normalizeHost(host)

                val categoriesUrl =
                    "$base/player_api.php?username=$username&password=$password&action=get_live_categories"

                val streamsUrl =
                    "$base/player_api.php?username=$username&password=$password&action=get_live_streams"

                val categoriesBody = request(categoriesUrl)
                val streamsBody = request(streamsUrl)

                val categoriesArray = JSONArray(categoriesBody)
                val streamsArray = JSONArray(streamsBody)

                val categoryMap = mutableMapOf<String, String>()

                for (i in 0 until categoriesArray.length()) {

                    val obj = categoriesArray.getJSONObject(i)

                    val id = obj.optString("category_id")
                    val name = obj.optString("category_name")

                    categoryMap[id] = name
                }

                val channels = mutableListOf<Channel>()

                for (i in 0 until streamsArray.length()) {

                    val obj = streamsArray.getJSONObject(i)

                    val id = obj.optString("stream_id")
                    val name = obj.optString("name")
                    val icon = obj.optString("stream_icon")
                    val categoryId = obj.optString("category_id")

                    val direct = obj.optString("direct_source")

                    val url =
                        if (direct.isNotEmpty())
                            direct
                        else
                            "$base/live/$username/$password/$id"

                    channels.add(
                        Channel(
                            streamId = id,
                            num = "",
                            name = name,
                            streamType = "live",
                            streamIcon = icon,
                            epgChannelId = null,
                            added = null,
                            categoryId = categoryId,
                            categoryName = categoryMap[categoryId],
                            customSid = null,
                            tvArchive = 0,
                            directSource = url,
                            tvArchiveDuration = 0
                        )
                    )
                }

                cachedChannels = channels
                cacheTime = now

                channels

            } catch (e: Exception) {

                Log.e(TAG, "Xtream failed", e)

                emptyList()
            }
        }

    // ================= M3U =================

    suspend fun loadM3U(url: String): List<Channel> =
        withContext(Dispatchers.IO) {

            val now = System.currentTimeMillis()

            if (cachedChannels != null && now - cacheTime < cacheLifetime) {
                return@withContext cachedChannels!!
            }

            try {

                val body = request(url)

                val lines = body.split("\n")

                val channels = mutableListOf<Channel>()

                var name = ""
                var logo: String? = null
                var group: String? = null

                for (line in lines) {

                    if (line.startsWith("#EXTINF")) {

                        val nameMatch = Regex(",(.*)").find(line)
                        name = nameMatch?.groupValues?.get(1) ?: "Channel"

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

                cachedChannels = channels
                cacheTime = now

                channels

            } catch (e: Exception) {

                Log.e(TAG, "M3U failed", e)

                emptyList()
            }
        }

    // ================= UTIL =================

    private fun normalizeHost(host: String): String {

        var h = host.trim()

        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }

        return h.removeSuffix("/")
    }

}
