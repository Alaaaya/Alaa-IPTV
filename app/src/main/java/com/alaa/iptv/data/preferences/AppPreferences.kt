package com.alaa.iptv.data.preferences

import android.content.Context
import android.content.SharedPreferences

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
    
    fun clear() {
        prefs.edit().clear().apply()
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
