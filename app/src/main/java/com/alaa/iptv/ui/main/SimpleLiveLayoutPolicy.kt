package com.alaa.iptv.ui.main

import com.alaa.iptv.data.preferences.AppPreferences

/** قواعد التخطيط الهادئ لواجهة البث في أحدث تصميم Alaa. */
object SimpleLiveLayoutPolicy {
    /** يسار 25.2%، وسط 37.8%، ومعاينة 37% من شاشة البث كما في المرجع. */
    const val CONTENT_PANEL_WIDTH = 0.63f
    const val CATEGORY_SIDE_WIDTH = 0.40f
    const val CATEGORY_ROW_HEIGHT_DP = 48
    const val CHANNEL_ROW_HEIGHT_DP = 48

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_ALAA_NEON_IPTV

    fun usesQuietRows(theme: String): Boolean = isEnabled(theme) ||
        com.alaa.iptv.ui.theme.AsinatLayoutPolicy.isEnabled(theme) ||
        com.alaa.iptv.ui.theme.Asinat2LayoutPolicy.isEnabled(theme) ||
        com.alaa.iptv.ui.theme.AyaLayoutPolicy.isEnabled(theme) ||
        com.alaa.iptv.ui.theme.Aya2LayoutPolicy.isEnabled(theme) ||
        com.alaa.iptv.ui.theme.Aya3LayoutPolicy.isEnabled(theme) ||
        com.alaa.iptv.ui.theme.Aya5LayoutPolicy.isEnabled(theme)

    fun categoryMeta(count: Int): String = if (count > 0) "$count" else ""
}
