package com.alaa.iptv.ui.theme

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.databinding.ActivityMoviesBinding
import com.alaa.iptv.databinding.ActivityPlayerBinding
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.databinding.ItemMovieCardBinding

/**
 * طبقة ألوان اختيارية فقط. لا تغيّر أي حساب أو TV ID أو بيانات محتوى.
 * يبقى مظهر Alaa Classic هو الافتراضي ما لم يختر المستخدم مظهراً آخر من الإعدادات.
 */
object DisplayTheme {
    data class CategoryGridStyle(val spanCount: Int, val orientation: Int)
    data class CategoryCardStyle(
        val focusScale: Float,
        val iconScale: Float,
        val focusedElevation: Float,
        val glowMultiplier: Float,
        val backdropAlpha: Float,
        val monochrome: Boolean = false
    )
    private data class Palette(
        val background: String,
        val sidebar: String,
        val panel: String,
        val accent: String,
        val accentText: String,
        val metadata: String,
        val radius: Float,
        val focusFill: String = accent,
        val focusStroke: String? = null,
        val panelStroke: String? = null
    )

    private fun palette(theme: String): Palette? = when (theme) {
        AppPreferences.THEME_MIDNIGHT_GOLD -> Palette("#0A1426", "#101D31", "#1B2A40", "#D8CA28", "#0A1426", "#2497DE", 7f)
        AppPreferences.THEME_CRIMSON_CLASSIC -> Palette("#111319", "#191D25", "#242A35", "#E53935", "#FFFFFF", "#5EB5F7", 10f)
        AppPreferences.THEME_MODERN_GRID -> Palette("#110D22", "#1B1433", "#282047", "#8B5CF6", "#FFFFFF", "#22D3EE", 18f)
        AppPreferences.THEME_TV_MINIMAL -> Palette("#070707", "#101010", "#1A1A1A", "#F2F2F2", "#050505", "#BDBDBD", 4f)
        AppPreferences.THEME_GLASS_UI -> Palette("#081321", "#122437", "#368BB8CC", "#8AD7FF", "#06111E", "#C6EDFF", 18f, "#66A7D8EE", "#C0ECFF", "#66D1F2FF")
        AppPreferences.THEME_CLASSIC_BLACK_TV -> Palette("#000000", "#060606", "#0E0E0E", "#2D8CFF", "#FFFFFF", "#84BBFF", 6f, "#050505", "#2D8CFF", "#262626")
        AppPreferences.THEME_NEON_ARCADE -> Palette("#090313", "#18072A", "#24113C", "#FF2ED1", "#16051F", "#45F7FF", 22f, "#4B134D", "#45F7FF", "#A329D5")
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> Palette("#120C0C", "#211313", "#2E1B1A", "#E6A750", "#24140A", "#FFD38F", 28f, "#572C14", "#FFD38F", "#5B3526")
        AppPreferences.THEME_SAPPHIRE_HORIZON -> Palette("#06142B", "#0A2146", "#0D315F", "#43A6FF", "#06182F", "#9ACBFF", 14f, "#114C87", "#A9D7FF", "#286AA6")
        AppPreferences.THEME_EMERALD_PULSE -> Palette("#051B16", "#0A2B22", "#0D4434", "#39E59B", "#062217", "#B5FFD8", 16f, "#126B4A", "#A8FFD0", "#248A62")
        AppPreferences.THEME_AMBER_CONSOLE -> Palette("#160F04", "#261A06", "#38280C", "#FFB61E", "#211604", "#FFE2A2", 2f, "#5A3B04", "#FFE2A2", "#865C0D")
        AppPreferences.THEME_NORDIC_LIGHT -> Palette("#10243B", "#173A5A", "#204D75", "#74B9FF", "#0D2136", "#C7E4FF", 18f, "#315F87", "#D8EEFF", "#5D8FB8")
        AppPreferences.THEME_SUNSET_LOUNGE -> Palette("#1B0B25", "#32113A", "#4A1C42", "#FF805C", "#361123", "#FFCCAB", 30f, "#703352", "#FFCCAB", "#A14D6E")
        AppPreferences.THEME_MONO_STUDIO -> Palette("#111111", "#1D1D1D", "#262626", "#F5F5F5", "#101010", "#CFCFCF", 0f, "#F5F5F5", "#FFFFFF", "#545454")
        AppPreferences.THEME_OCEAN_WAVE -> Palette("#031C26", "#073445", "#0A526A", "#21D4C2", "#03231F", "#AAFFF5", 24f, "#137A73", "#B5FFF8", "#168D93")
        AppPreferences.THEME_ROYAL_VELVET -> Palette("#190722", "#2D0E42", "#4A1B63", "#D49AFF", "#2C0B40", "#F1CDFF", 26f, "#6B2B8C", "#F5D6FF", "#935AC0")
        else -> null
    }

