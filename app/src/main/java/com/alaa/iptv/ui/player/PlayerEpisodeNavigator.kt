package com.alaa.iptv.ui.player

data class PlayableEpisode(val name: String, val streamUrl: String)

object PlayerEpisodeNavigator {
    private var episodes: List<PlayableEpisode> = emptyList()

    fun setEpisodes(items: List<PlayableEpisode>) {
        episodes = items
    }

    fun episodeAt(index: Int): PlayableEpisode? = episodes.getOrNull(index)
}
