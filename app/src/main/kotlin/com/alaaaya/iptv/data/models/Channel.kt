package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey
    val id: String,
    val name: String,
    val streamUrl: String,
    val streamType: String = "live",
    val categoryId: String = "",
    val categoryName: String = "",
    val iconUrl: String = "",
    val epgChannelId: String = "",
    val added: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val customOrder: Int = 0,
    val tvArchive: Int = 0,
    val tvArchiveDuration: Int = 0
)
