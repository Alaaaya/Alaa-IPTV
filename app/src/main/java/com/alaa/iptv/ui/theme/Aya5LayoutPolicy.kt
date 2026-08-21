package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

/**
 * هندسة Aya 5: قوائم بث عمودية مركزة للفئات والقنوات، مع معاينة واسعة للقناة.
 * النسب تعطي تقريباً 25% للفئات، 33% للقنوات، و42% للمعاينة على شاشة التلفاز.
 */
object Aya5LayoutPolicy {
    const val DASHBOARD_SIDEBAR_WIDTH_DP = 228
    const val DASHBOARD_HERO_HEIGHT_DP = 268
    const val LIVE_CONTENT_PANEL_WIDTH = 0.58f
    const val LIVE_CATEGORY_WIDTH = 0.44f
    const val LIVE_CATEGORY_ROW_HEIGHT_DP = 48
    const val LIVE_CHANNEL_ROW_HEIGHT_DP = 50
    const val MOVIE_CATEGORY_WIDTH_DP = 244

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_AYA_5
}
