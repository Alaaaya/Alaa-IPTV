package com.alaa.iptv.data.api

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import com.alaa.iptv.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    private val cookieJar = object : CookieJar {
        // قد يستدعي OkHttp الحفظ والقراءة من خيوط Dispatcher مختلفة؛ Map المتزامنة
        // تمنع سباقاً في ذاكرة الجلسة عند تنفيذ طلبات متوازية.
        private val cookieStore = ConcurrentHashMap<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private fun buildClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "IPTV/1.0 (AndroidTV; ExoPlayer)"
                    )
                    .header("Accept", "application/json")
                    .header("Connection", "keep-alive")
                    .build()
                chain.proceed(request)
            }
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        }
        return builder.build()
    }

    private fun normalizeUrl(url: String): String {
        var clean = url.trim()

        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }

        return clean.removeSuffix("/") + "/"
    }

    @Synchronized
    fun getXtreamApiService(baseUrl: String): XtreamApiService {

        val cleanUrl = normalizeUrl(baseUrl)

        if (retrofit == null || currentBaseUrl != cleanUrl) {

            retrofit = Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(buildClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            currentBaseUrl = cleanUrl
        }

        return requireNotNull(retrofit).create(XtreamApiService::class.java)
    }
}
