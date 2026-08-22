package com.alaa.iptv.ui.main

/** يحافظ على قائمة الفئات جانبية ومضغوطة لكن قابلة للقراءة على Android TV. */
object LiveCategoryLayoutPolicy {
    fun compactItemHeightDp(requestedHeightDp: Int): Int = requestedHeightDp.coerceIn(58, 64)

    fun compactNameSizeSp(requestedTextSizeSp: Float): Float = requestedTextSizeSp.coerceIn(16f, 18f)
}
