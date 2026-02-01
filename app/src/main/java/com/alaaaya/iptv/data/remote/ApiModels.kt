package com.alaaaya.iptv.data.remote

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val user_info: UserInfo?,
    val server_info: ServerInfo?
)

data class UserInfo(
    val username: String,
    val password: String,
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
    val timestamp_now: Long?
)

data class LiveStream(
    val num: Int?,
    val name: String?,
    val stream_type: String?,
    val stream_id: Int?,
    val stream_icon: String?,
    val epg_channel_id: String?,
    val added: String?,
    val category_id: String?,
    val custom_sid: String?,
    val tv_archive: Int?,
    val direct_source: String?,
    val tv_archive_duration: Int?
)

data class Category(
    val category_id: String?,
    val category_name: String?,
    val parent_id: Int?
)

data class VodInfo(
    val info: MovieInfo?,
    val movie_data: MovieData?
)

data class MovieInfo(
    val tmdb_id: String?,
    val name: String?,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    val duration: String?
)

data class MovieData(
    val stream_id: Int?,
    val name: String?,
    val added: String?,
    val category_id: String?,
    val container_extension: String?,
    val custom_sid: String?,
    val direct_source: String?
)
