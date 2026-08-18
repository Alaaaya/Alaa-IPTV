package com.alaa.iptv.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateArtifactPolicyTest {
    @Test
    fun `recognizes newer semantic version`() {
        assertTrue(UpdateArtifactPolicy.isNewerVersion("2.7.8", "2.7.7"))
        assertTrue(UpdateArtifactPolicy.isNewerVersion("v2.8.0", "2.7.9"))
        assertFalse(UpdateArtifactPolicy.isNewerVersion("2.7.7", "2.7.7"))
        assertFalse(UpdateArtifactPolicy.isNewerVersion("invalid", "2.7.7"))
    }

    @Test
    fun `accepts only official Alaa Player release APK url`() {
        assertTrue(
            UpdateArtifactPolicy.isTrustedDownloadUrl(
                "https://github.com/Alaaaya/Alaa-IPTV/releases/download/v2.7.8/AlaaPlayer-2.7.8.apk"
            )
        )
        assertFalse(UpdateArtifactPolicy.isTrustedDownloadUrl("http://github.com/Alaaaya/Alaa-IPTV/releases/download/v2.7.8/AlaaPlayer-2.7.8.apk"))
        assertFalse(UpdateArtifactPolicy.isTrustedDownloadUrl("https://example.com/AlaaPlayer-2.7.8.apk"))
        assertFalse(UpdateArtifactPolicy.isTrustedDownloadUrl("https://github.com/Alaaaya/Alaa-IPTV/releases/download/v2.7.8/other.apk"))
    }
}
