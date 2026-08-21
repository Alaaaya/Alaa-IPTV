package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** هندسة Aya 3: صفوف بث متوازنة، معاينة واسعة، وألواح داكنة بحافة قرمزية ثابتة. */
object Aya3LayoutPolicy {
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 240
    const val DASHBOARD_HERO_HEIGHT_DP = 276
    const val LIVE_CONTENT_PANEL_WIDTH = 0.54f
    const val LIVE_CATEGORY_WIDTH = 0.38f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 54
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 62
    const val MOVIE_CATEGORY_WIDTH_DP = 260

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_AYA_3
}
