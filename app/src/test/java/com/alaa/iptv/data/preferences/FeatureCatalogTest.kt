package com.alaa.iptv.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureCatalogTest {
    @Test
    fun `contains every selected optional feature exactly once`() {
        assertEquals(73, FeatureCatalog.options.size)
        assertEquals(FeatureCatalog.options.size, FeatureCatalog.options.map { it.id }.toSet().size)
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.CONTENT_RELOAD })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.MANUAL_SYNC })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.SAFE_ERROR_LOG })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.IMAGE_CACHE_CLEAR })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.DATA_SAVER })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.GUEST_MODE })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.NAVIGATION_SOUNDS })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.IDLE_REMINDER })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.SAFE_SUPPORT_REPORT })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LOW_LATENCY_MODE })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIVE_FAVORITES })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIVE_CHANNEL_MOVE })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIVE_NUMBER_JUMP })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.PLAYER_AUDIO_TRACKS })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.PLAYER_SUBTITLES })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.PLAYER_BACKGROUND_AUDIO })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIVE_AUDIO_ONLY })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.PLAYER_SUBTITLE_STYLE })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.PLAYER_AUTO_PLAY })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIBRARY_SIMILAR })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.LIBRARY_TRAILERS })
        assertTrue(FeatureCatalog.options.any { it.id == FeatureCatalog.IN_APP_UPDATES })
    }

    @Test
    fun `keeps sensitive features disabled until the owner chooses them`() {
        assertFalse(FeatureCatalog.option(FeatureCatalog.PARENTAL_PIN).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PROFILES).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.MULTI_SUBSCRIPTIONS).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.ENCRYPTED_BACKUP).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.OWNER_ALERTS).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.QUICK_START).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.START_SCREEN).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.DATA_SAVER).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.GUEST_MODE).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LOW_LATENCY_MODE).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIVE_FAVORITES).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIVE_CHANNEL_MOVE).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIVE_NUMBER_JUMP).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PLAYER_AUDIO_TRACKS).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PLAYER_SUBTITLES).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PLAYER_BACKGROUND_AUDIO).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIVE_AUDIO_ONLY).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PLAYER_SUBTITLE_STYLE).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.PLAYER_AUTO_PLAY).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIBRARY_SIMILAR).defaultEnabled)
        assertFalse(FeatureCatalog.option(FeatureCatalog.LIBRARY_TRAILERS).defaultEnabled)
    }

    @Test
    fun `enables the requested day to day features by default`() {
        assertTrue(FeatureCatalog.option(FeatureCatalog.GLOBAL_SEARCH).defaultEnabled)
        assertTrue(FeatureCatalog.option(FeatureCatalog.WATCHLIST).defaultEnabled)
        assertTrue(FeatureCatalog.option(FeatureCatalog.AUTO_RECONNECT).defaultEnabled)
    }
}
