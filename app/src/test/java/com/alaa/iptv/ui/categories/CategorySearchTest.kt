package com.alaa.iptv.ui.categories

import com.alaa.iptv.data.models.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorySearchTest {
    private val categories = listOf(
        Category("1", "Sports HD"),
        Category("2", "عربى أفلام"),
        Category("3", "Arabic Series")
    )

    @Test
    fun `blank search returns every provider category`() {
        assertEquals(3, CategorySearch.filter(categories, "").size)
    }

    @Test
    fun `search finds Arabic and Latin category names`() {
        assertEquals(listOf("عربى أفلام"), CategorySearch.filter(categories, "أفلام").map { it.categoryName })
        assertEquals(listOf("Arabic Series"), CategorySearch.filter(categories, "series").map { it.categoryName })
    }
}
