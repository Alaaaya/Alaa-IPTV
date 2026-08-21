package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** هندسة Aya: بلاطات منزلية واسعة، معاينة بث كبيرة، وقوائم عالية الوضوح للتلفزيون. */
object AyaLayoutPolicy {
    const val LIVE_CONTENT_PANEL_WIDTH = 0.55f
    const val LIVE_CATEGORY_WIDTH = 0.35f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 60
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 64
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 196
    const val DASHBOARD_HERO_HEIGHT_DP = 234
    const val MOVIE_CATEGORY_WIDTH_DP = 292

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_AYA
}
