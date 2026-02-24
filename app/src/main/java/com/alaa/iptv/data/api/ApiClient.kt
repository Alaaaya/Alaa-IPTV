package com.alaa.iptv.data.api

import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null
    private var workingConfig: Pair<String, String>? = null // (URL, UserAgent)

    // ✅ قائمة User-Agents للتجربة
    private val userAgents = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "VLC/3.0.18 LibVLC/3.0.18",
        "IPTV Smarters/1.0",
        "okhttp/4.12.0"
    )

    private val cookieJar = object : CookieJar {
        private val cookieStore = HashMap<HttpUrl, List<Cookie>>()
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url] = cookies
        }
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url] ?: emptyList()
        }
    }

    // ✅ Client سريع للاختبار
    private fun buildFastClient(userAgent: String): OkHttpClient {
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Accept", "application/json, */*")
                    .build()
                chain.proceed(request)
            }
            .followRedirects(true)
            .connectTimeout(5, TimeUnit.SECONDS) // سريع
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    // ✅ Client كامل للاستخدام
    private fun buildClient(userAgent: String): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Accept", "application/json, text/html, */*")
                    .addHeader("Accept-Encoding", "gzip")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(ErrorHandlerInterceptor())
            .followRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ✅ معالج الأخطاء الذكي
    private class ErrorHandlerInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response = chain.proceed(request)
            
            // معالجة خطأ 511
            if (response.code == 511) {
                response.close()
                // أعد المحاولة مع GET نظيف
                request = request.newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", "VLC/3.0.18 LibVLC/3.0.18")
                    .build()
                response = chain.proceed(request)
            }
            
            return response
        }
    }

    // ✅ تنفيذ ذكي مع Fallback
    suspend fun <T> executeSmart(
        baseUrl: String,
        call: suspend (XtreamApiService) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        
        // استخدم الإعداد المحفوظ إن وجد
        workingConfig?.let { (savedUrl, savedAgent) ->
            try {
                val service = createService(savedUrl, buildClient(savedAgent))
                val result = call(service)
                return@withContext Result.success(result)
            } catch (e: Exception) {
                // تجاهل واستمر
            }
        }

        val cleanUrl = normalizeBaseUrl(baseUrl)
        val urlsToTry = listOf(
            cleanUrl,
            cleanUrl.replace("http://", "https://")
        ).distinct()

        // جرب كل URL مع أول User-Agent (سريع)
        for (url in urlsToTry) {
            try {
                val service = createService(url, buildFastClient(userAgents[0]))
                val result = call(service)
                
                // ✅ حفظ الإعداد الناجح
                workingConfig = url to userAgents[0]
                
                return@withContext Result.success(result)
            } catch (e: Exception) {
                continue
            }
        }

        // إذا فشل الكل، جرب User-Agents أخرى
        return@withContext tryAllCombinations(baseUrl, call)
    }

    private suspend fun <T> tryAllCombinations(
        baseUrl: String,
        call: suspend (XtreamApiService) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        
        val urls = listOf(
            normalizeBaseUrl(baseUrl),
            normalizeBaseUrl(baseUrl).replace("http://", "https://")
        )

        for (url in urls) {
            for (agent in userAgents) {
                try {
                    val service = createService(url, buildClient(agent))
                    val result = call(service)
                    
                    workingConfig = url to agent
                    return@withContext Result.success(result)
                    
                } catch (e: Exception) {
                    continue
                }
            }
        }

        Result.failure(Exception("فشل الاتصال بجميع الطرق"))
    }

    private fun createService(baseUrl: String, client: OkHttpClient): XtreamApiService {
        return Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApiService::class.java)
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        var cleanUrl = baseUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        return cleanUrl.removeSuffix("/") + "/"
    }

    fun getXtreamApiService(baseUrl: String): XtreamApiService {
        val (url, agent) = workingConfig ?: (baseUrl to userAgents[0])
        return createService(url, buildClient(agent))
    }
}
