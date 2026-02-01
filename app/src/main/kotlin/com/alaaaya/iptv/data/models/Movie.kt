package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    val id: String,
    val name: String,
    val streamUrl: String,
    val categoryId: String = "",
    val categoryName: String = "",
    val iconUrl: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    val releaseDate: String = "",
    val rating: Float = 0f,
    val duration: Int = 0,
    val containerExtension: String = "mp4",
    val added: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val customOrder: Int = 0
)
