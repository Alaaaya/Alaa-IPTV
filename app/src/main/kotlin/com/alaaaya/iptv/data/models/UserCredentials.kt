package com.alaaaya.iptv.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_credentials")
data class UserCredentials(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serverUrl: String,
    val username: String,
    val password: String,
    val lastLogin: Long = System.currentTimeMillis()
)
