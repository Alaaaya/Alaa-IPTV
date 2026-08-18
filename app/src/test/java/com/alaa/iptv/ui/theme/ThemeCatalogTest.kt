package com.alaa.iptv.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeCatalogTest {
    @Test
    fun `catalog exposes all existing designs plus the Alaa Neon IPTV choice`() {
        assertEquals(18, ThemeCatalog.options.size)
        assertEquals(18, ThemeCatalog.options.map { it.id }.toSet().size)
        assertTrue(ThemeCatalog.options.any { it.id == "alaa_neon_iptv" && it.title == "Alaa Player – Neon IPTV" })
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

    @Test
    fun `alaa neon iptv uses a neon focus profile with a live category sidebar`() {
        val theme = "alaa_neon_iptv"

        assertTrue(DisplayTheme.isNeonIptv(theme))
        assertEquals(DisplayTheme.LiveCategoryPlacement.SIDE_LIST, DisplayTheme.liveCategorySpec(theme).placement)
        assertTrue(DisplayTheme.categoryCardStyle(theme).focusScale > 1f)
        assertTrue(DisplayTheme.channelRowSpec(theme).showNumber)
    }

    @Test
    fun `ten new themes expose distinct structural layouts not only palette changes`() {
        val newThemeIds = ThemeCatalog.options.drop(8).map { it.id }
        assertEquals(10, newThemeIds.size)

        val signatures = newThemeIds.map { id ->
            val live = DisplayTheme.liveCategorySpec(id)
            val channel = DisplayTheme.channelRowSpec(id)
            val card = DisplayTheme.dashboardCardSpec(id)
            listOf(
                live.placement.name,
                live.spanCount.toString(),
                live.itemHeightDp.toString(),
                channel.heightDp.toString(),
                channel.showNumber.toString(),
                card.widthDp.toString(),
                card.heightDp.toString(),
                DisplayTheme.mediaGridSpan(id).toString()
            ).joinToString("|")
        }

        assertEquals(10, signatures.toSet().size)
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.TOP_RAIL })
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.SIDE_GRID })
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.SIDE_LIST })
    }
}
