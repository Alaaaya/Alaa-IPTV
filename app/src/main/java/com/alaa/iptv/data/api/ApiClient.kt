package com.alaa.iptv.data.api

import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null

    private fun buildClient(): OkHttpClient {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()

            // 🔥 أهم شي لحل 511
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)

            // 🔥 هيدر مثل تطبيق IPTV حقيقي
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "IPTV Smarters Pro")
                    .header("Accept", "application/json")
                    .header("Connection", "keep-alive")
                    .build()

                chain.proceed(request)
            }

            .addInterceptor(logging)

            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

            .build()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        var cleanUrl = baseUrl.trim()

        // 🔥 نجبره يستخدم HTTPS دائماً
        if (!cleanUrl.startsWith("http")) {
            cleanUrl = "https://$cleanUrl"
        }

        return cleanUrl.removeSuffix("/") + "/"
    }

    fun getXtreamApiService(baseUrl: String): XtreamApiService {

        val cleanUrl = normalizeBaseUrl(baseUrl)

        if (retrofit == null || currentBaseUrl != cleanUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(buildClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            currentBaseUrl = cleanUrl
        }

        return retrofit!!.create(XtreamApiService::class.java)
    }
}
