package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/** هندسة Asinat 2: معاينة أعرض، قوائم مقروءة، وتركيز ثابت لشاشات Android TV. */
object Asinat2LayoutPolicy {
    const val LIVE_CONTENT_PANEL_WIDTH = 0.58f
    const val LIVE_CATEGORY_WIDTH = 0.42f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 56
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 58
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 222
    const val DASHBOARD_HERO_HEIGHT_DP = 246

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_ASINAT_2
}
