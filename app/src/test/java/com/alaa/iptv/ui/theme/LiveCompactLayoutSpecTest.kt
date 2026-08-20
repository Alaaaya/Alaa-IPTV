package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.ui.main.SimpleLiveLayoutPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveCompactLayoutSpecTest {
    @Test
    fun `neon live layout keeps category and channel columns compact`() {
        val categories = DisplayTheme.liveCategorySpec(AppPreferences.THEME_ALAA_NEON_IPTV)
        val channels = DisplayTheme.channelRowSpec(AppPreferences.THEME_ALAA_NEON_IPTV)

        assertEquals(SimpleLiveLayoutPolicy.CATEGORY_SIDE_WIDTH, categories.sideWidthPercent)
        assertEquals(SimpleLiveLayoutPolicy.CATEGORY_ROW_HEIGHT_DP, categories.itemHeightDp)
        assertEquals(SimpleLiveLayoutPolicy.CHANNEL_ROW_HEIGHT_DP, channels.heightDp)
        assertEquals(38, channels.logoWidthDp)
    }

    @Test
    fun `classic live layout uses a readable compact default`() {
        val categories = DisplayTheme.liveCategorySpec(AppPreferences.THEME_ALAA_CLASSIC)
        val channels = DisplayTheme.channelRowSpec(AppPreferences.THEME_ALAA_CLASSIC)

        assertEquals(0.44f, categories.sideWidthPercent)
        assertEquals(52, channels.heightDp)
    }
}
