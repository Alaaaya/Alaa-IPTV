package com.alaaaya.iptv.utils

object Constants {
    // Shared Preferences
    const val PREFS_NAME = "alaa_iptv_prefs"
    const val KEY_SERVER_URL = "server_url"
    const val KEY_USERNAME = "username"
    const val KEY_PASSWORD = "password"
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    // Content Types
    const val CONTENT_TYPE_CHANNEL = "channel"
    const val CONTENT_TYPE_MOVIE = "movie"
    const val CONTENT_TYPE_SERIES = "series"

    // Category Types
    const val CATEGORY_TYPE_LIVE = "live"
    const val CATEGORY_TYPE_MOVIE = "movie"
    const val CATEGORY_TYPE_SERIES = "series"

    // Stream Types
    const val STREAM_TYPE_LIVE = "live"
    const val STREAM_TYPE_VOD = "vod"
    const val STREAM_TYPE_SERIES = "series"

    // Intent Extras
    const val EXTRA_STREAM_URL = "stream_url"
    const val EXTRA_STREAM_TITLE = "stream_title"
    const val EXTRA_STREAM_ID = "stream_id"
    const val EXTRA_STREAM_TYPE = "stream_type"
    const val EXTRA_CONTENT_ID = "content_id"
    const val EXTRA_CONTENT_TYPE = "content_type"

    // API Endpoints
    const val PLAYER_API_PATH = "player_api.php"

    // Stream URL Templates
    // Live: http://domain:port/live/username/password/streamId.ext
    // VOD: http://domain:port/movie/username/password/streamId.ext
    // Series: http://domain:port/series/username/password/episodeId.ext

    // Network
    const val NETWORK_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds

    // Playback
    const val PLAYBACK_POSITION_UPDATE_INTERVAL = 5000L // 5 seconds

    // UI
    const val FOCUS_ANIMATION_DURATION = 200L
    const val CARD_SCALE_FOCUSED = 1.1f
    const val CARD_SCALE_NORMAL = 1.0f

    // Database
    const val DB_NAME = "alaa_iptv_database"
    const val DB_VERSION = 1
}
