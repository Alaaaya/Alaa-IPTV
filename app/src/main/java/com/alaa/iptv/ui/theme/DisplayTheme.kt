package com.alaa.iptv.ui.theme

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.SeekBar
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
    private data class Palette(
        val background: String,
        val sidebar: String,
        val panel: String,
        val accent: String,
        val accentText: String,
        val metadata: String,
        val radius: Float
    )

    private fun palette(theme: String): Palette? = when (theme) {
        AppPreferences.THEME_HOT_PLAYER -> Palette("#0A1426", "#101D31", "#1B2A40", "#D8CA28", "#0A1426", "#2497DE", 7f)
        AppPreferences.THEME_IBO_CLASSIC -> Palette("#111319", "#191D25", "#242A35", "#E53935", "#FFFFFF", "#5EB5F7", 10f)
        AppPreferences.THEME_MODERN_GRID -> Palette("#110D22", "#1B1433", "#282047", "#8B5CF6", "#FFFFFF", "#22D3EE", 18f)
        AppPreferences.THEME_TV_MINIMAL -> Palette("#070707", "#101010", "#1A1A1A", "#F2F2F2", "#050505", "#BDBDBD", 4f)
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
    }

    fun applyLive(binding: ActivityMainBinding, prefs: AppPreferences) {
        val palette = palette(prefs.displayTheme) ?: return
        binding.root.setBackgroundColor(Color.parseColor(palette.background))
        binding.channelPanel.background = rounded(palette.panel, palette.radius)
        binding.previewPanel.background = rounded(palette.panel, palette.radius)
        binding.filterAll.background = rounded(palette.accent, palette.radius)
        binding.filterAll.setTextColor(Color.parseColor(palette.accentText))
        binding.channelCounterFooter.setTextColor(Color.parseColor(palette.accent))
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
        binding.trackSelectionButton.background = rounded(palette.panel, palette.radius, palette.accent, 1)
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

    fun focusBackground(theme: String): GradientDrawable = rounded(requireNotNull(palette(theme)).accent, requireNotNull(palette(theme)).radius)

    fun focusTextColor(theme: String): Int = Color.parseColor(requireNotNull(palette(theme)).accentText)

    fun metadataColor(theme: String): Int = Color.parseColor(requireNotNull(palette(theme)).metadata)

    fun panelBackground(theme: String): GradientDrawable = rounded(requireNotNull(palette(theme)).panel, requireNotNull(palette(theme)).radius)

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
