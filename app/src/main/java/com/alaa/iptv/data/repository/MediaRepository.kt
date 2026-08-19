package com.alaa.iptv.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.alaa.iptv.BuildConfig
import com.alaa.iptv.data.local.PersistentLiveCatalog
import com.alaa.iptv.data.models.*
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.remote.TvProvisioningClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class SubscriptionSessionExpiredException(message: String) : IOException(message)

class MediaRepository(
    private val prefs: AppPreferences,
    private val context: Context,
    private val responseOverride: (suspend (String) -> String)? = null
) {

    companion object {
        private const val TAG = "MediaRepository"
        private const val CACHE_DURATION = 5 * 60 * 1000L // 5 دقائق
        private const val CONTENT_PAGE_SIZE = 120
        private const val LIVE_PAGE_SIZE = 100
        private const val MAX_LIVE_PAGE_CACHES = 18
        private const val MAX_MOVIE_CATEGORY_CACHES = 12
        private const val MAX_SERIES_CATEGORY_CACHES = 12
        private const val MAX_CATEGORY_CACHES = 6

        private val cacheLock = Any()
        private data class M3UCacheEntry(
            val sourceKey: String,
            val savedAt: Long,
            val channels: List<Channel>,
            val channelsByCategory: Map<String, List<Channel>>
        )

        private var cachedM3UPlaylist: M3UCacheEntry? = null
        private val cachedChannelPages = ConcurrentHashMap<String, Pair<Long, PagedContent<Channel>>>()
        private val cachedLiveCategories = ConcurrentHashMap<String, Pair<Long, List<Category>>>()
        private val cachedMovieCategories = ConcurrentHashMap<String, Pair<Long, List<Category>>>()
        private val cachedSeriesCategories = ConcurrentHashMap<String, Pair<Long, List<Category>>>()
        private val cachedMoviesByCategory = ConcurrentHashMap<String, Pair<Long, PagedContent<Movie>>>()
        private val cachedSeriesByCategory = ConcurrentHashMap<String, Pair<Long, PagedContent<Series>>>()
        private val cachedEpisodesBySeries = ConcurrentHashMap<String, Pair<Long, List<Episode>>>()

        private fun <T> getCachedValue(
            cache: Map<String, Pair<Long, T>>,
            key: String
        ): T? = synchronized(cacheLock) {
            cache[key]?.takeIf { (savedAt, _) -> System.currentTimeMillis() - savedAt < CACHE_DURATION }?.second
        }

        private fun <T> putBoundedCache(
            cache: MutableMap<String, Pair<Long, T>>,
            key: String,
            value: T,
            maxEntries: Int
        ) = synchronized(cacheLock) {
            if (key !in cache && cache.size >= maxEntries) {
                cache.entries.iterator().let { iterator ->
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
            }
            cache[key] = System.currentTimeMillis() to value
        }
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

    private fun persistentLiveCatalogOrNull(): PersistentLiveCatalog? = runCatching {
        if (responseOverride == null) PersistentLiveCatalog(context, sourceCacheKey()) else null
    }.getOrNull()

    // ================= CACHE =================

    // ================= HELPERS =================

    private suspend fun ensureContentAccess(): Result<Unit> = try {
        if (prefs.isControlPlaneEnrolled && prefs.shouldRefreshControlPlane()) {
            prefs.markControlPlaneRefreshAttempt()
            TvProvisioningClient.syncControlPlane(prefs.getOrCreateTvId(), BuildConfig.VERSION_NAME)
                .onSuccess { prefs.applyControlPlaneSnapshot(it) }
        }
        if (prefs.isDeviceAccessBlocked()) {
            throw IOException("هذا الجهاز موقوف من لوحة التحكم")
        }
        Result.success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }

    private suspend fun request(url: String): String =
        withContext(Dispatchers.IO) {
            ensureContentAccess().getOrThrow()
            responseOverride?.invoke(url)?.let { return@withContext it }
            Log.d(TAG, "Requesting IPTV endpoint")
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 401 || response.code == 403) {
                        prefs.isLoggedIn = false
                        throw SubscriptionSessionExpiredException("انتهت صلاحية جلسة الاشتراك أو رُفض الوصول")
                    }
                    throw IOException("HTTP ${response.code}")
                }
                response.body?.string() ?: throw IOException("Empty response body")
            }
        }

    private suspend fun readPlaylist(url: String): String =
        withContext(Dispatchers.IO) {
            if (url.startsWith("file:", ignoreCase = true)) {
                val file = File(java.net.URI(url))
                if (!file.exists() || !file.isFile) throw IOException("ملف القائمة غير موجود")
                file.readText()
            } else {
                request(url)
            }
        }

    private fun normalizeHost(host: String): String {
        var h = host.trim().substringBefore("?").removeSuffix("/")
        if (!h.startsWith("http://") && !h.startsWith("https://")) {
            h = "http://$h"
        }
        return h.removeSuffix("/player_api.php").removeSuffix("/")
    }

    fun isM3U(url: String = prefs.serverUrl): Boolean {
        return (url == prefs.serverUrl && prefs.useM3U) || IptvSourceClassifier.isM3U(url)
    }

    private fun buildApiUrl(action: String, extra: String = ""): String {
        val base = normalizeHost(prefs.serverUrl)
        val username = encodeQueryParameter(prefs.username)
        val password = encodeQueryParameter(prefs.password)
        return "$base/player_api.php?username=$username&password=$password&action=$action$extra"
    }

    private fun encodeQueryParameter(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private fun JsonObject.stringValue(name: String): String =
        get(name)?.takeUnless { it.isJsonNull }?.asString.orEmpty()

    private fun JsonObject.intValue(name: String): Int =
        get(name)?.takeUnless { it.isJsonNull }?.asInt ?: 0

    /** مفتاح داخلي غير قابل للعرض أو التسجيل يمنع اختلاط كاشات الاشتراكات المختلفة. */
    private fun sourceCacheKey(): String {
        val source = if (isM3U()) {
            "m3u:${prefs.serverUrl.trim()}"
        } else {
            "xtream:${normalizeHost(prefs.serverUrl)}:${prefs.username}:${prefs.password}"
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies an Xtream account before credentials are persisted. For M3U playlists,
     * it verifies that the remote resource is reachable and contains a valid playlist header.
     */
    suspend fun validateLogin(
        serverUrl: String,
        username: String,
        password: String,
        forceM3U: Boolean = false
    ): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                if (forceM3U || IptvSourceClassifier.isM3U(serverUrl)) {
                    val playlist = readPlaylist(serverUrl)
                    if (playlist.contains("#EXTM3U")) {
                        Result.success(Unit)
                    } else {
                        Result.failure(IOException("الرابط لا يحتوي على قائمة M3U صالحة"))
                    }
                } else {
                    if (username.isBlank() || password.isBlank()) {
                        return@withContext Result.failure(IllegalArgumentException("اسم المستخدم وكلمة المرور مطلوبان"))
                    }
                    val base = normalizeHost(serverUrl)
                    val url = "$base/player_api.php?username=${encodeQueryParameter(username)}&password=${encodeQueryParameter(password)}"
                    val response = JSONObject(request(url))
                    val userInfo = response.optJSONObject("user_info")
                    val authenticated = userInfo?.optInt("auth", 0) == 1
                    if (authenticated) {
                        Result.success(Unit)
                    } else {
                        val message = userInfo?.optString("message")?.takeIf { it.isNotBlank() }
                            ?: "تعذر التحقق من بيانات الدخول"
                        Result.failure(IOException(message))
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "Login validation failed", error)
                Result.failure(error)
            }
        }

    // ================= LIVE STREAMS =================

    suspend fun getLiveStreams(categoryId: String?, page: Int = 0): Result<List<Channel>> =
        getLiveContentPage(categoryId, page).map { it.items }

    suspend fun getPersistedLiveCategories(): List<Category> = withContext(Dispatchers.IO) {
        persistentLiveCatalogOrNull()?.categories().orEmpty()
    }

    suspend fun getPersistedLiveContentPage(categoryId: String, page: Int): PagedContent<Channel>? =
        withContext(Dispatchers.IO) {
            persistentLiveCatalogOrNull()?.page(categoryId, page.coerceAtLeast(0), LIVE_PAGE_SIZE)
        }

    /**
     * تعيد الصفحة المطلوبة مع العدد الحقيقي للعناصر في الفئة من الاستجابة نفسها.
     * لا تُحمّل فئات أخرى ولا تنشئ طلباً إضافياً لغرض العد فقط.
     */
    suspend fun getLiveContentPage(categoryId: String?, page: Int = 0): Result<PagedContent<Channel>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }

            val effectiveCategory = categoryId
                ?.trim()
                ?.takeIf { it.isNotEmpty() && !it.equals("all", ignoreCase = true) }

            val pageIndex = page.coerceAtLeast(0)
            val pageCacheKey = "${sourceCacheKey()}:live:${effectiveCategory ?: "all"}:$pageIndex"
            getCachedValue(cachedChannelPages, pageCacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }

            try {
                if (isM3U()) {
                    return@withContext loadM3USnapshot(prefs.serverUrl).map { snapshot ->
                        val channels = snapshot.channels
                        val scoped = effectiveCategory?.let { selectedId ->
                            snapshot.channelsByCategory[selectedId].orEmpty()
                        } ?: channels
                        effectiveCategory?.let { selectedId ->
                            persistentLiveCatalogOrNull()?.replaceCategoryChannels(selectedId, scoped)
                        }
                        val bounds = ContentPagingPolicy.bounds(scoped.size, pageIndex, LIVE_PAGE_SIZE)
                        PagedContent(
                            items = scoped.subList(bounds.startIndex, bounds.endIndex),
                            totalCount = scoped.size,
                            hasMore = bounds.hasMore
                        )
                    }
                }

                val categoryExtra = effectiveCategory?.let { "&category_id=${encodeQueryParameter(it)}" }.orEmpty()
                val url = buildApiUrl("get_live_streams", categoryExtra)
                val body = request(url)
                val array = JsonParser().parse(body).asJsonArray
                val base = normalizeHost(prefs.serverUrl)

                // بعض مزودي Xtream قد يعيدون عناصر من فئات أخرى رغم تمرير category_id.
                // نتحقق من الاستجابة نفسها كي لا تظهر قناة في عمود فئة غير فئتها المختارة.
                val scopedObjects = buildList {
                    for (i in 0 until array.size()) {
                        val obj = array.get(i).takeIf { element -> element.isJsonObject }?.asJsonObject ?: continue
                        if (effectiveCategory == null || obj.stringValue("category_id") == effectiveCategory) {
                            add(obj)
                        }
                    }
                }
                val channels = mutableListOf<Channel>()
                val bounds = ContentPagingPolicy.bounds(scopedObjects.size, pageIndex, LIVE_PAGE_SIZE)
                for (obj in scopedObjects) {
                    val id = obj.stringValue("stream_id")
                    if (id.isBlank()) continue
                    val providerSource = obj.stringValue("direct_source")
                        .trim()
                        .takeIf { it.startsWith("http://", true) || it.startsWith("https://", true) }
                    // نفضّل رابط المزود إن وجد، وإلا MPEG-TS المباشر. بعض المزودين يعيدون
                    // توجيه .m3u8 إلى TS مستمر، ما يسبب فشل محلل HLS في Media3.
                    val direct = providerSource ?: StreamUrlFactory.live(
                        base,
                        prefs.username,
                        prefs.password,
                        id
                    )

                    channels.add(
                        Channel(
                            streamId = id,
                            num = obj.stringValue("num"),
                            name = obj.stringValue("name"),
                            streamType = "live",
                            streamIcon = obj.stringValue("stream_icon"),
                            epgChannelId = obj.stringValue("epg_channel_id"),
                            added = obj.stringValue("added"),
                            categoryId = obj.stringValue("category_id"),
                            categoryName = null,
                            customSid = null,
                            tvArchive = obj.intValue("tv_archive"),
                            directSource = direct,
                            tvArchiveDuration = obj.intValue("tv_archive_duration")
                        )
                    )
                }

                effectiveCategory?.let { selectedId ->
                    persistentLiveCatalogOrNull()?.replaceCategoryChannels(selectedId, channels)
                }

                // حفظ في الكاش مع إجمالي الفئة بعد التحقق الدفاعي من استجابة المصدر.
                val contentPage = PagedContent(
                    items = channels.subList(bounds.startIndex, bounds.endIndex),
                    totalCount = scopedObjects.size,
                    hasMore = bounds.hasMore
                )
                putBoundedCache(cachedChannelPages, pageCacheKey, contentPage, MAX_LIVE_PAGE_CACHES)

                Result.success(contentPage)

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getLiveStreams error", e)
                Result.failure(e)
            }
        }

    // ================= LIVE CATEGORIES =================

    suspend fun getLiveCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }

            if (isM3U()) {
                return@withContext loadM3USnapshot(prefs.serverUrl).map { snapshot ->
                    M3UCategoryMapper.categories(snapshot.channels).also { categories ->
                        persistentLiveCatalogOrNull()?.replaceCategories(categories)
                    }
                }
            }

            val cacheKey = sourceCacheKey()
            getCachedValue(cachedLiveCategories, cacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }

            try {
                val url = buildApiUrl("get_live_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }

                putBoundedCache(cachedLiveCategories, cacheKey, categories, MAX_CATEGORY_CACHES)

                Result.success(categories)

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getLiveCategories error", e)
                Result.failure(e)
            }
        }

    // ================= VOD CATEGORIES =================

    suspend fun getMovieCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }
            if (isM3U()) {
                return@withContext Result.failure(IOException("قوائم M3U لا تدعم محتوى الأفلام والمسلسلات"))
            }
            val cacheKey = sourceCacheKey()
            getCachedValue(cachedMovieCategories, cacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }
            try {
                val url = buildApiUrl("get_vod_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }
                putBoundedCache(cachedMovieCategories, cacheKey, categories, MAX_CATEGORY_CACHES)
                Result.success(categories)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getMovieCategories error", e)
                Result.failure(e)
            }
        }

    // ================= SERIES CATEGORIES =================

    suspend fun getSeriesCategories(): Result<List<Category>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }
            if (isM3U()) {
                return@withContext Result.failure(IOException("قوائم M3U لا تدعم محتوى الأفلام والمسلسلات"))
            }
            val cacheKey = sourceCacheKey()
            getCachedValue(cachedSeriesCategories, cacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }
            try {
                val url = buildApiUrl("get_series_categories")
                val body = request(url)
                val array = JSONArray(body)

                val categories = mutableListOf<Category>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    categories.add(
                        Category(
                            categoryId = obj.optString("category_id"),
                            categoryName = obj.optString("category_name"),
                            parentId = obj.optInt("parent_id", 0)
                        )
                    )
                }
                putBoundedCache(cachedSeriesCategories, cacheKey, categories, MAX_CATEGORY_CACHES)
                Result.success(categories)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getSeriesCategories error", e)
                Result.failure(e)
            }
        }

    // ================= MOVIES (VOD) =================

    suspend fun getMovies(categoryId: String?, page: Int = 0): Result<List<Movie>> =
        getMovieContentPage(categoryId, page).map { it.items }

    suspend fun getMovieContentPage(categoryId: String?, page: Int = 0): Result<PagedContent<Movie>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }
            if (isM3U()) {
                return@withContext Result.failure(IOException("قوائم M3U لا تدعم محتوى الأفلام والمسلسلات"))
            }
            val effectiveCategory = categoryId?.takeIf { it.isNotBlank() && it != "all" }
            val pageIndex = page.coerceAtLeast(0)
            val cacheKey = "${sourceCacheKey()}:movie:${effectiveCategory ?: "all"}:$pageIndex"
            getCachedValue(cachedMoviesByCategory, cacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }

            try {
                val contentPage = fetchMovies(effectiveCategory, pageIndex)
                putBoundedCache(
                    cachedMoviesByCategory,
                    cacheKey,
                    contentPage,
                    MAX_MOVIE_CATEGORY_CACHES
                )
                Result.success(contentPage)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getMovies error", e)
                Result.failure(e)
            }
        }

    private suspend fun fetchMovies(categoryId: String?, page: Int = 0): PagedContent<Movie> {
        val extra = if (!categoryId.isNullOrBlank() && categoryId != "all") {
            "&category_id=${encodeQueryParameter(categoryId)}"
        } else {
            ""
        }
        val array = JSONArray(request(buildApiUrl("get_vod_streams", extra)))
        val bounds = ContentPagingPolicy.bounds(array.length(), page, CONTENT_PAGE_SIZE)
        val movies = mutableListOf<Movie>()
        for (index in bounds.startIndex until bounds.endIndex) {
            val obj = array.optJSONObject(index) ?: continue
            val streamId = obj.optString("stream_id")
            if (streamId.isBlank()) continue
            movies += Movie(
                streamId = streamId, name = obj.optString("name"), streamIcon = obj.optString("stream_icon"),
                rating = obj.optString("rating"), year = obj.optString("year", ""), plot = obj.optString("plot", ""),
                cast = obj.optString("cast", ""), director = obj.optString("director", ""), genre = obj.optString("genre", ""),
                releaseDate = obj.optString("release_date", ""), durationSecs = obj.optString("duration_secs", ""),
                duration = obj.optString("duration", ""), categoryId = obj.optString("category_id"),
                containerExtension = obj.optString("container_extension", "mp4"), isFavorite = false
            )
        }
        return PagedContent(
            items = movies,
            totalCount = array.length(),
            hasMore = bounds.hasMore
        )
    }

    private suspend fun fetchFeaturedMovies(): List<Movie> {
        val categories = getMovieCategories().getOrDefault(emptyList())
        val featured = linkedMapOf<String, Movie>()
        for (category in categories) {
            if (featured.size >= 500) break
            val page = try {
                fetchMovies(category.categoryId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w(TAG, "Skipping unavailable movie category")
                continue
            }
            page.items.forEach { movie ->
                if (featured.size < 500 && !featured.containsKey(movie.streamId)) {
                    featured[movie.streamId] = movie
                }
            }
        }
        return featured.values.toList()
    }

    // ================= SERIES =================

    suspend fun getSeries(categoryId: String?, page: Int = 0): Result<List<Series>> =
        getSeriesContentPage(categoryId, page).map { it.items }

    suspend fun getSeriesContentPage(categoryId: String?, page: Int = 0): Result<PagedContent<Series>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }
            if (isM3U()) {
                return@withContext Result.failure(IOException("قوائم M3U لا تدعم محتوى الأفلام والمسلسلات"))
            }
            val effectiveCategory = categoryId?.takeIf { it.isNotBlank() && it != "all" }
            val pageIndex = page.coerceAtLeast(0)
            val cacheKey = "${sourceCacheKey()}:series:${effectiveCategory ?: "all"}:$pageIndex"
            getCachedValue(cachedSeriesByCategory, cacheKey)?.let { cached ->
                return@withContext Result.success(cached)
            }

            try {
                val contentPage = fetchSeries(effectiveCategory, pageIndex)
                putBoundedCache(
                    cachedSeriesByCategory,
                    cacheKey,
                    contentPage,
                    MAX_SERIES_CATEGORY_CACHES
                )
                Result.success(contentPage)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "getSeries error", e)
                Result.failure(e)
            }
        }

    private suspend fun fetchSeries(categoryId: String?, page: Int = 0): PagedContent<Series> {
        val extra = if (!categoryId.isNullOrBlank() && categoryId != "all") {
            "&category_id=${encodeQueryParameter(categoryId)}"
        } else {
            ""
        }
        val array = JSONArray(request(buildApiUrl("get_series", extra)))
        val bounds = ContentPagingPolicy.bounds(array.length(), page, CONTENT_PAGE_SIZE)
        val series = mutableListOf<Series>()
        for (index in bounds.startIndex until bounds.endIndex) {
            val obj = array.optJSONObject(index) ?: continue
            val seriesId = obj.optString("series_id")
            if (seriesId.isBlank()) continue
            series += Series(
                seriesId = seriesId, name = obj.optString("name"), cover = obj.optString("cover"),
                plot = obj.optString("plot"), cast = obj.optString("cast", ""), director = obj.optString("director", ""),
                genre = obj.optString("genre"), releaseDate = obj.optString("release_date", ""),
                categoryId = obj.optString("category_id"), rating = obj.optString("rating"), isFavorite = false
            )
        }
        return PagedContent(
            items = series,
            totalCount = array.length(),
            hasMore = bounds.hasMore
        )
    }

    private suspend fun fetchFeaturedSeries(): List<Series> {
        val categories = getSeriesCategories().getOrDefault(emptyList())
        val featured = linkedMapOf<String, Series>()
        for (category in categories) {
            if (featured.size >= 500) break
            val page = try {
                fetchSeries(category.categoryId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w(TAG, "Skipping unavailable series category")
                continue
            }
            page.items.forEach { series ->
                if (featured.size < 500 && !featured.containsKey(series.seriesId)) {
                    featured[series.seriesId] = series
                }
            }
        }
        return featured.values.toList()
    }

    // ================= SERIES EPISODES =================

    suspend fun getSeriesEpisodes(seriesId: String): Result<List<Episode>> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }
            try {
                if (isM3U()) {
                    return@withContext Result.failure(IOException("قوائم M3U لا تحتوي على بيانات حلقات المسلسلات"))
                }
                val cacheKey = "${sourceCacheKey()}:episodes:$seriesId"
                getCachedValue(cachedEpisodesBySeries, cacheKey)?.let { cached ->
                    return@withContext Result.success(cached)
                }
                val url = buildApiUrl("get_series_info", "&series_id=${encodeQueryParameter(seriesId)}")
                val body = request(url)
                val episodesObject = JSONObject(body).optJSONObject("episodes")
                    ?: return@withContext Result.success(emptyList())

                val episodes = mutableListOf<Episode>()
                val seasons = episodesObject.keys()
                while (seasons.hasNext()) {
                    val seasonKey = seasons.next()
                    val seasonNumber = seasonKey.toIntOrNull() ?: 0
                    val array = episodesObject.optJSONArray(seasonKey) ?: continue
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val info = item.optJSONObject("info")
                        episodes += Episode(
                            id = item.optString("id"),
                            episodeNum = item.optInt("episode_num", index + 1),
                            title = item.optString("title").ifBlank { "الحلقة ${index + 1}" },
                            containerExtension = item.optString("container_extension", "mp4"),
                            info = EpisodeInfo(
                                plot = info?.optString("plot")?.takeIf { it.isNotBlank() },
                                duration = info?.optString("duration")?.takeIf { it.isNotBlank() },
                                rating = info?.optString("rating")?.takeIf { it.isNotBlank() }
                            ),
                            seasonNumber = seasonNumber
                        )
                    }
                }
                val sortedEpisodes = episodes.sortedWith(compareBy<Episode> { it.seasonNumber }.thenBy { it.episodeNum })
                putBoundedCache(cachedEpisodesBySeries, cacheKey, sortedEpisodes, MAX_SERIES_CATEGORY_CACHES)
                Result.success(sortedEpisodes)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "getSeriesEpisodes error", error)
                Result.failure(error)
            }
        }

    // ================= LOAD M3U =================

    suspend fun loadM3U(url: String): Result<List<Channel>> =
        loadM3USnapshot(url).map { it.channels }

    /**
     * يحلل M3U مرة واحدة في خيط IO ويحفظ فهرس الفئات بنفس مراجع القنوات،
     * لذلك لا تنفذ الشاشة فلترة O(n) عند كل انتقال بين الفئات.
     */
    private suspend fun loadM3USnapshot(url: String): Result<M3UCacheEntry> =
        withContext(Dispatchers.IO) {
            ensureContentAccess().exceptionOrNull()?.let { return@withContext Result.failure(it) }

            val sourceKey = sourceCacheKey()
            synchronized(cacheLock) {
                    cachedM3UPlaylist
                        ?.takeIf { it.sourceKey == sourceKey && System.currentTimeMillis() - it.savedAt < CACHE_DURATION }
            }?.let { cached -> return@withContext Result.success(cached) }

            try {
                val body = readPlaylist(url)
                val channels = mutableListOf<Channel>()
                var name = "Channel"
                var logo: String? = null
                var group: String? = null
                var epgChannelId: String? = null

                for (line in body.lineSequence()) {
                    val l = line.trim()

                    if (l.startsWith("#EXTINF")) {
                        name = Regex(",(.+)$").find(l)?.groupValues?.get(1)?.trim() ?: "Channel"
                        epgChannelId = Regex("tvg-id=\"([^\"]*?)\"").find(l)?.groupValues?.get(1)
                        logo = Regex("tvg-logo=\"([^\"]*?)\"").find(l)?.groupValues?.get(1)
                        group = Regex("group-title=\"([^\"]*?)\"").find(l)?.groupValues?.get(1)
                    }

                    if (l.startsWith("http://") || l.startsWith("https://") || l.startsWith("rtmp")) {
                        channels.add(
                            Channel(
                                streamId = l.hashCode().toString(),
                                num = "",
                                name = name,
                                streamType = "live",
                                streamIcon = logo,
                                epgChannelId = epgChannelId,
                                added = null,
                                categoryId = group ?: "Uncategorized",
                                categoryName = group ?: "Uncategorized",
                                customSid = null,
                                tvArchive = 0,
                                directSource = l,
                                tvArchiveDuration = 0
                            )
                        )
                        name = "Channel"
                        logo = null
                        group = null
                        epgChannelId = null
                    }
                }

                val index = channels.groupBy { it.categoryId?.ifBlank { "Uncategorized" } ?: "Uncategorized" }
                val snapshot = M3UCacheEntry(
                    sourceKey = sourceKey,
                    savedAt = System.currentTimeMillis(),
                    channels = channels,
                    channelsByCategory = index
                )
                synchronized(cacheLock) { cachedM3UPlaylist = snapshot }

                Result.success(snapshot)

            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                Log.e(TAG, "loadM3U error", e)
                Result.failure(e)
            }
        }

    // ================= CLEAR CACHE =================

    fun clearCache() {
        synchronized(cacheLock) {
            cachedM3UPlaylist = null
            cachedChannelPages.clear()
            cachedLiveCategories.clear()
            cachedMovieCategories.clear()
            cachedSeriesCategories.clear()
            cachedMoviesByCategory.clear()
            cachedSeriesByCategory.clear()
            cachedEpisodesBySeries.clear()
        }
    }
}
