package com.alaa.iptv.ui.theme

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.databinding.ActivityMoviesBinding
import com.alaa.iptv.databinding.ActivityPlayerBinding
import com.alaa.iptv.databinding.ActivitySeriesBinding
import com.alaa.iptv.databinding.ActivitySeriesDetailsBinding
import com.alaa.iptv.databinding.ItemMovieCardBinding

/**
 * نظام الهوية البصرية. كل تصميم جديد يحدد خريطة شاشة مستقلة للفئات والقنوات
 * والبطاقات، بينما تبقى بيانات IPTV وتشغيل القنوات والتنقل بالريموت مشتركة وآمنة.
 */
object DisplayTheme {
    enum class LiveCategoryPlacement { SIDE_LIST, SIDE_GRID, TOP_RAIL }

    data class CategoryGridStyle(val spanCount: Int, val orientation: Int)

    data class CategoryCardStyle(
        val focusScale: Float,
        val iconScale: Float,
        val focusedElevation: Float,
        val glowMultiplier: Float,
        val backdropAlpha: Float,
        val monochrome: Boolean = false
    )

    data class DashboardCardSpec(
        val widthDp: Int,
        val heightDp: Int,
        val titleSizeSp: Float,
        val radiusDp: Float,
        val showCount: Boolean,
        val iconScale: Float
    )

    data class ChannelRowSpec(
        val heightDp: Int,
        val horizontalPaddingDp: Int,
        val nameSizeSp: Float,
        val numberSizeSp: Float,
        val showNumber: Boolean,
        val showQuality: Boolean,
        val logoWidthDp: Int,
        val logoHeightDp: Int
    )

