package com.alaa.iptv.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeCatalogTest {
    @Test
    fun `catalog exposes all existing and ten new visual choices`() {
        assertEquals(17, ThemeCatalog.options.size)
        assertEquals(17, ThemeCatalog.options.map { it.id }.toSet().size)
        assertTrue(ThemeCatalog.options.any { it.title == "Neon Arcade" })
        assertTrue(ThemeCatalog.options.any { it.title == "Royal Velvet" })
    }

    @Test
    fun `every non default option provides a usable visual profile`() {
        ThemeCatalog.options.drop(1).forEach { option ->
            assertTrue(DisplayTheme.hasCustomTheme(option.id))
            assertTrue(DisplayTheme.dashboardCategoryGrid(option.id).spanCount > 0)
            assertTrue(DisplayTheme.categoryCardStyle(option.id).focusScale >= 1f)
        }
    }
}
