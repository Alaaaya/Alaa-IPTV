package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.ui.main.SimpleLiveLayoutPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Aya5LayoutPolicyTest {
    @Test
    fun `aya 5 policy is exclusive to its theme identifier`() {
        assertTrue(Aya5LayoutPolicy.isEnabled(AppPreferences.THEME_AYA_5))
        assertFalse(Aya5LayoutPolicy.isEnabled(AppPreferences.THEME_AYA_3))
    }

    @Test
    fun `aya 5 preserves a three column live tv flow through display theme`() {
        val theme = AppPreferences.THEME_AYA_5
        val category = DisplayTheme.liveCategorySpec(theme)
        val channel = DisplayTheme.channelRowSpec(theme)
        val dashboard = DisplayTheme.dashboardCardSpec(theme)

        assertEquals(DisplayTheme.LiveCategoryPlacement.SIDE_LIST, category.placement)
        assertEquals(0.44f, category.sideWidthPercent)
        assertEquals(48, category.itemHeightDp)
        assertEquals(50, channel.heightDp)
        assertEquals(10, channel.horizontalPaddingDp)
        assertTrue(channel.showNumber)
        assertTrue(channel.showQuality)
        assertEquals(40, channel.logoWidthDp)
        assertEquals(28, channel.logoHeightDp)
        assertEquals(184, dashboard.widthDp)
        assertEquals(116, dashboard.heightDp)
        assertEquals(10f, dashboard.radiusDp)
        assertEquals(5, DisplayTheme.mediaGridSpan(theme))
        assertTrue(SimpleLiveLayoutPolicy.usesQuietRows(theme))
    }
}
