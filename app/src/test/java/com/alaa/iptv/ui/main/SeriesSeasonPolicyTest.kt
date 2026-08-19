package com.alaa.iptv.ui.main

import com.alaa.iptv.data.models.Episode
import org.junit.Assert.assertEquals
import org.junit.Test

class SeriesSeasonPolicyTest {
    @Test
    fun `seasons and episodes remain in natural numeric order`() {
        val episodes = listOf(
            episode(season = 2, number = 3),
            episode(season = 1, number = 2),
            episode(season = 1, number = 1)
        )

        assertEquals(listOf(1, 2), SeriesSeasonPolicy.seasonsOf(episodes))
        assertEquals(listOf(1, 2), SeriesSeasonPolicy.episodesForSeason(episodes, 1).map { it.episodeNum })
    }

    private fun episode(season: Int, number: Int) = Episode(
        id = "$season-$number",
        episodeNum = number,
        title = "Episode $number",
        containerExtension = "mp4",
        info = null,
        seasonNumber = season
    )
}
