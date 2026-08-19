package com.alaa.iptv.ui.dashboard

import kotlin.math.ceil

/** يحسب ارتفاع صف فئات الرئيسية من البطاقة والثيم الفعليين فقط، لا من أكبر ثيم آخر. */
object DashboardRailLayoutPolicy {
    fun categoryRailHeightDp(cardHeightDp: Int, focusScale: Float): Int =
        ceil(cardHeightDp * focusScale).toInt() + 24
}
