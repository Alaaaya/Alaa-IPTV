package com.alaa.iptv.ui.dashboard

import kotlin.math.ceil

/** مساحة عمودية كافية لتكبير بطاقة المشاهدة اللاحقة دون قصها داخل RecyclerView. */
object ContinueWatchingRailPolicy {
    fun railHeightDp(cardHeightDp: Int, focusScale: Float): Int =
        ceil(cardHeightDp * focusScale).toInt() + 16
}
