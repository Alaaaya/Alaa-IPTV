package com.alaa.iptv.data.remote

data class RemoteConfigValue(
    val value: String,
    val type: String
)

data class RemoteFeatureFlag(
    val enabled: Boolean,
    val rolloutPercent: Int
)

data class DeviceControlPlaneSnapshot(
    val tvId: String,
    val deviceStatus: String,
    val remoteLogoutRequested: Boolean,
    val updateChannel: String = "stable",
    val remoteConfig: Map<String, RemoteConfigValue>,
    val featureFlags: Map<String, RemoteFeatureFlag>
)