    data class LiveCategorySpec(
        val placement: LiveCategoryPlacement,
        val sideWidthPercent: Float,
        val spanCount: Int,
        val itemHeightDp: Int,
        val textSizeSp: Float,
        val labelPrefix: String,
        val showSectionTitle: Boolean
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

    /** تفضيلات عرض محلية خفيفة لا تحتاج إعادة تحميل أو طلبات شبكة. */
    fun applyViewingPreferences(root: View, prefs: AppPreferences) {
        if (!prefs.isFeatureEnabled(FeatureCatalog.LARGE_TEXT)) return
        scaleTextRecursively(root, 1.12f)
    }

    private fun scaleTextRecursively(view: View, multiplier: Float) {
        if (view is TextView) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, view.textSize * multiplier)
        }
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) scaleTextRecursively(view.getChildAt(index), multiplier)
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
        binding.liveCategoriesTitle.setTextColor(Color.parseColor(palette.accent))
        binding.liveCategoryPanel.background = panelBackground(prefs.displayTheme)
        applyLiveStructure(binding, liveCategorySpec(prefs.displayTheme))
        binding.previewImage.alpha = when (prefs.displayTheme) {
            AppPreferences.THEME_CINEMA_SPOTLIGHT -> 0.95f
            AppPreferences.THEME_MONO_STUDIO -> 0.72f
            else -> 1f
        }
    }

    /**
     * يعيد توصيل عناصر لوحة البث. النمط العلوي يجعل الفئات مساراً أفقياً مستقلاً،
     * أما نمطا الجانب فيحافظان على وصول D-Pad الطبيعي بين الفئات والقنوات.
     */
    private fun applyLiveStructure(binding: ActivityMainBinding, spec: LiveCategorySpec) {
        val density = binding.root.resources.displayMetrics.density
        val constraintSet = ConstraintSet().apply { clone(binding.channelPanel) }
        if (spec.placement == LiveCategoryPlacement.TOP_RAIL) {
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.START)
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.END)
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (10 * density).toInt())
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (10 * density).toInt())
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (10 * density).toInt())
            constraintSet.constrainWidth(R.id.liveCategoryPanel, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainHeight(R.id.liveCategoryPanel, (116 * density).toInt())

            constraintSet.clear(R.id.filterTabs, ConstraintSet.START)
            constraintSet.clear(R.id.filterTabs, ConstraintSet.END)
            constraintSet.connect(R.id.filterTabs, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (12 * density).toInt())
            constraintSet.connect(R.id.filterTabs, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (12 * density).toInt())
            constraintSet.connect(R.id.filterTabs, ConstraintSet.TOP, R.id.liveCategoryPanel, ConstraintSet.BOTTOM, (4 * density).toInt())

            constraintSet.clear(R.id.channelsRecyclerView, ConstraintSet.START)
            constraintSet.clear(R.id.channelsRecyclerView, ConstraintSet.END)
            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (8 * density).toInt())
            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (8 * density).toInt())
            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.TOP, R.id.filterTabs, ConstraintSet.BOTTOM, 0)

            constraintSet.clear(R.id.channelCounterFooter, ConstraintSet.START)
            constraintSet.clear(R.id.channelCounterFooter, ConstraintSet.END)
            constraintSet.connect(R.id.channelCounterFooter, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
            constraintSet.connect(R.id.channelCounterFooter, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            binding.liveCategoriesTitle.visibility = View.GONE
        } else {
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.END)
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.TOP)
            constraintSet.clear(R.id.liveCategoryPanel, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (10 * density).toInt())
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (10 * density).toInt())
            constraintSet.connect(R.id.liveCategoryPanel, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (10 * density).toInt())
            constraintSet.constrainWidth(R.id.liveCategoryPanel, ConstraintSet.MATCH_CONSTRAINT)
            constraintSet.constrainPercentWidth(R.id.liveCategoryPanel, spec.sideWidthPercent)
            constraintSet.constrainHeight(R.id.liveCategoryPanel, ConstraintSet.MATCH_CONSTRAINT)

            constraintSet.connect(R.id.filterTabs, ConstraintSet.START, R.id.liveCategoryPanel, ConstraintSet.END, 0)
            constraintSet.connect(R.id.filterTabs, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            constraintSet.connect(R.id.filterTabs, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)

            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.START, R.id.liveCategoryPanel, ConstraintSet.END, 0)
            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            constraintSet.connect(R.id.channelsRecyclerView, ConstraintSet.TOP, R.id.filterTabs, ConstraintSet.BOTTOM, 0)

            constraintSet.connect(R.id.channelCounterFooter, ConstraintSet.START, R.id.liveCategoryPanel, ConstraintSet.END, 0)
            constraintSet.connect(R.id.channelCounterFooter, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
            binding.liveCategoriesTitle.visibility = if (spec.showSectionTitle) View.VISIBLE else View.GONE
        }
        constraintSet.applyTo(binding.channelPanel)
    }

    fun applyMovies(binding: ActivityMoviesBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(palette.sidebar))
        binding.movieCategoryPanel.background = rounded(palette.sidebar, palette.radius, palette.panelStroke, 1)
        binding.movieCategoriesTitle.setTextColor(Color.parseColor(palette.accent))
    }

    fun applySeries(binding: ActivitySeriesBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(palette.sidebar))
        binding.seriesCategoryPanel.background = rounded(palette.sidebar, palette.radius, palette.panelStroke, 1)
        binding.seriesCategoriesTitle.setTextColor(Color.parseColor(palette.accent))
    }

    fun applySeriesDetails(binding: ActivitySeriesDetailsBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.backButton.background = rounded(palette.sidebar, palette.radius, palette.panelStroke, 1)
        binding.backButton.setTextColor(Color.parseColor(palette.accent))
        binding.seriesMeta.setTextColor(Color.parseColor(palette.metadata))
        binding.seriesDescription.setTextColor(Color.parseColor(palette.metadata))
        binding.episodesTitle.setTextColor(Color.parseColor(palette.accent))
    }

    fun applyPlayer(binding: ActivityPlayerBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.loadingProgress.indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(palette.accent))
        binding.trackSelectionButton.background = rounded(palette.panel, palette.radius, palette.focusStroke ?: palette.accent, 1)
    }

    fun applyPlayerControls(root: View, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        root.findViewById<SeekBar?>(R.id.exo_progress)?.let { progress ->
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

    /** أبعاد ومعلومات البطاقة متغيرة، لذلك لا تشترك التصاميم في مظهر فئات واحد. */
    fun dashboardCardSpec(theme: String): DashboardCardSpec = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> DashboardCardSpec(214, 108, 13f, 22f, false, 1.18f)
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> DashboardCardSpec(292, 152, 17f, 28f, false, 0.92f)
        AppPreferences.THEME_SAPPHIRE_HORIZON -> DashboardCardSpec(178, 150, 14f, 14f, true, 1.08f)
        AppPreferences.THEME_EMERALD_PULSE -> DashboardCardSpec(164, 142, 15f, 16f, true, 1.12f)
        AppPreferences.THEME_AMBER_CONSOLE -> DashboardCardSpec(126, 96, 12f, 2f, true, 0.86f)
        AppPreferences.THEME_NORDIC_LIGHT -> DashboardCardSpec(206, 120, 14f, 18f, true, 0.98f)
        AppPreferences.THEME_SUNSET_LOUNGE -> DashboardCardSpec(234, 146, 16f, 30f, false, 1.15f)
        AppPreferences.THEME_MONO_STUDIO -> DashboardCardSpec(118, 118, 12f, 0f, false, 0.82f)
        AppPreferences.THEME_OCEAN_WAVE -> DashboardCardSpec(214, 128, 14f, 24f, true, 1.10f)
        AppPreferences.THEME_ROYAL_VELVET -> DashboardCardSpec(170, 170, 15f, 26f, true, 1.18f)
        else -> DashboardCardSpec(184, 128, 14f, 12f, true, 1f)
    }

    /** صف القناة نفسه يتبدل من مسار عريض إلى بطاقات كثيفة أو صف كونسول مدمج. */
    fun channelRowSpec(theme: String): ChannelRowSpec = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> ChannelRowSpec(72, 14, 16f, 13f, true, true, 54, 34)
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> ChannelRowSpec(84, 18, 18f, 13f, false, false, 70, 42)
        AppPreferences.THEME_SAPPHIRE_HORIZON -> ChannelRowSpec(60, 12, 15f, 12f, true, true, 48, 30)
        AppPreferences.THEME_EMERALD_PULSE -> ChannelRowSpec(66, 14, 16f, 12f, true, true, 52, 32)
        AppPreferences.THEME_AMBER_CONSOLE -> ChannelRowSpec(42, 8, 13f, 11f, true, true, 34, 22)
        AppPreferences.THEME_NORDIC_LIGHT -> ChannelRowSpec(58, 16, 15f, 12f, false, true, 46, 30)
        AppPreferences.THEME_SUNSET_LOUNGE -> ChannelRowSpec(74, 16, 17f, 13f, true, false, 60, 38)
        AppPreferences.THEME_MONO_STUDIO -> ChannelRowSpec(44, 8, 13f, 11f, true, false, 34, 22)
        AppPreferences.THEME_OCEAN_WAVE -> ChannelRowSpec(68, 16, 16f, 12f, false, true, 56, 34)
        AppPreferences.THEME_ROYAL_VELVET -> ChannelRowSpec(78, 18, 17f, 13f, true, true, 62, 38)
        else -> ChannelRowSpec(45, 10, 14f, 12f, true, true, 40, 24)
    }

    /** الفئات إما قائمة جانبية أو مصفوفة جانبية أو مسار أعلى مستقل. */
    fun liveCategorySpec(theme: String): LiveCategorySpec = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> LiveCategorySpec(LiveCategoryPlacement.TOP_RAIL, 0.34f, 1, 72, 15f, "◆ ", false)
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> LiveCategorySpec(LiveCategoryPlacement.SIDE_LIST, 0.25f, 1, 72, 17f, "", true)
        AppPreferences.THEME_SAPPHIRE_HORIZON -> LiveCategorySpec(LiveCategoryPlacement.SIDE_GRID, 0.38f, 2, 70, 14f, "", true)
        AppPreferences.THEME_EMERALD_PULSE -> LiveCategorySpec(LiveCategoryPlacement.SIDE_GRID, 0.36f, 2, 62, 13f, "● ", false)
        AppPreferences.THEME_AMBER_CONSOLE -> LiveCategorySpec(LiveCategoryPlacement.TOP_RAIL, 0.22f, 1, 58, 12f, "[ ", false)
        AppPreferences.THEME_NORDIC_LIGHT -> LiveCategorySpec(LiveCategoryPlacement.SIDE_GRID, 0.32f, 2, 66, 14f, "", true)
        AppPreferences.THEME_SUNSET_LOUNGE -> LiveCategorySpec(LiveCategoryPlacement.TOP_RAIL, 0.38f, 1, 74, 16f, "✦ ", false)
        AppPreferences.THEME_MONO_STUDIO -> LiveCategorySpec(LiveCategoryPlacement.SIDE_LIST, 0.20f, 1, 48, 12f, "# ", true)
        AppPreferences.THEME_OCEAN_WAVE -> LiveCategorySpec(LiveCategoryPlacement.SIDE_GRID, 0.40f, 2, 70, 14f, "≈ ", false)
        AppPreferences.THEME_ROYAL_VELVET -> LiveCategorySpec(LiveCategoryPlacement.SIDE_LIST, 0.40f, 1, 78, 16f, "✦ ", true)
        else -> LiveCategorySpec(LiveCategoryPlacement.SIDE_LIST, 0.34f, 1, 52, 14f, "", true)
    }

    /** يغير كثافة مكتبة الأفلام والمسلسلات لكل هوية بصرية. */
    fun mediaGridSpan(theme: String, useRoomyPosters: Boolean): Int {
        val base = mediaGridSpan(theme)
        return if (useRoomyPosters) (base - 1).coerceAtLeast(2) else base
    }

    fun mediaGridSpan(theme: String): Int = when (theme) {
        AppPreferences.THEME_NEON_ARCADE -> 6
        AppPreferences.THEME_CINEMA_SPOTLIGHT -> 3
        AppPreferences.THEME_SAPPHIRE_HORIZON -> 5
        AppPreferences.THEME_EMERALD_PULSE -> 4
        AppPreferences.THEME_AMBER_CONSOLE -> 7
        AppPreferences.THEME_NORDIC_LIGHT -> 5
        AppPreferences.THEME_SUNSET_LOUNGE -> 4
        AppPreferences.THEME_MONO_STUDIO -> 7
        AppPreferences.THEME_OCEAN_WAVE -> 5
        AppPreferences.THEME_ROYAL_VELVET -> 3
        else -> 5
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
            if (stroke != null && strokeWidthDp > 0) setStroke(strokeWidthDp * 3, Color.parseColor(stroke))
        }
    }
}
