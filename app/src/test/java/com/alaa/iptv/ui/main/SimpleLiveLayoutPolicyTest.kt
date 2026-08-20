package com.alaa.iptv.ui.main

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleLiveLayoutPolicyTest {
    @Test
    fun `latest Alaa design uses the calm compact live layout`() {
        assertTrue(SimpleLiveLayoutPolicy.isEnabled(AppPreferences.THEME_ALAA_NEON_IPTV))
        assertFalse(SimpleLiveLayoutPolicy.isEnabled(AppPreferences.THEME_ALAA_CLASSIC))
        assertEquals(0.32f, SimpleLiveLayoutPolicy.CATEGORY_SIDE_WIDTH)
        assertEquals(48, SimpleLiveLayoutPolicy.CATEGORY_ROW_HEIGHT_DP)
        assertEquals(48, SimpleLiveLayoutPolicy.CHANNEL_ROW_HEIGHT_DP)
    }

    @Test
    fun `category metadata remains a short count only`() {
        assertEquals("12", SimpleLiveLayoutPolicy.categoryMeta(12))
        assertEquals("", SimpleLiveLayoutPolicy.categoryMeta(0))
    }
}
