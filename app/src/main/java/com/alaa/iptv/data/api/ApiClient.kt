package com.alaa.iptv.data.api

import okhttp3.OkHttpClient
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
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getClient(baseUrl: String): Retrofit {

        // تنظيف الرابط
        val cleanUrl = baseUrl
            .trim()
            .removeSuffix("/")
            .plus("/")

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
