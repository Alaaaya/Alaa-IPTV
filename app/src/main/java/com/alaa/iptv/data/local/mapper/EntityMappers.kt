package com.alaa.iptv.data.local.mapper

import com.alaa.iptv.data.local.entity.*
import com.alaa.iptv.data.models.*

/**
 * Extension functions to convert between Room entities and domain models
 */

// Channel conversions
fun ChannelEntity.toChannel(isFavorite: Boolean = false): Channel {
    return Channel(
        streamId = streamId,
        num = num,
        name = name,
        streamType = streamType,
        streamIcon = streamIcon,
        epgChannelId = epgChannelId,
        added = added,
        categoryId = categoryId,
        categoryName = categoryName,
        customSid = customSid,
        tvArchive = tvArchive,
        directSource = directSource,
        tvArchiveDuration = tvArchiveDuration,
        isFavorite = isFavorite,
        position = position
    )
}

fun Channel.toEntity(): ChannelEntity {
    return ChannelEntity(
        streamId = streamId,
        num = num,
        name = name,
        streamType = streamType,
        streamIcon = streamIcon,
        epgChannelId = epgChannelId,
        added = added,
        categoryId = categoryId,
        categoryName = categoryName,
        customSid = customSid,
        tvArchive = tvArchive,
        directSource = directSource,
        tvArchiveDuration = tvArchiveDuration,
        position = position
    )
}

// Category conversions
fun CategoryEntity.toCategory(): Category {
    return Category(
        categoryId = categoryId,
        categoryName = categoryName,
        parentId = parentId
    )
}

fun Category.toEntity(categoryType: String): CategoryEntity {
    return CategoryEntity(
        categoryId = categoryId,
        categoryName = categoryName,
        parentId = parentId,
        categoryType = categoryType
    )
}

// Movie conversions
fun MovieEntity.toMovie(isFavorite: Boolean = false): Movie {
    return Movie(
        streamId = streamId,
        name = name,
        streamIcon = streamIcon,
        rating = rating,
        year = year,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        durationSecs = durationSecs,
        duration = duration,
        containerExtension = containerExtension,
        categoryId = categoryId,
        isFavorite = isFavorite
    )
}

fun Movie.toEntity(): MovieEntity {
    return MovieEntity(
        streamId = streamId,
        name = name,
        streamIcon = streamIcon,
        rating = rating,
        year = year,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        durationSecs = durationSecs,
        duration = duration,
        containerExtension = containerExtension,
        categoryId = categoryId
    )
}

// Series conversions
fun SeriesEntity.toSeries(isFavorite: Boolean = false): Series {
    return Series(
        seriesId = seriesId,
        name = name,
        cover = cover,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        rating = rating,
        categoryId = categoryId,
        isFavorite = isFavorite
    )
}

fun Series.toEntity(): SeriesEntity {
    return SeriesEntity(
        seriesId = seriesId,
        name = name,
        cover = cover,
        plot = plot,
        cast = cast,
        director = director,
        genre = genre,
        releaseDate = releaseDate,
        rating = rating,
        categoryId = categoryId
    )
}

// Episode conversions
fun EpisodeEntity.toEpisode(): Episode {
    return Episode(
        id = id,
        episodeNum = episodeNum,
        title = title,
        containerExtension = containerExtension,
        info = if (plot != null || duration != null || rating != null) {
            EpisodeInfo(plot, duration, rating)
        } else null,
        seasonNumber = seasonNumber
    )
}

fun Episode.toEntity(seriesId: String): EpisodeEntity {
    return EpisodeEntity(
        id = id,
        seriesId = seriesId,
        episodeNum = episodeNum,
        title = title,
        containerExtension = containerExtension,
        plot = info?.plot,
        duration = info?.duration,
        rating = info?.rating,
        seasonNumber = seasonNumber
    )
}

// Recent conversions
fun RecentEntity.toRecent(): Recent {
    return Recent(
        itemId = itemId,
        itemType = itemType,
        timestamp = viewedAt
    )
}

fun Recent.toEntity(): RecentEntity {
    return RecentEntity(
        itemId = itemId,
        itemType = itemType,
        viewedAt = timestamp
    )
}

// Extended conversions for UI display
fun FavoriteEntity.toFavoriteItem(): FavoriteItem {
    return FavoriteItem(
        contentId = itemId,
        name = name,
        type = itemType,
        icon = icon,
        categoryId = categoryId,
        timestamp = addedAt
    )
}

fun RecentEntity.toRecentItem(): RecentItem {
    return RecentItem(
        contentId = itemId,
        name = name,
        type = itemType,
        icon = icon,
        categoryId = categoryId,
        timestamp = viewedAt
    )
}
