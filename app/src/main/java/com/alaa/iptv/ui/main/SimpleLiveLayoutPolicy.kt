package com.alaa.iptv.ui.main

import com.alaa.iptv.data.preferences.AppPreferences

/** قواعد التخطيط الهادئ لواجهة البث في أحدث تصميم Alaa. */
object SimpleLiveLayoutPolicy {
    const val CATEGORY_SIDE_WIDTH = 0.32f
    const val CATEGORY_ROW_HEIGHT_DP = 48
    const val CHANNEL_ROW_HEIGHT_DP = 48

    fun isEnabled(theme: String): Boolean = theme == AppPreferences.THEME_ALAA_NEON_IPTV

    fun categoryMeta(count: Int): String = if (count > 0) "$count" else ""
}
