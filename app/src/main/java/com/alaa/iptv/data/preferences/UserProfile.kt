package com.alaa.iptv.data.preferences

data class UserProfile(
    val id: String,
    val name: String,
    val isKidsProfile: Boolean = false,
    val pinHash: String = ""
)

data class IptvSubscription(
    val id: String,
    val title: String,
    val serverUrl: String,
    val username: String,
    val password: String,
    val m3uUrl: String = "",
    val isFavorite: Boolean = false,
    val isDefault: Boolean = false,
    val color: String = "#22D3EE",
    val expiresAtMs: Long = 0L,
    val status: String = "active",
    val notes: String = ""
)
