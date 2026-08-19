package com.alaa.iptv.ui.main

import com.alaa.iptv.data.models.Episode

/** يحافظ على ترتيب مواسم وحلقات المصدر ولا يخلط أرقام المواسم عند الانتقال. */
object SeriesSeasonPolicy {
    fun seasonsOf(episodes: List<Episode>): List<Int> = episodes.map { it.seasonNumber }.distinct().sorted()

    fun episodesForSeason(episodes: List<Episode>, seasonNumber: Int): List<Episode> =
        episodes.filter { it.seasonNumber == seasonNumber }.sortedBy { it.episodeNum }
}
