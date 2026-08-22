package com.alaa.iptv.ui.common

import com.alaa.iptv.data.models.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryDisplayPolicyTest {
    @Test
    fun `shows the original category name and keeps an unknown count compact`() {
        val category = Category(categoryId = "42", categoryName = "  رياضة عربية  ", channelCount = 0)

        assertEquals("رياضة عربية", CategoryDisplayPolicy.name(category))
        assertEquals("—", CategoryDisplayPolicy.countLabel(category.channelCount))
    }

    @Test
    fun `uses a compact fallback only when the provider returns no category name`() {
        val category = Category(categoryId = "42", categoryName = "", channelCount = 15)

        assertEquals("فئة 42", CategoryDisplayPolicy.name(category))
        assertEquals("15 عنصر", CategoryDisplayPolicy.countLabel(category.channelCount))
    }
}
