package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Aya2LayoutPolicyTest {
    @Test
    fun `aya 2 policy is exclusive to its theme identifier`() {
        assertTrue(Aya2LayoutPolicy.isEnabled(AppPreferences.THEME_AYA_2))
        assertFalse(Aya2LayoutPolicy.isEnabled(AppPreferences.THEME_AYA))
    }

    @Test
    fun `aya 2 exposes its required tv geometry through display theme`() {
        val theme = AppPreferences.THEME_AYA_2
        val category = DisplayTheme.liveCategorySpec(theme)
        val channel = DisplayTheme.channelRowSpec(theme)
        val dashboard = DisplayTheme.dashboardCardSpec(theme)

        assertEquals(DisplayTheme.LiveCategoryPlacement.SIDE_LIST, category.placement)
        assertEquals(0.40f, category.sideWidthPercent)
        assertEquals(58, category.itemHeightDp)
        assertEquals(64, channel.heightDp)
        assertEquals(14, channel.horizontalPaddingDp)
        assertTrue(channel.showNumber)
        assertTrue(channel.showQuality)
        assertEquals(48, channel.logoWidthDp)
        assertEquals(30, channel.logoHeightDp)
        assertEquals(210, dashboard.widthDp)
        assertEquals(136, dashboard.heightDp)
        assertEquals(18f, dashboard.radiusDp)
        assertEquals(4, DisplayTheme.mediaGridSpan(theme))
    }
}
