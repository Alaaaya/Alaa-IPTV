package com.alaa.iptv.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamUrlFactoryTest {
    @Test
    fun `live stream uses direct transport stream path`() {
        assertEquals(
            "http://provider.example/live/user/pass/42.ts",
            StreamUrlFactory.live("http://provider.example/", "user", "pass", "42")
        )
    }

    @Test
    fun `movie preserves provider container extension`() {
        assertEquals(
            "http://provider.example/movie/user/pass/99.mkv",
            StreamUrlFactory.movie("http://provider.example", "user", "pass", "99", "mkv")
        )
    }

    @Test
    fun `episode removes a leading extension dot and defaults when absent`() {
        assertEquals(
            "http://provider.example/series/user/pass/10.avi",
            StreamUrlFactory.episode("http://provider.example", "user", "pass", "10", ".avi")
        )
        assertEquals(
            "http://provider.example/series/user/pass/11.mp4",
            StreamUrlFactory.episode("http://provider.example", "user", "pass", "11", null)
        )
    }
}