    fun applyDashboard(binding: ActivityDashboardBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(palette.sidebar))
        binding.heroWatchNow.background = rounded(palette.accent, palette.radius)
        binding.heroWatchNow.setTextColor(Color.parseColor(palette.accentText))
        binding.categoriesViewAll.setTextColor(Color.parseColor(palette.accent))
        binding.continueWatchingViewAll.setTextColor(Color.parseColor(palette.accent))
        binding.heroImage.alpha = when (prefs.displayTheme) {
            AppPreferences.THEME_CINEMA_SPOTLIGHT -> 0.92f
            AppPreferences.THEME_MONO_STUDIO -> 0.66f
            AppPreferences.THEME_NEON_ARCADE -> 0.78f
            else -> 1f
        }
        binding.categoriesHeaderTitle.letterSpacing = when (prefs.displayTheme) {
            AppPreferences.THEME_AMBER_CONSOLE, AppPreferences.THEME_MONO_STUDIO -> 0.08f
            else -> 0f
        }
    }

    fun applyLive(binding: ActivityMainBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.channelPanel.background = panelBackground(prefs.displayTheme)
        binding.previewPanel.background = panelBackground(prefs.displayTheme)
        binding.filterAll.background = rounded(palette.accent, palette.radius)
        binding.filterAll.setTextColor(Color.parseColor(palette.accentText))
        binding.channelCounterFooter.setTextColor(Color.parseColor(palette.accent))
        val categoryPercent = when (prefs.displayTheme) {
            AppPreferences.THEME_NEON_ARCADE -> 0.42f
            AppPreferences.THEME_CINEMA_SPOTLIGHT -> 0.25f
            AppPreferences.THEME_AMBER_CONSOLE -> 0.22f
            AppPreferences.THEME_NORDIC_LIGHT -> 0.30f
            AppPreferences.THEME_SUNSET_LOUNGE -> 0.38f
            AppPreferences.THEME_MONO_STUDIO -> 0.20f
            AppPreferences.THEME_OCEAN_WAVE -> 0.36f
            AppPreferences.THEME_ROYAL_VELVET -> 0.40f
            else -> 0.34f
        }
        (binding.liveCategoryPanel.layoutParams as? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
            ?.let { params ->
                params.matchConstraintPercentWidth = categoryPercent
                binding.liveCategoryPanel.layoutParams = params
            }
        binding.previewImage.alpha = when (prefs.displayTheme) {
            AppPreferences.THEME_CINEMA_SPOTLIGHT -> 0.95f
            AppPreferences.THEME_MONO_STUDIO -> 0.72f
            else -> 1f
        }
    }

    fun applyMovies(binding: ActivityMoviesBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(palette.sidebar))
        binding.movieCategorySelector.background = rounded(palette.accent, palette.radius)
        binding.movieCategorySelector.setTextColor(Color.parseColor(palette.accentText))
    }

    fun applySeries(binding: ActivitySeriesBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(palette.sidebar))
        binding.seriesCategorySelector.background = rounded(palette.accent, palette.radius)
        binding.seriesCategorySelector.setTextColor(Color.parseColor(palette.accentText))
    }

    fun applyPlayer(binding: ActivityPlayerBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.loadingProgress.indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(palette.accent))
        binding.trackSelectionButton.background = rounded(palette.panel, palette.radius, palette.focusStroke ?: palette.accent, 1)
    }

    fun applyPlayerControls(root: View, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        root.findViewById<SeekBar?>(com.alaa.iptv.R.id.exo_progress)?.let { progress ->
            val accent = android.content.res.ColorStateList.valueOf(Color.parseColor(palette.accent))
            progress.progressTintList = accent
            progress.thumbTintList = accent
        }
    }

    fun applyPosterCard(binding: ItemMovieCardBinding, theme: String) {
        val palette = palette(theme) ?: return
        binding.posterCard.radius = palette.radius
        binding.posterCard.setCardBackgroundColor(Color.parseColor(palette.panel))
        binding.ratingBadge.background = rounded(palette.accent, palette.radius)
        binding.ratingBadge.setTextColor(Color.parseColor(palette.accentText))
        binding.yearText.background = rounded(palette.sidebar, palette.radius)
        binding.yearText.setPadding(12, 4, 12, 4)
    }

    fun hasCustomTheme(theme: String): Boolean = palette(theme) != null

    fun categoryCardStyle(theme: String): CategoryCardStyle = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> CategoryCardStyle(1.10f, 1.16f, 20f, 1.35f, 0.75f)
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> CategoryCardStyle(1.025f, 1.02f, 14f, 0.55f, 1.0f)
        AppPreferences.THEME_SAPPHIRE_HORIZON -> CategoryCardStyle(1.055f, 1.10f, 16f, 1.10f, 0.86f)
        AppPreferences.THEME_EMERALD_PULSE -> CategoryCardStyle(1.075f, 1.14f, 18f, 1.25f, 0.82f)
        AppPreferences.THEME_AMBER_CONSOLE -> CategoryCardStyle(1.02f, 1.04f, 9f, 0.45f, 0.65f)
        AppPreferences.THEME_NORDIC_LIGHT -> CategoryCardStyle(1.035f, 1.06f, 10f, 0.70f, 0.92f)
        AppPreferences.THEME_SUNSET_LOUNGE -> CategoryCardStyle(1.08f, 1.13f, 19f, 1.30f, 0.88f)
        AppPreferences.THEME_MONO_STUDIO -> CategoryCardStyle(1.015f, 1.0f, 8f, 0.0f, 0.65f, monochrome = true)
        AppPreferences.THEME_OCEAN_WAVE -> CategoryCardStyle(1.065f, 1.12f, 17f, 1.15f, 0.84f)
        AppPreferences.THEME_ROYAL_VELVET -> CategoryCardStyle(1.075f, 1.15f, 18f, 1.22f, 0.90f)
        else -> CategoryCardStyle(1.05f, 1.08f, 12f, 1.0f, 1.0f)
    }

    fun dashboardCategoryGrid(theme: String): CategoryGridStyle = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> CategoryGridStyle(2, RecyclerView.HORIZONTAL)
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> CategoryGridStyle(1, RecyclerView.HORIZONTAL)
        AppPreferences.THEME_SAPPHIRE_HORIZON -> CategoryGridStyle(2, RecyclerView.HORIZONTAL)
        AppPreferences.THEME_EMERALD_PULSE -> CategoryGridStyle(2, RecyclerView.VERTICAL)
        AppPreferences.THEME_AMBER_CONSOLE -> CategoryGridStyle(4, RecyclerView.VERTICAL)
        AppPreferences.THEME_NORDIC_LIGHT -> CategoryGridStyle(3, RecyclerView.VERTICAL)
        AppPreferences.THEME_SUNSET_LOUNGE -> CategoryGridStyle(2, RecyclerView.HORIZONTAL)
        AppPreferences.THEME_MONO_STUDIO -> CategoryGridStyle(4, RecyclerView.VERTICAL)
        AppPreferences.THEME_OCEAN_WAVE -> CategoryGridStyle(2, RecyclerView.HORIZONTAL)
        AppPreferences.THEME_ROYAL_VELVET -> CategoryGridStyle(2, RecyclerView.VERTICAL)
        else -> CategoryGridStyle(1, RecyclerView.HORIZONTAL)
    }

    fun focusBackground(theme: String): GradientDrawable {
        val palette = requireNotNull(palette(theme))
        return rounded(palette.focusFill, palette.radius, palette.focusStroke, if (palette.focusStroke == null) 0 else 2)
    }

    fun focusTextColor(theme: String): Int = Color.parseColor(requireNotNull(palette(theme)).accentText)

    fun metadataColor(theme: String): Int = Color.parseColor(requireNotNull(palette(theme)).metadata)

    fun panelBackground(theme: String): GradientDrawable {
        val palette = requireNotNull(palette(theme))
        return rounded(palette.panel, palette.radius, palette.panelStroke, if (palette.panelStroke == null) 0 else 1)
    }

    private fun rounded(fill: String, radiusDp: Float, stroke: String? = null, strokeWidthDp: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radiusDp * 3f
            setColor(Color.parseColor(fill))
            if (stroke != null && strokeWidthDp > 0) {
                setStroke(strokeWidthDp * 3, Color.parseColor(stroke))
            }
        }
    }
}
