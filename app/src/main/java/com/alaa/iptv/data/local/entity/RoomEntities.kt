package com.alaa.iptv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Room entity for Channel/Live TV stream
 */
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey
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
    val position: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Category
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val categoryName: String,
    val parentId: Int = 0,
    val categoryType: String, // "live", "movie", "series"
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Movie
 */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
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
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Series
 */
@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey
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
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Episode
 */
@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey
    val id: String,
    val seriesId: String,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String?,
    val plot: String?,
    val duration: String?,
    val rating: String?,
    val seasonNumber: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Room entity for Favorites
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val itemId: String,
    val itemType: String, // "channel", "movie", "series"
    val addedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for Recently viewed items
 */
@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey
    val itemId: String,
    val itemType: String, // "channel", "movie", "series"
    val viewedAt: Long = System.currentTimeMillis()
)

/**
 * Room entity for EPG Program data
 */
@Entity(
    tableName = "epg_programs",
    indices = [
        Index(value = ["channelId"]),
        Index(value = ["startTime"]),
        Index(value = ["endTime"])
    ]
)
data class EpgProgramEntity(
    @PrimaryKey
    val id: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val category: String?,
    val icon: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
