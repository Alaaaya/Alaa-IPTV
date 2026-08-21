package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Aya3LayoutPolicyTest {
    @Test
    fun `aya 3 policy is exclusive to its theme identifier`() {
        assertTrue(Aya3LayoutPolicy.isEnabled(AppPreferences.THEME_AYA_3))
        assertFalse(Aya3LayoutPolicy.isEnabled(AppPreferences.THEME_AYA_2))
    }

    @Test
    fun `aya 3 exposes its dark red tv geometry through display theme`() {
        val theme = AppPreferences.THEME_AYA_3
        val category = DisplayTheme.liveCategorySpec(theme)
        val channel = DisplayTheme.channelRowSpec(theme)
        val dashboard = DisplayTheme.dashboardCardSpec(theme)

        assertEquals(DisplayTheme.LiveCategoryPlacement.SIDE_LIST, category.placement)
        assertEquals(0.38f, category.sideWidthPercent)
        assertEquals(54, category.itemHeightDp)
        assertEquals(62, channel.heightDp)
        assertEquals(12, channel.horizontalPaddingDp)
        assertTrue(channel.showNumber)
        assertTrue(channel.showQuality)
        assertEquals(42, channel.logoWidthDp)
        assertEquals(30, channel.logoHeightDp)
        assertEquals(188, dashboard.widthDp)
        assertEquals(122, dashboard.heightDp)
        assertEquals(12f, dashboard.radiusDp)
        assertEquals(5, DisplayTheme.mediaGridSpan(theme))
    }
}
