package com.alaa.iptv.data.models

import java.nio.charset.StandardCharsets
import java.net.URLDecoder
import java.net.URLEncoder

object FavoriteChannelCodec {
    fun encode(channels: List<Channel>): String = channels.joinToString("\n") { channel ->
        listOf(
            channel.streamId, channel.num, channel.name, channel.streamType, channel.streamIcon.orEmpty(),
            channel.categoryId.orEmpty(), channel.categoryName.orEmpty(), channel.directSource.orEmpty(),
            channel.tvArchive.toString(), channel.tvArchiveDuration.toString()
        ).joinToString(",") { value -> URLEncoder.encode(value, StandardCharsets.UTF_8.name()) }
    }

    fun decode(value: String?): List<Channel> = value.orEmpty().lineSequence().mapNotNull { row ->
        try {
            val fields = row.split(",")
            if (fields.size != 10) return@mapNotNull null
            fun field(index: Int) = URLDecoder.decode(fields[index], StandardCharsets.UTF_8.name())
            val id = field(0)
            if (id.isBlank()) return@mapNotNull null
            Channel(
                streamId = id, num = field(1), name = field(2).ifBlank { "Channel $id" },
                streamType = field(3).ifBlank { "live" }, streamIcon = field(4).takeIf { it.isNotBlank() },
                epgChannelId = null, added = null, categoryId = field(5).takeIf { it.isNotBlank() },
                categoryName = field(6).takeIf { it.isNotBlank() }, customSid = null,
                tvArchive = field(8).toIntOrNull() ?: 0, directSource = field(7).takeIf { it.isNotBlank() },
                tvArchiveDuration = field(9).toIntOrNull() ?: 0, isFavorite = true
            )
        } catch (_: Exception) {
            null
        }
    }.toList()
}
