package com.alaa.iptv.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureCatalogTest {
    @Test
    fun `contains every selected optional feature exactly once`() {
        assertEquals(61, FeatureCatalog.options.size)
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
    }

    @Test
    fun `enables the requested day to day features by default`() {
        assertTrue(FeatureCatalog.option(FeatureCatalog.GLOBAL_SEARCH).defaultEnabled)
        assertTrue(FeatureCatalog.option(FeatureCatalog.WATCHLIST).defaultEnabled)
        assertTrue(FeatureCatalog.option(FeatureCatalog.AUTO_RECONNECT).defaultEnabled)
    }
}
