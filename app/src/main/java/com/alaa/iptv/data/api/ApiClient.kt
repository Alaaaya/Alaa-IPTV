package com.alaa.iptv.data.api

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    // 🔥 نحفظ الكوكيز
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
            .cookieJar(cookieJar) // 🔥 أهم سطر
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "IPTVPlayer/1.0")
                    .addHeader("Accept", "application/json")
                    .build()

                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        return baseUrl
            .trim()
            .removeSuffix("/")
            .plus("/")
    }

    fun getClient(baseUrl: String): Retrofit {

        val cleanUrl = normalizeBaseUrl(baseUrl)

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
        return getClient(baseUrl)
            .create(XtreamApiService::class.java)
    }
}
