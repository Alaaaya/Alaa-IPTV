package com.alaa.iptv.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// ======================== CHANNEL ========================

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
    var position: Int = 0,
    val containerExtension: String? = null
) : Parcelable {

    fun getStreamUrl(
        serverUrl: String,
        username: String,
        password: String
    ): String {
        val type = streamType.lowercase()

        // استخدم direct source الذي يرسله المزود عندما يكون رابط HTTP صالحاً.
        if (!directSource.isNullOrEmpty()) {
            return directSource
        }

        return when (type) {
            "live" -> StreamUrlFactory.live(serverUrl, username, password, streamId)
            "movie", "vod" -> StreamUrlFactory.movie(serverUrl, username, password, streamId, containerExtension)
            "series" -> StreamUrlFactory.episode(serverUrl, username, password, streamId, containerExtension)
            else -> StreamUrlFactory.live(serverUrl, username, password, streamId)
        }
    }
}

// ======================== CATEGORY ========================

@Parcelize
data class Category(
    val categoryId: String,
    val categoryName: String,
    val parentId: Int = 0,
    val channelCount: Int = 0
) : Parcelable

// ======================== MOVIE ========================

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

    fun getStreamUrl(
        serverUrl: String,
        username: String,
        password: String
    ): String {
        return StreamUrlFactory.movie(serverUrl, username, password, streamId, containerExtension)
    }
}

// ======================== SERIES ========================

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

// ======================== EPISODE ========================

@Parcelize
data class Episode(
    val id: String,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String?,
    val info: EpisodeInfo?,
    val seasonNumber: Int
) : Parcelable {

    fun getStreamUrl(
        serverUrl: String,
        username: String,
        password: String
    ): String {
        return StreamUrlFactory.episode(serverUrl, username, password, id, containerExtension)
    }
}

// ======================== EXTRA MODELS ========================

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

@Parcelize
data class FavoriteItem(
    val contentId: String,
    val name: String,
    val type: String,
    val icon: String?,
    val categoryId: String?,
    val timestamp: Long
) : Parcelable

@Parcelize
data class RecentItem(
    val contentId: String,
    val name: String,
    val type: String,
    val icon: String?,
    val categoryId: String?,
    val timestamp: Long
) : Parcelable
