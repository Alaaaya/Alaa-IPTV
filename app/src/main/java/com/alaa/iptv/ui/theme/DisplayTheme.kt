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
 * يبقى مظهر Alaa Classic هو الافتراضي ما لم يختر المستخدم Hot Player من الإعدادات.
 */
object DisplayTheme {
    private const val HOT_BACKGROUND = "#0A1426"
    private const val HOT_SIDEBAR = "#101D31"
    private const val HOT_PANEL = "#1B2A40"
    private const val HOT_ACCENT = "#D8CA28"
    private const val HOT_BLUE = "#2497DE"

    fun applyDashboard(binding: ActivityDashboardBinding, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        binding.root.setBackgroundColor(Color.parseColor(HOT_BACKGROUND))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(HOT_SIDEBAR))
        binding.heroWatchNow.background = rounded(HOT_ACCENT, 10f)
        binding.heroWatchNow.setTextColor(Color.parseColor(HOT_BACKGROUND))
        binding.categoriesViewAll.setTextColor(Color.parseColor(HOT_ACCENT))
        binding.continueWatchingViewAll.setTextColor(Color.parseColor(HOT_ACCENT))
    }

    fun applyLive(binding: ActivityMainBinding, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        binding.root.setBackgroundColor(Color.parseColor(HOT_BACKGROUND))
        binding.channelPanel.background = rounded(HOT_PANEL, 10f)
        binding.previewPanel.background = rounded(HOT_PANEL, 10f)
        binding.filterAll.background = rounded(HOT_ACCENT, 8f)
        binding.filterAll.setTextColor(Color.parseColor(HOT_BACKGROUND))
        binding.channelCounterFooter.setTextColor(Color.parseColor(HOT_ACCENT))
    }

    fun applyMovies(binding: ActivityMoviesBinding, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        binding.root.setBackgroundColor(Color.parseColor(HOT_BACKGROUND))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(HOT_SIDEBAR))
        binding.movieCategorySelector.background = rounded(HOT_ACCENT, 8f)
        binding.movieCategorySelector.setTextColor(Color.parseColor(HOT_BACKGROUND))
    }

    fun applySeries(binding: ActivitySeriesBinding, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        binding.root.setBackgroundColor(Color.parseColor(HOT_BACKGROUND))
        binding.sidebarContainer.setBackgroundColor(Color.parseColor(HOT_SIDEBAR))
        binding.seriesCategorySelector.background = rounded(HOT_ACCENT, 8f)
        binding.seriesCategorySelector.setTextColor(Color.parseColor(HOT_BACKGROUND))
    }

    fun applyPlayer(binding: ActivityPlayerBinding, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        binding.root.setBackgroundColor(Color.parseColor(HOT_BACKGROUND))
        binding.loadingProgress.indeterminateTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(HOT_ACCENT))
        binding.trackSelectionButton.background = rounded(HOT_PANEL, 12f, HOT_ACCENT, 1)
    }

    fun applyPlayerControls(root: View, prefs: AppPreferences) {
        if (!prefs.isHotPlayerTheme) return
        root.findViewById<SeekBar?>(com.alaa.iptv.R.id.exo_progress)?.let { progress ->
            val accent = android.content.res.ColorStateList.valueOf(Color.parseColor(HOT_ACCENT))
            progress.progressTintList = accent
            progress.thumbTintList = accent
        }
    }

    fun applyPosterCard(binding: ItemMovieCardBinding, hotPlayerTheme: Boolean) {
        if (!hotPlayerTheme) return
        binding.posterCard.radius = 5f
        binding.posterCard.setCardBackgroundColor(Color.parseColor(HOT_PANEL))
        binding.ratingBadge.background = rounded(HOT_ACCENT, 6f)
        binding.ratingBadge.setTextColor(Color.parseColor(HOT_BACKGROUND))
        binding.yearText.background = rounded("#172941", 5f)
        binding.yearText.setPadding(12, 4, 12, 4)
    }

    fun hotFocusBackground(): GradientDrawable = rounded(HOT_ACCENT, 8f)

    fun hotAccentColor(): Int = Color.parseColor(HOT_ACCENT)

    fun hotBlueColor(): Int = Color.parseColor(HOT_BLUE)

    fun hotPanelBackground(): GradientDrawable = rounded(HOT_PANEL, 7f)

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
