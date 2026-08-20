package com.alaa.iptv.ui.theme

import com.alaa.iptv.data.preferences.AppPreferences

data class ThemeOption(
    val id: String,
    val title: String,
    val description: String
)

object ThemeCatalog {
    val options = listOf(
        ThemeOption(AppPreferences.THEME_ALAA_CLASSIC, "Alaa Player Classic", "التصميم الأصلي الافتراضي"),
        ThemeOption(AppPreferences.THEME_ALAA_NEON_IPTV, "Alaa Player – Neon IPTV", "واجهة IPTV سينمائية زجاجية بتوهج أحمر نيون"),
        ThemeOption(AppPreferences.THEME_ALAA_FIGMA, "Alaa — Figma TV", "طبقات منتصف الليل وبانر أزرق–قرمزي وبطاقات قرمزية مستوحاة من المرجع"),
        ThemeOption(AppPreferences.THEME_MIDNIGHT_GOLD, "Midnight Gold", "كحلي داكن ولمسات ذهبية"),
        ThemeOption(AppPreferences.THEME_CRIMSON_CLASSIC, "Crimson Classic", "قوائم داكنة بتمييز أحمر"),
        ThemeOption(AppPreferences.THEME_MODERN_GRID, "Modern Grid", "شبكة حديثة بلون بنفسجي"),
        ThemeOption(AppPreferences.THEME_TV_MINIMAL, "TV Minimal", "واجهة أسود وأبيض بسيطة"),
        ThemeOption(AppPreferences.THEME_GLASS_UI, "Glass UI", "ألواح زجاجية ولمسات ضوئية"),
        ThemeOption(AppPreferences.THEME_CLASSIC_BLACK_TV, "Classic Black TV", "تلفزيون أسود وحدود زرقاء"),
        ThemeOption(AppPreferences.THEME_NEON_ARCADE, "Neon Arcade", "سايبربانك داكن مع حدود نيون"),
        ThemeOption(AppPreferences.THEME_CINEMA_SPOTLIGHT, "Cinema Spotlight", "صالة سينما داكنة ببطاقات عريضة"),
        ThemeOption(AppPreferences.THEME_SAPPHIRE_HORIZON, "Sapphire Horizon", "أزرق ملكي ولوحات واسعة"),
        ThemeOption(AppPreferences.THEME_EMERALD_PULSE, "Emerald", "أخضر زمردي بحالات تركيز ثابتة"),
        ThemeOption(AppPreferences.THEME_AMBER_CONSOLE, "Amber Console", "واجهة كونسول كهرمانية عالية التباين"),
        ThemeOption(AppPreferences.THEME_NORDIC_LIGHT, "Nordic Light", "ألواح فاتحة بلمسات جليدية"),
        ThemeOption(AppPreferences.THEME_SUNSET_LOUNGE, "Sunset Lounge", "تدرجات غروب أرجوانية وبرتقالية"),
        ThemeOption(AppPreferences.THEME_MONO_STUDIO, "Mono Studio", "أبيض وأسود هندسي مع تركيز حاد"),
        ThemeOption(AppPreferences.THEME_OCEAN_WAVE, "Ocean Wave", "بحري تركوازي بألواح طبقية"),
        ThemeOption(AppPreferences.THEME_ROYAL_VELVET, "Royal Velvet", "بنفسجي ملكي وملمس فاخر")
    )

    fun option(id: String): ThemeOption = options.firstOrNull { it.id == id } ?: options.first()
}
