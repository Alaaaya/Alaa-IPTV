package com.alaa.iptv.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ActivitySettingsBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        binding.tvIdValue.text = prefs.getOrCreateTvId()
        when (prefs.displayTheme) {
            AppPreferences.THEME_HOT_PLAYER -> binding.themeHotPlayer.isChecked = true
            AppPreferences.THEME_IBO_CLASSIC -> binding.themeIboClassic.isChecked = true
            AppPreferences.THEME_MODERN_GRID -> binding.themeModernGrid.isChecked = true
            AppPreferences.THEME_TV_MINIMAL -> binding.themeTvMinimal.isChecked = true
            AppPreferences.THEME_GLASS_UI -> binding.themeGlassUi.isChecked = true
            AppPreferences.THEME_CLASSIC_BLACK_TV -> binding.themeClassicBlackTv.isChecked = true
            else -> binding.themeAlaaClassic.isChecked = true
        }

        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                R.id.themeHotPlayer -> AppPreferences.THEME_HOT_PLAYER
                R.id.themeIboClassic -> AppPreferences.THEME_IBO_CLASSIC
                R.id.themeModernGrid -> AppPreferences.THEME_MODERN_GRID
                R.id.themeTvMinimal -> AppPreferences.THEME_TV_MINIMAL
                R.id.themeGlassUi -> AppPreferences.THEME_GLASS_UI
                R.id.themeClassicBlackTv -> AppPreferences.THEME_CLASSIC_BLACK_TV
                else -> AppPreferences.THEME_ALAA_CLASSIC
            }
            if (prefs.displayTheme != newTheme) {
                prefs.displayTheme = newTheme
                binding.selectionState.text = when (newTheme) {
                    AppPreferences.THEME_HOT_PLAYER -> "تم اختيار مظهر Hot Player. اضغط تطبيق التصميم."
                    AppPreferences.THEME_IBO_CLASSIC -> "تم اختيار مظهر iBO Classic. اضغط تطبيق التصميم."
                    AppPreferences.THEME_MODERN_GRID -> "تم اختيار مظهر Modern Grid. اضغط تطبيق التصميم."
                    AppPreferences.THEME_TV_MINIMAL -> "تم اختيار مظهر TV Minimal. اضغط تطبيق التصميم."
                    AppPreferences.THEME_GLASS_UI -> "تم اختيار مظهر Glass UI الزجاجي. اضغط تطبيق التصميم."
                    AppPreferences.THEME_CLASSIC_BLACK_TV -> "تم اختيار مظهر Classic Black TV. اضغط تطبيق التصميم."
                    else -> "تم اختيار التصميم الأصلي لتطبيق Alaa IPTV. اضغط تطبيق التصميم."
                }
            }
        }

        binding.applyThemeButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            Toast.makeText(this, "تم تطبيق التصميم. لا تتغير بياناتك أو اشتراكك.", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.backButton.setOnClickListener { finish() }
    }
}
