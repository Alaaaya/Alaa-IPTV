package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class Series(
    @PrimaryKey
    val id: String,
    val name: String,
    val categoryId: String = "",
    val categoryName: String = "",
    val iconUrl: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val rating: Float = 0f,
    val episodeRunTime: String = "",
    val added: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val customOrder: Int = 0
)

@Entity(tableName = "episodes")
data class Episode(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val title: String,
    val streamUrl: String,
    val episodeNum: Int = 0,
    val season: Int = 0,
    val containerExtension: String = "mp4",
    val plot: String = "",
    val duration: Int = 0,
    val added: Long = System.currentTimeMillis()
)
