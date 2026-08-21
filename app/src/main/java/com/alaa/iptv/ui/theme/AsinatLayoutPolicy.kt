package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** مقاييس Asinat الأصلية لشاشات Android TV، مع الحفاظ على اتجاه LTR والتنقل بالريموت. */
object AsinatLayoutPolicy {
    const val LIVE_CONTENT_PANEL_WIDTH = 0.60f
    const val LIVE_CATEGORY_WIDTH = 0.40f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 52
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 54
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 248
    const val DASHBOARD_HERO_HEIGHT_DP = 270

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_ASINAT
}
