package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** هندسة Aya 2: مساحة منزلية واسعة، فئات جانبية واضحة، وبطاقات وردية بنفسجية عالية التباين. */
object Aya2LayoutPolicy {
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 280
    const val DASHBOARD_HERO_HEIGHT_DP = 300
    const val LIVE_CONTENT_PANEL_WIDTH = 0.52f
    const val LIVE_CATEGORY_WIDTH = 0.40f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 58
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 64
    const val MOVIE_CATEGORY_WIDTH_DP = 270

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_AYA_2
}
