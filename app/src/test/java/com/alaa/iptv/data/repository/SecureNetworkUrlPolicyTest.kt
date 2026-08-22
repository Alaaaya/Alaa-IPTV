package com.alaa.iptv.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureNetworkUrlPolicyTest {
    @Test
    fun `normalizes a host without scheme to HTTPS`() {
        assertEquals(
            "https://iptv.example.test",
            SecureNetworkUrlPolicy.normalizeServerUrlOrNull("iptv.example.test/player_api.php?username=user")
        )
    }

    @Test
    fun `accepts HTTPS and explicit HTTP while rejecting malformed remote sources`() {
        assertTrue(SecureNetworkUrlPolicy.isAllowedRemoteUrl("https://iptv.example.test:8443"))
        assertTrue(SecureNetworkUrlPolicy.isAllowedRemoteUrl("http://iptv.example.test"))
        assertFalse(SecureNetworkUrlPolicy.isAllowedRemoteUrl("rtmp://iptv.example.test/live"))
        assertFalse(SecureNetworkUrlPolicy.isAllowedRemoteUrl("https:///missing-host"))
        assertFalse(SecureNetworkUrlPolicy.isAllowedRemoteUrl("http://127.0.0.1:8080"))
    }

    @Test
    fun `allows HTTP HTTPS or local file M3U playlists`() {
        assertTrue(SecureNetworkUrlPolicy.isAllowedPlaylistUrl("https://iptv.example.test/list.m3u"))
        assertTrue(SecureNetworkUrlPolicy.isAllowedPlaylistUrl("file:///data/user/0/com.alaa.iptv/files/playlists/imported.m3u"))
        assertTrue(SecureNetworkUrlPolicy.isAllowedPlaylistUrl("http://iptv.example.test/list.m3u"))
        assertFalse(SecureNetworkUrlPolicy.isAllowedPlaylistUrl("content://untrusted.provider/list.m3u"))
    }
}
