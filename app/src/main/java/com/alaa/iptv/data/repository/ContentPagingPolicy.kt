package com.alaa.iptv.data.repository

/** حدود صفحة محتوى محسوبة من استجابة الفئة نفسها دون جلب فئات أخرى. */
internal object ContentPagingPolicy {
    data class Bounds(
        val totalCount: Int,
        val startIndex: Int,
        val endIndex: Int
    ) {
        val hasMore: Boolean
            get() = endIndex < totalCount
    }

    fun bounds(totalCount: Int, requestedPage: Int, pageSize: Int): Bounds {
        require(totalCount >= 0) { "إجمالي العناصر يجب ألا يكون سالباً" }
        require(pageSize > 0) { "حجم الصفحة يجب أن يكون أكبر من صفر" }
        val startIndex = (requestedPage.coerceAtLeast(0) * pageSize).coerceAtMost(totalCount)
        return Bounds(totalCount, startIndex, minOf(totalCount, startIndex + pageSize))
    }
}
