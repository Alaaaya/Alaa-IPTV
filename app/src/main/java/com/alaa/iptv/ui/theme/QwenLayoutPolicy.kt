package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** هندسة Qwen: طبقات زرقاء هادئة، فئات جانبية ثابتة، ومعاينة رحبة قابلة للقراءة من مسافة التلفزيون. */
object QwenLayoutPolicy {
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 272
    const val DASHBOARD_HERO_HEIGHT_DP = 286
    const val LIVE_CONTENT_PANEL_WIDTH = 0.55f
    const val LIVE_CATEGORY_WIDTH = 0.36f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 62
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 66
    const val MOVIE_CATEGORY_WIDTH_DP = 264

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_QWEN
}
