package com.alaa.iptv.data.preferences

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class AppPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "alaa_iptv_prefs"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USE_M3U = "use_m3u"
        private const val KEY_M3U_URL = "m3u_url"
        private const val KEY_TV_ID = "tv_id"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_CHANNEL_ORDER = "channel_order"
        private const val KEY_LAST_LIVE_CATEGORY = "last_live_category"
        private const val KEY_LAST_MOVIE_CATEGORY = "last_movie_category"
        private const val KEY_LAST_SERIES_CATEGORY = "last_series_category"
        private const val KEY_DISPLAY_THEME = "display_theme"

        const val THEME_ALAA_CLASSIC = "alaa_classic"
        const val THEME_HOT_PLAYER = "hot_player"
        const val THEME_IBO_CLASSIC = "ibo_classic"
        const val THEME_MODERN_GRID = "modern_grid"
        const val THEME_TV_MINIMAL = "tv_minimal"
        const val THEME_GLASS_UI = "glass_ui"
        const val THEME_CLASSIC_BLACK_TV = "classic_black_tv"
    }
    
    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SERVER_URL, value).apply()
    
    var username: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()
    
    var password: String
        get() = prefs.getString(KEY_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASSWORD, value).apply()
    
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    var useM3U: Boolean
        get() = prefs.getBoolean(KEY_USE_M3U, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_M3U, value).apply()
    
    var m3uUrl: String
        get() = prefs.getString(KEY_M3U_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_M3U_URL, value).apply()

    var tvId: String
        get() = prefs.getString(KEY_TV_ID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TV_ID, value).apply()

    var lastLiveCategoryId: String
        get() = prefs.getString(KEY_LAST_LIVE_CATEGORY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_LIVE_CATEGORY, value).apply()

    var lastMovieCategoryId: String
        get() = prefs.getString(KEY_LAST_MOVIE_CATEGORY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_MOVIE_CATEGORY, value).apply()

    var lastSeriesCategoryId: String
        get() = prefs.getString(KEY_LAST_SERIES_CATEGORY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SERIES_CATEGORY, value).apply()

    var displayTheme: String
        get() = prefs.getString(KEY_DISPLAY_THEME, THEME_ALAA_CLASSIC) ?: THEME_ALAA_CLASSIC
        set(value) = prefs.edit().putString(KEY_DISPLAY_THEME, value).apply()

    val isHotPlayerTheme: Boolean
        get() = displayTheme == THEME_HOT_PLAYER

    fun getOrCreateTvId(): String {
        if (tvId.isNotBlank()) return tvId

        val token = UUID.randomUUID().toString().replace("-", "").uppercase()
        val generatedId = "ALA-${token.take(4)}-${token.drop(4).take(4)}-${token.drop(8).take(4)}"
        tvId = generatedId
        return generatedId
    }
    
    fun clear() {
        val deviceId = tvId
        prefs.edit().clear().putString(KEY_TV_ID, deviceId).apply()
    }
    
    fun saveFavorites(favorites: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
    
    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    }

    fun saveChannelOrder(channelKeys: List<String>) {
        prefs.edit().putString(KEY_CHANNEL_ORDER, channelKeys.joinToString(separator = "|")) .apply()
    }

    fun getChannelOrder(): List<String> {
        return prefs.getString(KEY_CHANNEL_ORDER, "")
            .orEmpty()
            .split("|")
            .filter { it.isNotBlank() }
    }
}
