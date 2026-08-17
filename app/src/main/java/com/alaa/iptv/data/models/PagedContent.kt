package com.alaa.iptv.data.models

/**
 * صفحة محتوى من مصدر IPTV، مع العدد الحقيقي للعناصر في الفئة التي طلبها المستخدم.
 * العدد يأتي من الاستجابة التي تُجلب أصلاً، لذلك لا يفرض طلباً إضافياً ولا تحميل الفئات الأخرى.
 */
data class PagedContent<T>(
    val items: List<T>,
    val totalCount: Int,
    val hasMore: Boolean
) {
    init {
        require(totalCount >= items.size) { "إجمالي العناصر لا يمكن أن يكون أقل من العناصر المعروضة" }
    }
}
