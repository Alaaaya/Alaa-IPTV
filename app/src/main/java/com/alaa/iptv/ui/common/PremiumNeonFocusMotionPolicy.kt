package com.alaa.iptv.ui.common

import kotlin.math.roundToLong

/** القيم الموحدة لحركة تركيز التلفزيون، مستقلة عن كل شاشة أو مكوّن. */
object PremiumNeonFocusMotionPolicy {
    const val PULSE_DURATION_MS = 2_000L
    const val FOCUS_IN_DURATION_MS = 150L
    const val FOCUS_OUT_DURATION_MS = 120L
    const val FOCUSED_SCALE = 1.02f

    fun scaledDuration(baseDurationMs: Long, animatorScale: Float): Long {
        if (animatorScale <= 0f) return 0L
        return (baseDurationMs * animatorScale).roundToLong().coerceIn(1L, 2_200L)
    }
}
