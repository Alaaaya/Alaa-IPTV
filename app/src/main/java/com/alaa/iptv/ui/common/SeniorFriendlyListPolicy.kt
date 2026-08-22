package com.alaa.iptv.ui.common

/** حدود وصول ثابتة تجعل الرقم والاسم مقروءين من مسافة مناسبة على شاشة التلفزيون. */
object SeniorFriendlyListPolicy {
    fun categoryRowHeightDp(requested: Int): Int = requested.coerceIn(58, 64)

    fun categoryNameSizeSp(requested: Float): Float = requested.coerceIn(16f, 18f)

    fun channelRowHeightDp(requested: Int): Int = requested.coerceAtLeast(58)

    fun channelNameSizeSp(requested: Float): Float = requested.coerceAtLeast(15f)

    fun rowNumberSizeSp(requested: Float): Float = requested.coerceAtLeast(17f)
}
