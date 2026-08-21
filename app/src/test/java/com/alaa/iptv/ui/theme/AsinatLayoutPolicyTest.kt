package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AsinatLayoutPolicyTest {
    @Test
    fun `Asinat defines a readable three-column live composition`() {
        assertTrue(AsinatLayoutPolicy.isEnabled(AppPreferences.THEME_ASINAT))
        assertEquals(0.60f, AsinatLayoutPolicy.LIVE_CONTENT_PANEL_WIDTH)
        assertEquals(0.40f, AsinatLayoutPolicy.LIVE_CATEGORY_WIDTH)
        assertEquals(52, AsinatLayoutPolicy.LIVE_CATEGORY_ROW_HEIGHT_DP)
        assertEquals(54, AsinatLayoutPolicy.LIVE_CHANNEL_ROW_HEIGHT_DP)
    }

    @Test
    fun `Asinat is registered with live and dashboard specifications`() {
        val live = DisplayTheme.liveCategorySpec(AppPreferences.THEME_ASINAT)
        val channel = DisplayTheme.channelRowSpec(AppPreferences.THEME_ASINAT)
        val dashboard = DisplayTheme.dashboardCardSpec(AppPreferences.THEME_ASINAT)

        assertEquals(AsinatLayoutPolicy.LIVE_CATEGORY_WIDTH, live.sideWidthPercent)
        assertEquals(AsinatLayoutPolicy.LIVE_CHANNEL_ROW_HEIGHT_DP, channel.heightDp)
        assertEquals(190, dashboard.widthDp)
    }
}
