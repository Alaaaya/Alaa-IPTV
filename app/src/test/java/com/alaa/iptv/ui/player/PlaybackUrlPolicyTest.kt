package com.alaa.iptv.ui.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackUrlPolicyTest {
    @Test
    fun `accepts complete HTTPS playback URLs for live movies and series`() {
        assertEquals(
            "https://stream.example.test/live/user/pass/1.ts",
            PlaybackUrlPolicy.normalizedHttpsUrlOrNull("  https://stream.example.test/live/user/pass/1.ts  ")
        )
        assertEquals(
            "https://stream.example.test:8443/live/user/pass/3.ts?token=abc",
            PlaybackUrlPolicy.normalizedHttpsUrlOrNull("https://stream.example.test:8443/live/user/pass/3.ts?token=abc")
        )
        assertEquals(
            "https://stream.example.test/series/user/pass/4.mkv",
            PlaybackUrlPolicy.normalizedHttpsUrlOrNull("https://stream.example.test/series/user/pass/4.mkv")
        )
    }

    @Test
    fun `rejects cleartext empty malformed and unsupported playback URLs`() {
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull("http://stream.example.test/movie/user/pass/2.mp4"))
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull(null))
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull("   "))
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull("not a url"))
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull("file:///sdcard/video.mp4"))
        assertNull(PlaybackUrlPolicy.normalizedHttpsUrlOrNull("https:///missing-host"))
    }
}
