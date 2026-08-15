package com.alaa.iptv.data.preferences

data class MediaLibraryEntry(
    val id: String,
    val title: String,
    val streamUrl: String,
    val streamType: String,
    val imageUrl: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
