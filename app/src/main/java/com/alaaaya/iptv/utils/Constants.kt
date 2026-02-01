package com.alaaaya.iptv.utils

object Constants {
    // API Constants
    const val DEFAULT_SERVER_URL = "http://example.com/"
    const val API_TIMEOUT = 30L // seconds
    
    // Player Constants
    const val PLAYER_BUFFER_FOR_PLAYBACK_MS = 2500
    const val PLAYER_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000
    const val PLAYER_MIN_BUFFER_MS = 15000
    const val PLAYER_MAX_BUFFER_MS = 50000
    
    // Database Constants
    const val DATABASE_NAME = "alaa_iptv_database"
    const val DATABASE_VERSION = 1
    
    // SharedPreferences Constants
    const val PREFS_NAME = "alaa_iptv_prefs"
    const val PREF_KEY_USERNAME = "username"
    const val PREF_KEY_PASSWORD = "password"
    const val PREF_KEY_SERVER_URL = "server_url"
    const val PREF_KEY_IS_LOGGED_IN = "is_logged_in"
    
    // Channel Categories
    const val CATEGORY_ALL = "all"
    const val CATEGORY_LIVE_TV = "live_tv"
    const val CATEGORY_MOVIES = "movies"
    const val CATEGORY_SERIES = "series"
    const val CATEGORY_NEWS = "news"
    const val CATEGORY_SPORTS = "sports"
    const val CATEGORY_ENTERTAINMENT = "entertainment"
    const val CATEGORY_KIDS = "kids"
    
    // Intent Extras
    const val EXTRA_CHANNEL_ID = "extra_channel_id"
    const val EXTRA_CHANNEL_NAME = "extra_channel_name"
    const val EXTRA_STREAM_URL = "extra_stream_url"
}
