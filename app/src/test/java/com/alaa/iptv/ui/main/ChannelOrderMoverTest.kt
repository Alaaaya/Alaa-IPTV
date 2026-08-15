package com.alaa.iptv.ui.main

import com.alaa.iptv.data.models.Channel
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelOrderMoverTest {
    private fun channel(id: String) = Channel(
        streamId = id, num = id, name = "Channel $id", streamType = "live", streamIcon = null,
        epgChannelId = null, added = null, categoryId = "1", categoryName = null,
        customSid = null, directSource = null
    )

    @Test
    fun `move lets a channel travel from a later row to the first row`() {
        val initial = listOf(channel("1"), channel("2"), channel("3"), channel("4"))
        val moved = ChannelOrderMover.move(initial, "live:4", -3)
        assertEquals(listOf("4", "1", "2", "3"), moved.map { it.streamId })
    }

    @Test
    fun `move clamps at the top and preserves the current order`() {
        val initial = listOf(channel("1"), channel("2"))
        assertEquals(initial, ChannelOrderMover.move(initial, "live:1", -1))
    }
}
