package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password: String,
    val serverUrl: String,
    val token: String? = null,
    val expiryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
