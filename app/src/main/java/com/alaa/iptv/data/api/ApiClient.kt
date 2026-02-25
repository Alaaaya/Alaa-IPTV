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
                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15")
                    .header("Accept", "application/json, */*")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .followRedirects(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        var cleanUrl = baseUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
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
