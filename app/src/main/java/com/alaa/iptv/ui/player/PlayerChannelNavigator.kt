package com.alaa.iptv.ui.player

data class PlayableChannel(
    val name: String,
    val streamUrl: String,
    val streamType: String
)

object PlayerChannelNavigator {
    private var channels: List<PlayableChannel> = emptyList()

    fun setChannels(items: List<PlayableChannel>) {
        channels = items
    }

    fun channelAt(index: Int): PlayableChannel? = channels.getOrNull(index)

    fun size(): Int = channels.size
}
