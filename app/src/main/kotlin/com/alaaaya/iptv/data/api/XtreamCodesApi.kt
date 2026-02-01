package com.alaaaya.iptv.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface XtreamCodesApi {
    
    /**
     * Authenticate user and get account info
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx
     */
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<AuthResponse>

    /**
     * Get live streams categories
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_live_categories
     */
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<CategoryResponse>>

    /**
     * Get live streams
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_live_streams
     */
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<LiveStreamResponse>>

    /**
     * Get VOD categories
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_vod_categories
     */
    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): Response<List<CategoryResponse>>

    /**
     * Get VOD streams (movies)
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_vod_streams
     */
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<VodStreamResponse>>

    /**
     * Get VOD info
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_vod_info&vod_id=xxx
     */
    @GET("player_api.php")
    suspend fun getVodInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_info",
        @Query("vod_id") vodId: String
    ): Response<VodInfoResponse>

    /**
     * Get series categories
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_series_categories
     */
    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): Response<List<CategoryResponse>>

    /**
     * Get series
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_series
     */
    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series",
        @Query("category_id") categoryId: String? = null
    ): Response<List<SeriesResponse>>

    /**
     * Get series info (episodes)
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_series_info&series_id=xxx
     */
    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: String
    ): Response<SeriesInfoResponse>

    /**
     * Get EPG for a specific stream
     * URL format: http://domain:port/player_api.php?username=xxx&password=xxx&action=get_simple_data_table&stream_id=xxx
     */
    @GET("player_api.php")
    suspend fun getEpgForStream(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_simple_data_table",
        @Query("stream_id") streamId: String
    ): Response<EpgListingsResponse>
}
