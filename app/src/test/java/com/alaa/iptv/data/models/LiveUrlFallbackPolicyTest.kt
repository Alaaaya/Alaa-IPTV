package com.alaa.iptv.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveUrlFallbackPolicyTest {
    @Test
    fun `converts ts to hls while preserving query parameters`() {
        assertEquals(
            "http://example.test/live/user/pass/1.m3u8?token=abc",
            LiveUrlFallbackPolicy.nextAlternative(
                "http://example.test/live/user/pass/1.ts?token=abc",
                setOf("http://example.test/live/user/pass/1.ts?token=abc")
            )
        )
    }

    @Test
    fun `does not retry a previously attempted alternate url`() {
        val tsUrl = "http://example.test/live/user/pass/1.ts"
        val hlsUrl = "http://example.test/live/user/pass/1.m3u8"
        assertNull(LiveUrlFallbackPolicy.nextAlternative(hlsUrl, setOf(tsUrl, hlsUrl)))
    }

    @Test
    fun `does not invent fallback for unknown containers`() {
        assertNull(LiveUrlFallbackPolicy.nextAlternative("http://example.test/live/1.mp4", emptySet()))
    }
}
