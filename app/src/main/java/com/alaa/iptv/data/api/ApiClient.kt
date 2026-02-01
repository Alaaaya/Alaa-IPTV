package com.alaa.iptv.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""
    
    fun getClient(baseUrl: String): Retrofit {
        if (retrofit == null || currentBaseUrl != baseUrl) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            
            retrofit = Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            
            currentBaseUrl = baseUrl
        }
        return retrofit!!
    }
    
    fun getXtreamApiService(baseUrl: String): XtreamApiService {
        return getClient(baseUrl).create(XtreamApiService::class.java)
    }
}
