package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "live", "movie", "series"
    val customOrder: Int = 0
)
