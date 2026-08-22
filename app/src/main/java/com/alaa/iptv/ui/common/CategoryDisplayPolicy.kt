package com.alaa.iptv.ui.common

import com.alaa.iptv.data.models.Category

/** يحافظ على ظهور اسم الفئة داخل واجهات التلفزيون حتى حين لا يوفّر المصدر عدداً للعناصر. */
object CategoryDisplayPolicy {
    fun name(category: Category): String =
        category.categoryName.trim().ifBlank { "فئة ${category.categoryId}" }

    fun countLabel(count: Int): String =
        if (count > 0) "$count عنصر" else "—"
}
