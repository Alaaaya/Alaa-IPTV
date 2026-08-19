package com.alaa.iptv.data.remote

data class RemoteConfigValue(
    val value: String,
    val type: String
)

data class RemoteFeatureFlag(
    val enabled: Boolean,
    val rolloutPercent: Int
)

data class RemoteSmartFavoriteEntry(
    val contentType: String,
    val contentKey: String,
    val title: String,
    val posterUrl: String?,
    val sortOrder: Int
)

data class RemoteSmartFavoriteGroup(
    val id: String,
    val name: String,
    val color: String,
    val sortOrder: Int,
    val entries: List<RemoteSmartFavoriteEntry>
)

data class DeviceControlPlaneSnapshot(
    val tvId: String,
    val deviceStatus: String,
    val remoteLogoutRequested: Boolean,
    val updateChannel: String = "stable",
    val remoteConfig: Map<String, RemoteConfigValue>,
    val featureFlags: Map<String, RemoteFeatureFlag>,
    val smartFavorites: List<RemoteSmartFavoriteGroup> = emptyList()
)
