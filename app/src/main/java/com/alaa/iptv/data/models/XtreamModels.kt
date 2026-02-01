package com.alaa.iptv.data.models

import com.google.gson.annotations.SerializedName

data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: UserInfo?,
    @SerializedName("server_info") val serverInfo: ServerInfo?
)

data class UserInfo(
    val username: String?,
    val password: String?,
    val message: String?,
    val auth: Int?,
    val status: String?,
    @SerializedName("exp_date") val expDate: String?,
    @SerializedName("is_trial") val isTrial: String?,
    @SerializedName("active_cons") val activeCons: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("max_connections") val maxConnections: String?
)

data class ServerInfo(
    val url: String?,
    val port: String?,
    @SerializedName("https_port") val httpsPort: String?,
    @SerializedName("server_protocol") val serverProtocol: String?,
    @SerializedName("rtmp_port") val rtmpPort: String?,
    val timezone: String?,
    @SerializedName("timestamp_now") val timestampNow: Long?
)

data class XtreamCategory(
    @SerializedName("category_id") val categoryId: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("parent_id") val parentId: Int = 0
)

data class XtreamStream(
    @SerializedName("stream_id") val streamId: Int?,
    val num: Int?,
    val name: String?,
    @SerializedName("stream_type") val streamType: String?,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int?,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int?
)

data class XtreamMovie(
    @SerializedName("stream_id") val streamId: Int?,
    val name: String?,
    @SerializedName("stream_icon") val streamIcon: String?,
    val rating: String?,
    val year: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("duration_secs") val durationSecs: String?,
    val duration: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class XtreamSeries(
    @SerializedName("series_id") val seriesId: Int?,
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val rating: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class XtreamSeriesInfo(
    val seasons: List<XtreamSeason>?,
    val info: XtreamSeriesDetails?,
    val episodes: Map<String, List<XtreamEpisode>>?
)

data class XtreamSeason(
    @SerializedName("season_number") val seasonNumber: Int,
    val name: String?,
    @SerializedName("episode_count") val episodeCount: Int?
)

data class XtreamEpisode(
    val id: String,
    @SerializedName("episode_num") val episodeNum: Int,
    val title: String?,
    @SerializedName("container_extension") val containerExtension: String?,
    val info: XtreamEpisodeInfo?
)

data class XtreamEpisodeInfo(
    val plot: String?,
    val duration: String?,
    val rating: String?
)

data class XtreamSeriesDetails(
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val rating: String?
)
