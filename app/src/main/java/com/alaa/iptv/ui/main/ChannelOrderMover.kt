package com.alaa.iptv.ui.main

import com.alaa.iptv.data.models.Channel

object ChannelOrderMover {
    fun move(channels: List<Channel>, channelKey: String, offset: Int): List<Channel> {
        val currentIndex = channels.indexOfFirst { keyFor(it) == channelKey }
        if (currentIndex == -1) return channels
        val targetIndex = (currentIndex + offset).coerceIn(0, channels.lastIndex)
        if (targetIndex == currentIndex) return channels

        return channels.toMutableList().apply {
            add(targetIndex, removeAt(currentIndex))
        }.mapIndexed { index, channel -> channel.copy(position = index) }
    }

    fun keyFor(channel: Channel): String = "${channel.streamType.lowercase()}:${channel.streamId}"
}
