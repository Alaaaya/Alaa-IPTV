package com.alaa.iptv.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IptvSourceClassifierTest {
    @Test
    fun `plain Xtream host is not a playlist`() {
        assertFalse(IptvSourceClassifier.isM3U("http://pro.4vstrong.com"))
    }

    @Test
    fun `explicit playlist endpoints are recognized`() {
        assertTrue(IptvSourceClassifier.isM3U("http://host.test/get.php?username=u&password=p&type=m3u_plus"))
        assertTrue(IptvSourceClassifier.isM3U("http://host.test/playlist/u/p/m3u"))
        assertTrue(IptvSourceClassifier.isM3U("file:///data/user/0/app/imported_playlist.m3u"))
    }

    @Test
    fun `host name containing m3u text is not falsely classified`() {
        assertFalse(IptvSourceClassifier.isM3U("http://m3u-provider.example"))
    }
}
