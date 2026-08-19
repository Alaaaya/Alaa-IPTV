package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveCompactLayoutSpecTest {
    @Test
    fun `neon live layout keeps category and channel columns compact`() {
        val categories = DisplayTheme.liveCategorySpec(AppPreferences.THEME_ALAA_NEON_IPTV)
        val channels = DisplayTheme.channelRowSpec(AppPreferences.THEME_ALAA_NEON_IPTV)

        assertEquals(0.46f, categories.sideWidthPercent)
        assertEquals(54, categories.itemHeightDp)
        assertEquals(54, channels.heightDp)
        assertEquals(44, channels.logoWidthDp)
    }

    @Test
    fun `classic live layout uses a readable compact default`() {
        val categories = DisplayTheme.liveCategorySpec(AppPreferences.THEME_ALAA_CLASSIC)
        val channels = DisplayTheme.channelRowSpec(AppPreferences.THEME_ALAA_CLASSIC)

        assertEquals(0.44f, categories.sideWidthPercent)
        assertEquals(52, channels.heightDp)
    }
}
