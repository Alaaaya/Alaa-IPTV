package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contentId: String,
    val contentType: String, // "channel", "movie", "series"
    val contentName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val position: Long = 0 // playback position in milliseconds
)
