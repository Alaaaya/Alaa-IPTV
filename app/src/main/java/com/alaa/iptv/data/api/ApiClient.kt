package com.alaa.iptv.data.api

import okhttp3.OkHttpClient
import okhttp3.Request
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

            // 🔥 نضيف User-Agent متصفح
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0 Safari/537.36"
                    )
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
