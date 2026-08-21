package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Asinat2LayoutPolicyTest {
    @Test
    fun `Asinat 2 owns a distinct live hierarchy`() {
        val theme = AppPreferences.THEME_ASINAT_2

        assertTrue(Asinat2LayoutPolicy.isEnabled(theme))
        assertFalse(Asinat2LayoutPolicy.isEnabled(AppPreferences.THEME_ASINAT))
        assertEquals(0.58f, Asinat2LayoutPolicy.LIVE_CONTENT_PANEL_WIDTH)
        assertEquals(0.42f, Asinat2LayoutPolicy.LIVE_CATEGORY_WIDTH)
        assertEquals(56, DisplayTheme.liveCategorySpec(theme).itemHeightDp)
        assertEquals(58, DisplayTheme.channelRowSpec(theme).heightDp)
        assertEquals(6, DisplayTheme.mediaGridSpan(theme))
    }
}
