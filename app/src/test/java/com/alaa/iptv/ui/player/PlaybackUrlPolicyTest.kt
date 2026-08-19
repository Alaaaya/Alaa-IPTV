package com.alaa.iptv.ui.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackUrlPolicyTest {
    @Test
    fun `accepts complete HTTP and HTTPS playback URLs for live movies and series`() {
        assertEquals(
            "https://stream.example.test/live/user/pass/1.ts",
            PlaybackUrlPolicy.normalizedHttpUrlOrNull("  https://stream.example.test/live/user/pass/1.ts  ")
        )
        assertEquals(
            "http://stream.example.test/movie/user/pass/2.mp4",
            PlaybackUrlPolicy.normalizedHttpUrlOrNull("http://stream.example.test/movie/user/pass/2.mp4")
        )
        assertEquals(
            "https://stream.example.test:8443/live/user/pass/3.ts?token=abc",
            PlaybackUrlPolicy.normalizedHttpUrlOrNull("https://stream.example.test:8443/live/user/pass/3.ts?token=abc")
        )
        assertEquals(
            "https://stream.example.test/series/user/pass/4.mkv",
            PlaybackUrlPolicy.normalizedHttpUrlOrNull("https://stream.example.test/series/user/pass/4.mkv")
        )
    }

    @Test
    fun `rejects empty malformed and unsupported playback URLs`() {
        assertNull(PlaybackUrlPolicy.normalizedHttpUrlOrNull(null))
        assertNull(PlaybackUrlPolicy.normalizedHttpUrlOrNull("   "))
        assertNull(PlaybackUrlPolicy.normalizedHttpUrlOrNull("not a url"))
        assertNull(PlaybackUrlPolicy.normalizedHttpUrlOrNull("file:///sdcard/video.mp4"))
        assertNull(PlaybackUrlPolicy.normalizedHttpUrlOrNull("https:///missing-host"))
    }
}
