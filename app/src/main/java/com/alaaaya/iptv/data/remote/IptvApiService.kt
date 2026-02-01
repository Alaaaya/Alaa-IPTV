package com.alaaaya.iptv.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface IptvApiService {
    
    @GET("player_api.php")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<LoginResponse>
    
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): Response<List<LiveStream>>
    
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<Category>>
    
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): Response<List<LiveStream>>
    
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<List<Category>>
    
    @GET("player_api.php")
    suspend fun getSeriesStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): Response<List<LiveStream>>
    
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<List<Category>>
}
