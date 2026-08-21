package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QwenThemeContractTest {
    @Test
    fun qwenIsSelectableAndUsesItsOwnTvLayoutPolicy() {
        assertTrue(ThemeCatalog.options.any { it.id == AppPreferences.THEME_QWEN && it.title == "Qwen" })
        assertTrue(QwenLayoutPolicy.isEnabled(AppPreferences.THEME_QWEN))
        assertEquals(QwenLayoutPolicy.LIVE_CHANNEL_ROW_HEIGHT_DP, DisplayTheme.channelRowSpec(AppPreferences.THEME_QWEN).heightDp)
        assertEquals(QwenLayoutPolicy.LIVE_CATEGORY_WIDTH, DisplayTheme.liveCategorySpec(AppPreferences.THEME_QWEN).sideWidthPercent)
        assertEquals(4, DisplayTheme.mediaGridSpan(AppPreferences.THEME_QWEN))
    }
}
