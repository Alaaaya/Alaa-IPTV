package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AyaLayoutPolicyTest {
    @Test
    fun `Aya uses its own large tile and live geometry`() {
        val theme = AppPreferences.THEME_AYA

        assertTrue(AyaLayoutPolicy.isEnabled(theme))
        assertFalse(AyaLayoutPolicy.isEnabled(AppPreferences.THEME_ASINAT_2))
        assertEquals(0.55f, AyaLayoutPolicy.LIVE_CONTENT_PANEL_WIDTH)
        assertEquals(0.35f, AyaLayoutPolicy.LIVE_CATEGORY_WIDTH)
        assertEquals(60, DisplayTheme.liveCategorySpec(theme).itemHeightDp)
        assertEquals(64, DisplayTheme.channelRowSpec(theme).heightDp)
        assertEquals(4, DisplayTheme.mediaGridSpan(theme))
    }
}
