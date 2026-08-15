package com.alaa.iptv.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.models.FavoriteChannelCodec
import com.alaa.iptv.data.remote.DeviceControlPlaneSnapshot
import com.alaa.iptv.data.remote.RemoteConfigValue
import com.alaa.iptv.data.remote.RemoteFeatureFlag
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

class AppPreferences(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val credentialCrypto = CredentialCrypto()

    init {
        migrateCredentialsIfNeeded()
    }
    
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
        private const val KEY_FAVORITE_CHANNEL_DATA = "favorite_channel_data"
        private const val KEY_CHANNEL_ORDER = "channel_order"
        private const val KEY_LAST_LIVE_CATEGORY = "last_live_category"
        private const val KEY_LAST_MOVIE_CATEGORY = "last_movie_category"
        private const val KEY_LAST_SERIES_CATEGORY = "last_series_category"
        private const val KEY_DISPLAY_THEME = "display_theme"
        private const val KEY_CREDENTIAL_ENCRYPTION_COMPLETE = "credential_encryption_complete"
        private const val KEY_FEATURE_PREFIX = "feature_enabled_"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val KEY_SUBSCRIPTIONS = "subscriptions"
        private const val KEY_ACTIVE_SUBSCRIPTION_ID = "active_subscription_id"
        private const val KEY_HIDDEN_CONTENT_PREFIX = "hidden_content_"
        private const val KEY_CATEGORY_ORDER_PREFIX = "category_order_profile_"
        private const val KEY_HOME_CATEGORIES_PREFIX = "home_categories_"
        private const val KEY_CONNECTION_FAILURE_COUNT = "connection_failure_count"
        private const val KEY_WATCHLIST_PREFIX = "watchlist_"
        private const val KEY_HISTORY_PREFIX = "history_"
        private const val KEY_RECENT_CHANNELS_PREFIX = "recent_channels_"
        private const val KEY_CONTROL_PLANE_ENROLLED = "control_plane_enrolled"
        private const val KEY_CONTROL_PLANE_STATUS = "control_plane_status"
        private const val KEY_CONTROL_PLANE_SYNCED_AT = "control_plane_synced_at"
        private const val KEY_REMOTE_CONFIG_SNAPSHOT = "remote_config_snapshot"
        private const val KEY_REMOTE_FEATURE_FLAGS = "remote_feature_flags"
        private const val KEY_REMOTE_LOGOUT_REQUESTED = "remote_logout_requested"
        private const val KEY_SAFE_DIAGNOSTICS = "safe_diagnostics"
        private const val CONTROL_PLANE_REFRESH_MS = 60_000L
        private const val DEFAULT_PROFILE_ID = "owner"

        const val THEME_ALAA_CLASSIC = "alaa_classic"
        const val THEME_MIDNIGHT_GOLD = "midnight_gold"
        const val THEME_CRIMSON_CLASSIC = "crimson_classic"
        const val THEME_MODERN_GRID = "modern_grid"
        const val THEME_TV_MINIMAL = "tv_minimal"
        const val THEME_GLASS_UI = "glass_ui"
        const val THEME_CLASSIC_BLACK_TV = "classic_black_tv"
        const val THEME_NEON_ARCADE = "neon_arcade"
        const val THEME_CINEMA_SPOTLIGHT = "cinema_spotlight"
        const val THEME_SAPPHIRE_HORIZON = "sapphire_horizon"
        const val THEME_EMERALD_PULSE = "emerald_pulse"
        const val THEME_AMBER_CONSOLE = "amber_console"
        const val THEME_NORDIC_LIGHT = "nordic_light"
        const val THEME_SUNSET_LOUNGE = "sunset_lounge"
        const val THEME_MONO_STUDIO = "mono_studio"
        const val THEME_OCEAN_WAVE = "ocean_wave"
        const val THEME_ROYAL_VELVET = "royal_velvet"
    }

    private fun migrateCredentialsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            prefs.getBoolean(KEY_CREDENTIAL_ENCRYPTION_COMPLETE, false)
        ) return

        val editor = prefs.edit()
        listOf(KEY_SERVER_URL, KEY_USERNAME, KEY_PASSWORD, KEY_M3U_URL).forEach { key ->
            prefs.getString(key, null)
                ?.takeIf { it.isNotBlank() && !credentialCrypto.isEncrypted(it) }
                ?.let { editor.putString(key, credentialCrypto.encrypt(it)) }
        }
        editor.putBoolean(KEY_CREDENTIAL_ENCRYPTION_COMPLETE, true).apply()
    }
    
    var serverUrl: String
        get() = credentialCrypto.decrypt(prefs.getString(KEY_SERVER_URL, "") ?: "")
        set(value) = prefs.edit().putString(KEY_SERVER_URL, credentialCrypto.encrypt(value)).apply()
    
    var username: String
        get() = credentialCrypto.decrypt(prefs.getString(KEY_USERNAME, "") ?: "")
        set(value) = prefs.edit().putString(KEY_USERNAME, credentialCrypto.encrypt(value)).apply()
    
    var password: String
        get() = credentialCrypto.decrypt(prefs.getString(KEY_PASSWORD, "") ?: "")
        set(value) = prefs.edit().putString(KEY_PASSWORD, credentialCrypto.encrypt(value)).apply()
    
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
    
    var useM3U: Boolean
        get() = prefs.getBoolean(KEY_USE_M3U, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_M3U, value).apply()
    
    var m3uUrl: String
        get() = credentialCrypto.decrypt(prefs.getString(KEY_M3U_URL, "") ?: "")
        set(value) = prefs.edit().putString(KEY_M3U_URL, credentialCrypto.encrypt(value)).apply()

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

    val isMidnightGoldTheme: Boolean
        get() = displayTheme == THEME_MIDNIGHT_GOLD

    fun isFeatureEnabled(featureId: String): Boolean {
        val defaultValue = FeatureCatalog.option(featureId).defaultEnabled
        return prefs.getBoolean(featureKey(featureId), defaultValue) &&
            ControlPlanePolicy.isFeatureAllowed(featureId, tvId, getRemoteFeatureFlags())
    }

    fun setFeatureEnabled(featureId: String, enabled: Boolean) {
        FeatureCatalog.option(featureId)
        prefs.edit().putBoolean(featureKey(featureId), enabled).apply()
    }

    private fun featureKey(featureId: String): String = "$KEY_FEATURE_PREFIX${activeProfileId}_$featureId"

    var isControlPlaneEnrolled: Boolean
        get() = prefs.getBoolean(KEY_CONTROL_PLANE_ENROLLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONTROL_PLANE_ENROLLED, value).apply()

    var controlPlaneStatus: String
        get() = prefs.getString(KEY_CONTROL_PLANE_STATUS, "active").orEmpty().ifBlank { "active" }
        private set(value) = prefs.edit().putString(KEY_CONTROL_PLANE_STATUS, value).apply()

    fun shouldRefreshControlPlane(nowMs: Long = System.currentTimeMillis()): Boolean {
        return nowMs - prefs.getLong(KEY_CONTROL_PLANE_SYNCED_AT, 0L) >= CONTROL_PLANE_REFRESH_MS
    }

    fun lastControlPlaneSyncAt(): Long = prefs.getLong(KEY_CONTROL_PLANE_SYNCED_AT, 0L)

    fun addSafeDiagnostic(area: String, throwable: Throwable? = null): String {
        val reference = "AL-${System.currentTimeMillis().toString(36).uppercase()}"
        val message = throwable?.message.orEmpty()
            .replace(serverUrl, "[server]")
            .replace(username, "[user]")
            .replace(password, "[secret]")
            .take(180)
        val entries = prefs.getStringSet(KEY_SAFE_DIAGNOSTICS, emptySet()).orEmpty().toMutableSet()
        entries += "$reference|$area|$message"
        prefs.edit().putStringSet(KEY_SAFE_DIAGNOSTICS, entries.toList().takeLast(20).toSet()).apply()
        return reference
    }

    fun getSafeDiagnostics(): List<String> = prefs.getStringSet(KEY_SAFE_DIAGNOSTICS, emptySet())
        ?.toList()?.sortedDescending().orEmpty()

    fun clearSafeDiagnostics() {
        prefs.edit().remove(KEY_SAFE_DIAGNOSTICS).apply()
    }

    fun isDeviceAccessBlocked(): Boolean = ControlPlanePolicy.isDeviceBlocked(isControlPlaneEnrolled, controlPlaneStatus)

    fun isRemoteLogoutRequested(): Boolean = prefs.getBoolean(KEY_REMOTE_LOGOUT_REQUESTED, false)

    fun applyControlPlaneSnapshot(snapshot: DeviceControlPlaneSnapshot) {
        val remoteConfigJson = JSONObject().apply {
            snapshot.remoteConfig.forEach { (key, item) -> put(key, JSONObject().put("value", item.value).put("type", item.type)) }
        }
        val flagsJson = JSONObject().apply {
            snapshot.featureFlags.forEach { (key, item) -> put(key, JSONObject().put("enabled", item.enabled).put("rolloutPercent", item.rolloutPercent)) }
        }
        prefs.edit()
            .putString(KEY_CONTROL_PLANE_STATUS, snapshot.deviceStatus)
            .putString(KEY_REMOTE_CONFIG_SNAPSHOT, remoteConfigJson.toString())
            .putString(KEY_REMOTE_FEATURE_FLAGS, flagsJson.toString())
            .putBoolean(KEY_REMOTE_LOGOUT_REQUESTED, snapshot.remoteLogoutRequested)
            .putLong(KEY_CONTROL_PLANE_SYNCED_AT, System.currentTimeMillis())
            .apply()
    }

    fun isHomeCategoryRemotelyHidden(categoryType: String): Boolean =
        ControlPlanePolicy.isHomeCategoryHidden(categoryType, getRemoteConfig())

    fun isMaintenanceEnabled(): Boolean = ControlPlanePolicy.isMaintenanceEnabled(getRemoteConfig())

    fun maintenanceMessage(): String = ControlPlanePolicy.maintenanceMessage(getRemoteConfig())

    fun isForcedUpdateRequired(currentVersion: String): Boolean =
        ControlPlanePolicy.isForcedUpdateRequired(currentVersion, getRemoteConfig())

    fun forcedUpdateUrl(): String = ControlPlanePolicy.updateUrl(getRemoteConfig())

    private fun getRemoteConfig(): Map<String, RemoteConfigValue> = runCatching {
        val root = JSONObject(prefs.getString(KEY_REMOTE_CONFIG_SNAPSHOT, "{}").orEmpty())
        buildMap {
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = root.optJSONObject(key) ?: continue
                put(key, RemoteConfigValue(item.optString("value"), item.optString("type", "string")))
            }
        }
    }.getOrDefault(emptyMap())

    private fun getRemoteFeatureFlags(): Map<String, RemoteFeatureFlag> = runCatching {
        val root = JSONObject(prefs.getString(KEY_REMOTE_FEATURE_FLAGS, "{}").orEmpty())
        buildMap {
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = root.optJSONObject(key) ?: continue
                put(key, RemoteFeatureFlag(item.optBoolean("enabled", false), item.optInt("rolloutPercent", 100).coerceIn(0, 100)))
            }
        }
    }.getOrDefault(emptyMap())

    var activeProfileId: String
        get() = prefs.getString(KEY_ACTIVE_PROFILE_ID, DEFAULT_PROFILE_ID).orEmpty().ifBlank { DEFAULT_PROFILE_ID }
        private set(value) = prefs.edit().putString(KEY_ACTIVE_PROFILE_ID, value).apply()

    fun getProfiles(): List<UserProfile> {
        val raw = prefs.getString(KEY_PROFILES, "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val name = item.optString("name")
                    if (id.isBlank() || name.isBlank()) continue
                    add(UserProfile(id, name, item.optBoolean("kids"), item.optString("pinHash")))
                }
            }
        }.getOrDefault(emptyList()).ifEmpty { listOf(UserProfile(DEFAULT_PROFILE_ID, "المالك")) }
    }

    fun getActiveProfile(): UserProfile = getProfiles().firstOrNull { it.id == activeProfileId }
        ?: getProfiles().first()

    fun saveProfile(profile: UserProfile) {
        val profiles = getProfiles().filterNot { it.id == profile.id } + profile
        val raw = JSONArray().apply {
            profiles.forEach {
                put(JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("kids", it.isKidsProfile)
                    .put("pinHash", it.pinHash)
                )
            }
        }.toString()
        prefs.edit().putString(KEY_PROFILES, raw).apply()
    }

    fun switchProfile(profileId: String, pin: String? = null): Boolean {
        val profile = getProfiles().firstOrNull { it.id == profileId } ?: return false
        if (profile.pinHash.isNotBlank() && profile.pinHash != hashPin(pin.orEmpty())) return false
        activeProfileId = profile.id
        return true
    }

    fun createProfile(name: String, isKidsProfile: Boolean, pin: String? = null): UserProfile {
        val profile = UserProfile(
            id = "profile_${UUID.randomUUID()}",
            name = name.trim().ifBlank { "ملف جديد" },
            isKidsProfile = isKidsProfile,
            pinHash = pin?.takeIf { it.isNotBlank() }?.let(::hashPin).orEmpty()
        )
        saveProfile(profile)
        return profile
    }

    fun setActiveProfilePin(pin: String?) {
        val active = getActiveProfile()
        saveProfile(active.copy(pinHash = pin?.takeIf { it.isNotBlank() }?.let(::hashPin).orEmpty()))
    }

    fun isContentHidden(contentKey: String): Boolean = getHiddenContent().contains(contentKey)

    fun getHiddenContent(): Set<String> = prefs.getStringSet("$KEY_HIDDEN_CONTENT_PREFIX$activeProfileId", emptySet())
        ?.toSet().orEmpty()

    fun setContentHidden(contentKey: String, hidden: Boolean) {
        val values = getHiddenContent().toMutableSet()
        if (hidden) values += contentKey else values -= contentKey
        prefs.edit().putStringSet("$KEY_HIDDEN_CONTENT_PREFIX$activeProfileId", values).apply()
    }

    fun clearHiddenContent() {
        prefs.edit().remove("$KEY_HIDDEN_CONTENT_PREFIX$activeProfileId").apply()
    }

    fun saveProfileCategoryOrder(categoryIds: List<String>) {
        prefs.edit().putString("$KEY_CATEGORY_ORDER_PREFIX$activeProfileId", categoryIds.joinToString("|")).apply()
    }

    fun getProfileCategoryOrder(): List<String> = prefs
        .getString("$KEY_CATEGORY_ORDER_PREFIX$activeProfileId", "").orEmpty()
        .split("|").filter { it.isNotBlank() }

    fun saveHomeCategoryTypes(types: List<String>) {
        prefs.edit().putString("$KEY_HOME_CATEGORIES_PREFIX$activeProfileId", types.joinToString("|")).apply()
    }

    fun getHomeCategoryTypes(defaultTypes: List<String>): List<String> {
        val saved = prefs.getString("$KEY_HOME_CATEGORIES_PREFIX$activeProfileId", "").orEmpty()
            .split("|").filter { it.isNotBlank() }
        return saved.ifEmpty { defaultTypes }
    }

    fun registerConnectionFailure(): Int {
        val next = prefs.getInt(KEY_CONNECTION_FAILURE_COUNT, 0).coerceAtMost(9) + 1
        prefs.edit().putInt(KEY_CONNECTION_FAILURE_COUNT, next).apply()
        return next
    }

    fun resetConnectionFailures() {
        prefs.edit().remove(KEY_CONNECTION_FAILURE_COUNT).apply()
    }

    /** نسخة محلية مشفرة قابلة للاستعادة على الجهاز نفسه، ولا تحتوي أي كلمات مرور أو روابط اشتراك. */
    fun exportEncryptedSettingsBackup(): String {
        val profile = getActiveProfile()
        val featureStates = JSONObject().apply {
            FeatureCatalog.options.forEach { put(it.id, isFeatureEnabled(it.id)) }
        }
        val payload = JSONObject()
            .put("version", 1)
            .put("profileId", profile.id)
            .put("theme", displayTheme)
            .put("features", featureStates)
            .put("favorites", JSONArray(getFavorites().toList()))
            .put("channelOrder", JSONArray(getChannelOrder()))
            .put("hidden", JSONArray(getHiddenContent().toList()))
            .put("categoryOrder", JSONArray(getProfileCategoryOrder()))
            .put("homeCategories", JSONArray(getHomeCategoryTypes(emptyList())))
        return credentialCrypto.encrypt(payload.toString())
    }

    fun importEncryptedSettingsBackup(encrypted: String): Boolean = runCatching {
        val root = JSONObject(credentialCrypto.decrypt(encrypted))
        require(root.optInt("version") == 1) { "نسخة احتياطية غير مدعومة" }
        root.optString("theme").takeIf { it.isNotBlank() }?.let { displayTheme = it }
        root.optJSONObject("features")?.let { featureObject ->
            FeatureCatalog.options.forEach { option ->
                if (featureObject.has(option.id)) setFeatureEnabled(option.id, featureObject.optBoolean(option.id, option.defaultEnabled))
            }
        }
        fun jsonStrings(name: String): List<String> {
            val values = root.optJSONArray(name) ?: return emptyList()
            return List(values.length()) { values.optString(it) }.filter { it.isNotBlank() }
        }
        saveFavorites(jsonStrings("favorites").toSet())
        saveChannelOrder(jsonStrings("channelOrder"))
        prefs.edit().putStringSet("$KEY_HIDDEN_CONTENT_PREFIX$activeProfileId", jsonStrings("hidden").toSet()).apply()
        saveProfileCategoryOrder(jsonStrings("categoryOrder"))
        saveHomeCategoryTypes(jsonStrings("homeCategories"))
        true
    }.getOrDefault(false)

    fun getSubscriptions(): List<IptvSubscription> {
        val raw = credentialCrypto.decrypt(prefs.getString(KEY_SUBSCRIPTIONS, "") ?: "")
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val title = item.optString("title")
                    val host = item.optString("host")
                    if (id.isBlank() || title.isBlank() || host.isBlank()) continue
                    add(IptvSubscription(
                        id = id,
                        title = title,
                        serverUrl = host,
                        username = item.optString("username"),
                        password = item.optString("password"),
                        m3uUrl = item.optString("m3uUrl")
                    ))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveSubscription(subscription: IptvSubscription) {
        val subscriptions = getSubscriptions().filterNot { it.id == subscription.id } + subscription
        val raw = JSONArray().apply {
            subscriptions.forEach {
                put(JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("host", it.serverUrl)
                    .put("username", it.username)
                    .put("password", it.password)
                    .put("m3uUrl", it.m3uUrl)
                )
            }
        }.toString()
        prefs.edit().putString(KEY_SUBSCRIPTIONS, credentialCrypto.encrypt(raw)).apply()
    }

    fun activateSubscription(subscriptionId: String): Boolean {
        val subscription = getSubscriptions().firstOrNull { it.id == subscriptionId } ?: return false
        serverUrl = subscription.serverUrl
        username = subscription.username
        password = subscription.password
        m3uUrl = subscription.m3uUrl
        useM3U = subscription.m3uUrl.isNotBlank()
        prefs.edit().putString(KEY_ACTIVE_SUBSCRIPTION_ID, subscription.id).apply()
        return true
    }

    fun saveCurrentSubscription(title: String): IptvSubscription {
        val subscription = IptvSubscription(
            id = prefs.getString(KEY_ACTIVE_SUBSCRIPTION_ID, null) ?: "subscription_${UUID.randomUUID()}",
            title = title.trim().ifBlank { "اشتراك IPTV" },
            serverUrl = serverUrl,
            username = username,
            password = password,
            m3uUrl = m3uUrl
        )
        saveSubscription(subscription)
        return subscription
    }

    fun getWatchlist(): List<MediaLibraryEntry> = getLibraryEntries(KEY_WATCHLIST_PREFIX)

    fun toggleWatchlist(entry: MediaLibraryEntry): Boolean {
        val current = getWatchlist().toMutableList()
        val index = current.indexOfFirst { it.id == entry.id && it.streamType == entry.streamType }
        val added = index < 0
        if (added) current.add(0, entry.copy(updatedAt = System.currentTimeMillis())) else current.removeAt(index)
        saveLibraryEntries(KEY_WATCHLIST_PREFIX, current)
        return added
    }

    fun isInWatchlist(id: String, streamType: String): Boolean = getWatchlist()
        .any { it.id == id && it.streamType == streamType }

    fun getPlaybackHistory(): List<MediaLibraryEntry> = getLibraryEntries(KEY_HISTORY_PREFIX)

    fun savePlayback(entry: MediaLibraryEntry) {
        val current = getPlaybackHistory().toMutableList()
        current.removeAll { it.id == entry.id && it.streamType == entry.streamType }
        current.add(0, entry.copy(updatedAt = System.currentTimeMillis()))
        saveLibraryEntries(KEY_HISTORY_PREFIX, current.take(120))
    }

    fun getRecentChannels(): List<MediaLibraryEntry> = getLibraryEntries(KEY_RECENT_CHANNELS_PREFIX)

    fun saveRecentChannel(entry: MediaLibraryEntry) {
        val current = getRecentChannels().toMutableList()
        current.removeAll { it.id == entry.id }
        current.add(0, entry.copy(updatedAt = System.currentTimeMillis(), positionMs = 0L, durationMs = 0L))
        saveLibraryEntries(KEY_RECENT_CHANNELS_PREFIX, current.take(30))
    }

    private fun getLibraryEntries(prefix: String): List<MediaLibraryEntry> {
        val raw = credentialCrypto.decrypt(prefs.getString("$prefix$activeProfileId", "") ?: "")
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    val title = item.optString("title")
                    val url = item.optString("url")
                    val type = item.optString("type")
                    if (id.isBlank() || title.isBlank() || url.isBlank() || type.isBlank()) continue
                    add(MediaLibraryEntry(
                        id = id,
                        title = title,
                        streamUrl = url,
                        streamType = type,
                        imageUrl = item.optString("image").takeIf { it.isNotBlank() },
                        positionMs = item.optLong("position"),
                        durationMs = item.optLong("duration"),
                        updatedAt = item.optLong("updated")
                    ))
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun saveLibraryEntries(prefix: String, entries: List<MediaLibraryEntry>) {
        val raw = JSONArray().apply {
            entries.forEach {
                put(JSONObject()
                    .put("id", it.id)
                    .put("title", it.title)
                    .put("url", it.streamUrl)
                    .put("type", it.streamType)
                    .put("image", it.imageUrl.orEmpty())
                    .put("position", it.positionMs)
                    .put("duration", it.durationMs)
                    .put("updated", it.updatedAt)
                )
            }
        }.toString()
        prefs.edit().putString("$prefix$activeProfileId", credentialCrypto.encrypt(raw)).apply()
    }

    private fun hashPin(pin: String): String = MessageDigest.getInstance("SHA-256")
        .digest(pin.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    fun getOrCreateTvId(): String {
        if (tvId.isNotBlank()) return tvId

        val token = UUID.randomUUID().toString().replace("-", "").uppercase()
        val generatedId = "ALA-${token.take(4)}-${token.drop(4).take(4)}-${token.drop(8).take(4)}"
        tvId = generatedId
        return generatedId
    }
    
    fun clear() {
        val deviceId = tvId
        val controlPlaneEnrolled = isControlPlaneEnrolled
        val lastControlPlaneStatus = controlPlaneStatus
        prefs.edit().clear()
            .putString(KEY_TV_ID, deviceId)
            .putBoolean(KEY_CONTROL_PLANE_ENROLLED, controlPlaneEnrolled)
            .putString(KEY_CONTROL_PLANE_STATUS, lastControlPlaneStatus)
            .apply()
    }
    
    fun saveFavorites(favorites: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }
    
    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet() ?: emptySet()
    }

    fun saveFavoriteChannel(channel: Channel) {
        val key = favoriteKey(channel)
        val saved = getFavoriteChannels().filterNot { favoriteKey(it) == key } + channel.copy(isFavorite = true)
        prefs.edit().putString(KEY_FAVORITE_CHANNEL_DATA, FavoriteChannelCodec.encode(saved)).apply()
    }

    fun removeFavoriteChannel(channel: Channel) {
        val key = favoriteKey(channel)
        val saved = getFavoriteChannels().filterNot { favoriteKey(it) == key }
        prefs.edit().putString(KEY_FAVORITE_CHANNEL_DATA, FavoriteChannelCodec.encode(saved)).apply()
    }

    fun getFavoriteChannels(): List<Channel> =
        FavoriteChannelCodec.decode(prefs.getString(KEY_FAVORITE_CHANNEL_DATA, "[]"))

    private fun favoriteKey(channel: Channel): String = "${channel.streamType.lowercase()}:${channel.streamId}"

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
