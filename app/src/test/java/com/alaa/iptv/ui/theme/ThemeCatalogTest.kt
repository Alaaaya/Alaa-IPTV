package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeCatalogTest {
    @Test
    fun `catalog exposes all existing designs plus the Alaa Neon IPTV choice`() {
        assertEquals(25, ThemeCatalog.options.size)
        assertEquals(25, ThemeCatalog.options.map { it.id }.toSet().size)
        assertTrue(ThemeCatalog.options.any { it.id == "alaa_neon_iptv" && it.title == "Alaa Player – Neon IPTV" })
        assertTrue(ThemeCatalog.options.any { it.id == "alaa_figma" && it.title == "Alaa — Figma TV" })
        assertTrue(ThemeCatalog.options.any { it.id == "asinat" && it.title == "Asinat" })
        assertTrue(ThemeCatalog.options.any { it.id == "asinat_2" && it.title == "Asinat 2" })
        assertTrue(ThemeCatalog.options.any { it.id == "aya" && it.title == "Aya" })
        assertTrue(ThemeCatalog.options.any { it.id == "aya_2" && it.title == "أية 2" })
        assertTrue(ThemeCatalog.options.any { it.id == "aya_3" && it.title == "أية 3" })
        assertTrue(ThemeCatalog.options.any { it.id == "aya_5" && it.title == "Aya 5" })
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
    fun `later tv themes expose distinct structural layouts not only palette changes`() {
        val newThemeIds = listOf(
            AppPreferences.THEME_AYA,
            AppPreferences.THEME_AYA_2,
            AppPreferences.THEME_AYA_3,
            AppPreferences.THEME_AYA_5,
            AppPreferences.THEME_NEON_ARCADE,
            AppPreferences.THEME_CINEMA_SPOTLIGHT,
            AppPreferences.THEME_SAPPHIRE_HORIZON,
            AppPreferences.THEME_EMERALD_PULSE,
            AppPreferences.THEME_AMBER_CONSOLE,
            AppPreferences.THEME_NORDIC_LIGHT,
            AppPreferences.THEME_SUNSET_LOUNGE,
            AppPreferences.THEME_MONO_STUDIO,
            AppPreferences.THEME_OCEAN_WAVE,
            AppPreferences.THEME_ROYAL_VELVET
        )
        assertEquals(14, newThemeIds.size)

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

        assertEquals(14, signatures.toSet().size)
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.TOP_RAIL })
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.SIDE_GRID })
        assertTrue(newThemeIds.any { DisplayTheme.liveCategorySpec(it).placement == DisplayTheme.LiveCategoryPlacement.SIDE_LIST })
    }
}
