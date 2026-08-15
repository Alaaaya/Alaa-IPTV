package com.alaa.iptv.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class M3UCategoryMapperTest {
    private fun channel(id: String, category: String) = Channel(
        streamId = id, num = id, name = "Channel $id", streamType = "live", streamIcon = null,
        epgChannelId = null, added = null, categoryId = category, categoryName = category,
        customSid = null, directSource = "https://example.test/$id.ts"
    )

    @Test
    fun `categories are derived from playlist group titles`() {
        val categories = M3UCategoryMapper.categories(
            listOf(channel("1", "Arabic"), channel("2", "Arabic"), channel("3", "Sports"))
        )
        assertEquals(listOf("Arabic", "Sports"), categories.map { it.categoryName })
        assertEquals(listOf(2, 1), categories.map { it.channelCount })
    }

    @Test
    fun `page only returns channels belonging to the selected category`() {
        val channels = listOf(channel("1", "Arabic"), channel("2", "Sports"), channel("3", "Arabic"))
        assertEquals(listOf("1", "3"), M3UCategoryMapper.page(channels, "Arabic", 0, 100).map { it.streamId })
    }
}
