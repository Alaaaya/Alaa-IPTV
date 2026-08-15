package com.alaa.iptv.data.preferences

import com.alaa.iptv.data.remote.RemoteConfigValue
import com.alaa.iptv.data.remote.RemoteFeatureFlag

/** قواعد نقية قابلة للاختبار لتطبيق سياسات لوحة التحكم بدون تخزين أسرار أو بيانات اشتراك. */
object ControlPlanePolicy {
    fun isDeviceBlocked(enrolled: Boolean, deviceStatus: String): Boolean {
        return enrolled && deviceStatus.lowercase() in setOf("suspended", "unknown")
    }

    fun isFeatureAllowed(
        featureId: String,
        tvId: String,
        remoteFlags: Map<String, RemoteFeatureFlag>
    ): Boolean {
        val flag = remoteFlags[featureId] ?: return true
        if (!flag.enabled || flag.rolloutPercent <= 0) return false
        if (flag.rolloutPercent >= 100) return true
        return stableBucket("$tvId:$featureId") < flag.rolloutPercent
    }

    fun isHomeCategoryHidden(categoryType: String, remoteConfig: Map<String, RemoteConfigValue>): Boolean {
        val key = when (categoryType) {
            "live" -> "home.hideLive"
            "sports" -> "home.hideSports"
            "news" -> "home.hideNews"
            "movie" -> "home.hideMovies"
            "series" -> "home.hideSeries"
            "kids" -> "home.hideKids"
            "documentary" -> "home.hideDocumentary"
            "music" -> "home.hideMusic"
            else -> return false
        }
        return remoteConfig[key]?.value?.trim()?.equals("true", ignoreCase = true) == true
    }

    fun isMaintenanceEnabled(remoteConfig: Map<String, RemoteConfigValue>): Boolean =
        remoteConfig["maintenance.enabled"]?.value?.trim()?.equals("true", ignoreCase = true) == true

    fun maintenanceMessage(remoteConfig: Map<String, RemoteConfigValue>): String =
        remoteConfig["maintenance.message"]?.value?.trim().orEmpty()

    fun isForcedUpdateRequired(currentVersion: String, remoteConfig: Map<String, RemoteConfigValue>): Boolean {
        val forceEnabled = remoteConfig["app.forceUpdate"]?.value?.trim()?.equals("true", ignoreCase = true) == true
        val minimumVersion = remoteConfig["app.minimumVersion"]?.value?.trim().orEmpty()
        return forceEnabled && minimumVersion.isNotBlank() && compareVersions(currentVersion, minimumVersion) < 0
    }

    fun updateUrl(remoteConfig: Map<String, RemoteConfigValue>): String =
        remoteConfig["app.updateUrl"]?.value?.trim().orEmpty()

    private fun compareVersions(current: String, required: String): Int {
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val requiredParts = required.split(".").map { it.toIntOrNull() ?: 0 }
        for (index in 0 until maxOf(currentParts.size, requiredParts.size)) {
            val comparison = currentParts.getOrElse(index) { 0 }.compareTo(requiredParts.getOrElse(index) { 0 })
            if (comparison != 0) return comparison
        }
        return 0
    }

    private fun stableBucket(value: String): Int {
        var hash = 0L
        value.forEach { char -> hash = (hash * 31 + char.code) and Long.MAX_VALUE }
        return (hash % 100).toInt()
    }
}
