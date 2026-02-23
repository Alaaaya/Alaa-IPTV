package com.alaa.iptv.data.api

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    private val cookieJar = object : CookieJar {
        private val cookieStore = HashMap<HttpUrl, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url] ?: emptyList()
        }
    }

    private fun buildClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // ✅ User-Agent صحيح (Chrome حقيقي)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .addHeader("Accept", "application/json, text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .addHeader("Accept-Language", "en-US,en;q=0.9,ar;q=0.8")
                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .followRedirects(true)           // ✅ تابع الـ Redirects
            .followSslRedirects(true)        // ✅ تابع SSL Redirects
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        var cleanUrl = baseUrl.trim()
        
        // ✅ أضف http:// تلقائياً إذا لم يكن موجوداً
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }
        
        // ✅ تأكد من / في النهاية
        return cleanUrl.removeSuffix("/") + "/"
    }

    fun getClient(baseUrl: String): Retrofit {
        val cleanUrl = normalizeBaseUrl(baseUrl)

        // ✅ إعادة البناء إذا تغير الـ URL
        if (retrofit == null || currentBaseUrl != cleanUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(buildClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            currentBaseUrl = cleanUrl
        }
        return retrofit!!
    }

    fun getXtreamApiService(baseUrl: String): XtreamApiService {
        return getClient(baseUrl).create(XtreamApiService::class.java)
    }
}
