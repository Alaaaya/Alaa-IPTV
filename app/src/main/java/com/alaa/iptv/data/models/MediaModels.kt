package com.alaa.iptv.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Channel(
    val streamId: String,
    val num: String,
    val name: String,
    val streamType: String,
    val streamIcon: String?,
    val epgChannelId: String?,
    val added: String?,
    val categoryId: String?,
    val categoryName: String?,
    val customSid: String?,
    val tvArchive: Int = 0,
    val directSource: String?,
    val tvArchiveDuration: Int = 0,
    var isFavorite: Boolean = false,
    var position: Int = 0
) : Parcelable {
    fun getStreamUrl(serverUrl: String, username: String, password: String): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return when (streamType) {
            "live" -> "$cleanUrl/live/$username/$password/$streamId.m3u8"
            else -> "$cleanUrl/$username/$password/$streamId"
        }
    }
}

@Parcelize
data class Category(
    val categoryId: String,
    val categoryName: String,
    val parentId: Int = 0
) : Parcelable

@Parcelize
data class Movie(
    val streamId: String,
    val name: String,
    val streamIcon: String?,
    val rating: String?,
    val year: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val durationSecs: String?,
    val duration: String?,
    val containerExtension: String?,
    val categoryId: String?,
    var isFavorite: Boolean = false
) : Parcelable {
    fun getStreamUrl(serverUrl: String, username: String, password: String): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return "$cleanUrl/movie/$username/$password/$streamId.$containerExtension"
    }
}

@Parcelize
data class Series(
    val seriesId: String,
    val name: String,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    val categoryId: String?,
    var isFavorite: Boolean = false
) : Parcelable

@Parcelize
data class Episode(
    val id: String,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String?,
    val info: EpisodeInfo?,
    val seasonNumber: Int
) : Parcelable {
    fun getStreamUrl(serverUrl: String, username: String, password: String): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return "$cleanUrl/series/$username/$password/$id.$containerExtension"
    }
}

@Parcelize
data class EpisodeInfo(
    val plot: String?,
    val duration: String?,
    val rating: String?
) : Parcelable

@Parcelize
data class Recent(
    val itemId: String,
    val itemType: String,
    val timestamp: Long
) : Parcelable

