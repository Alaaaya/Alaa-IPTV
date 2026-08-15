package com.alaa.iptv.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteChannelCodecTest {
    @Test
    fun `favorite channel details survive encode and decode`() {
        val source = Channel(
            streamId = "77", num = "7", name = "Arabic Sports", streamType = "live",
            streamIcon = "https://example.test/logo.png", epgChannelId = null, added = null,
            categoryId = "sports", categoryName = "Sports", customSid = null,
            directSource = "https://example.test/live/77.ts", isFavorite = true
        )
        val restored = FavoriteChannelCodec.decode(FavoriteChannelCodec.encode(listOf(source))).single()
        assertEquals(source.streamId, restored.streamId)
        assertEquals(source.name, restored.name)
        assertEquals(source.directSource, restored.directSource)
        assertTrue(restored.isFavorite)
    }
}
