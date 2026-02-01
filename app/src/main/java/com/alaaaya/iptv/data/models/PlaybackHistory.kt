package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: String,
    val channelName: String,
    val lastPlayedAt: Long = System.currentTimeMillis(),
    val playbackPosition: Long = 0
)
