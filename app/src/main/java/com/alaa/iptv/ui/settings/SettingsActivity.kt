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
        binding.themeAlaaClassic.isChecked = !prefs.isHotPlayerTheme
        binding.themeHotPlayer.isChecked = prefs.isHotPlayerTheme

        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                R.id.themeHotPlayer -> AppPreferences.THEME_HOT_PLAYER
                else -> AppPreferences.THEME_ALAA_CLASSIC
            }
            if (prefs.displayTheme != newTheme) {
                prefs.displayTheme = newTheme
                binding.selectionState.text = if (newTheme == AppPreferences.THEME_HOT_PLAYER) {
                    "تم اختيار مظهر Hot Player. اضغط تطبيق التصميم."
                } else {
                    "تم اختيار التصميم الأصلي لتطبيق Alaa IPTV. اضغط تطبيق التصميم."
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
