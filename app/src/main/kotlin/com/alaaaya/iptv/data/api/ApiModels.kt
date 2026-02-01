package com.alaaaya.iptv.data.api

// API Response Models
data class AuthResponse(
    val user_info: UserInfo?,
    val server_info: ServerInfo?
)

data class UserInfo(
    val username: String?,
    val password: String?,
    val message: String?,
    val auth: Int?,
    val status: String?,
    val exp_date: String?,
    val is_trial: String?,
    val active_cons: String?,
    val created_at: String?,
    val max_connections: String?
)

data class ServerInfo(
    val url: String?,
    val port: String?,
    val https_port: String?,
    val server_protocol: String?,
    val rtmp_port: String?,
    val timezone: String?,
    val timestamp_now: Long?
)

data class LiveStreamResponse(
    val num: Int?,
    val stream_id: Int?,
    val name: String?,
    val stream_type: String?,
    val stream_icon: String?,
    val epg_channel_id: String?,
    val added: String?,
    val category_id: String?,
    val custom_sid: String?,
    val tv_archive: Int?,
    val direct_source: String?,
    val tv_archive_duration: Int?
)

data class VodStreamResponse(
    val num: Int?,
    val stream_id: Int?,
    val name: String?,
    val stream_icon: String?,
    val rating: String?,
    val rating_5based: Float?,
    val added: String?,
    val category_id: String?,
    val container_extension: String?,
    val custom_sid: String?,
    val direct_source: String?
)

data class SeriesResponse(
    val num: Int?,
    val series_id: Int?,
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    val rating_5based: Float?,
    val category_id: String?,
    val episode_run_time: String?
)

data class SeriesInfoResponse(
    val seasons: List<SeasonInfo>?,
    val info: SeriesInfo?,
    val episodes: Map<String, List<EpisodeInfo>>?
)

data class SeriesInfo(
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    val rating_5based: Float?,
    val episode_run_time: String?
)

data class SeasonInfo(
    val season_number: Int?,
    val name: String?,
    val episode_count: Int?,
    val cover: String?
)

data class EpisodeInfo(
    val id: String?,
    val episode_num: Int?,
    val title: String?,
    val container_extension: String?,
    val info: EpisodeDetails?
)

data class EpisodeDetails(
    val plot: String?,
    val duration: String?,
    val rating: String?
)

data class CategoryResponse(
    val category_id: String?,
    val category_name: String?,
    val parent_id: Int?
)

data class VodInfoResponse(
    val info: VodInfo?,
    val movie_data: MovieData?
)

data class VodInfo(
    val movie_image: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releasedate: String?,
    val rating: String?,
    val rating_5based: Float?,
    val duration: String?
)

data class MovieData(
    val stream_id: Int?,
    val name: String?,
    val container_extension: String?
)

data class EpgListingsResponse(
    val epg_listings: List<EpgProgram>?
)

data class EpgProgram(
    val id: String?,
    val epg_id: String?,
    val title: String?,
    val lang: String?,
    val start: String?,
    val end: String?,
    val description: String?,
    val channel_id: String?
)
