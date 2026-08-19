package com.alaa.iptv.ui.main

import kotlin.math.roundToLong

/** حركة تركيز قصيرة يمكن إلغاؤها فوراً عند تتابع ضغطات الريموت. */
object ChannelFocusMotionPolicy {
    const val FOCUS_DURATION_MS = 110L
    const val UNFOCUS_DURATION_MS = 85L
    const val FOCUSED_SCALE = 1.018f
    const val FOCUSED_ALPHA = 1.0f
    const val UNFOCUSED_ALPHA = 0.94f

    fun durationMs(hasFocus: Boolean, animatorScale: Float): Long {
        if (animatorScale <= 0f) return 0L
        val base = if (hasFocus) FOCUS_DURATION_MS else UNFOCUS_DURATION_MS
        return (base * animatorScale).roundToLong().coerceIn(1L, 180L)
    }
}
