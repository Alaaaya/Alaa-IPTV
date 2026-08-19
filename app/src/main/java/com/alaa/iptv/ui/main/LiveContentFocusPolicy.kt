package com.alaa.iptv.ui.main

/**
 * يحدد متى يجوز لتحديث قائمة القنوات طلب التركيز. يمنع تحديث الكاش الخلفي
 * من إعادة المستخدم قسراً من الفئات إلى أول قناة.
 */
object LiveContentFocusPolicy {
    fun requestFocusAfterRefresh(renderedCachedContent: Boolean, contentListHasFocus: Boolean): Boolean =
        !renderedCachedContent || contentListHasFocus

    fun requestFocusAfterAppend(contentListHasFocus: Boolean): Boolean = contentListHasFocus
}
