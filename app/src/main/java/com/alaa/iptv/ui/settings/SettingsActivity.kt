package com.alaa.iptv.ui.settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.databinding.ActivitySettingsBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity
import com.alaa.iptv.ui.theme.ThemeCatalog

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: AppPreferences
    private val themeButtons = mutableMapOf<String, RadioButton>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        binding.tvIdValue.text = prefs.getOrCreateTvId()
        buildThemeOptions()
        buildOptionalFeatureSettings()

        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = themeButtons.entries.firstOrNull { it.value.id == checkedId }?.key
                ?: AppPreferences.THEME_ALAA_CLASSIC
            if (prefs.displayTheme != newTheme) {
                prefs.displayTheme = newTheme
                binding.selectionState.text = "تم اختيار ${ThemeCatalog.option(newTheme).title}. اضغط تطبيق التصميم."
            }
        }

        binding.applyThemeButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            Toast.makeText(this, "تم تطبيق التصميم. لا تتغير بياناتك أو اشتراكك.", Toast.LENGTH_SHORT).show()
            finish()
        }
        binding.manageAccountsButton.setOnClickListener {
            startActivity(Intent(this, AccountManagementActivity::class.java))
        }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun buildThemeOptions() {
        binding.themeGroup.removeAllViews()
        themeButtons.clear()
        ThemeCatalog.options.forEach { option ->
            val button = RadioButton(this).apply {
                id = View.generateViewId()
                text = "${option.title} — ${option.description}"
                setTextColor(Color.WHITE)
                textSize = 15f
                isFocusable = true
                setPadding(dp(12), dp(10), dp(12), dp(10))
                setBackgroundResource(R.drawable.bg_login_input)
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            themeButtons[option.id] = button
            binding.themeGroup.addView(button)
        }
        val activeTheme = ThemeCatalog.option(prefs.displayTheme).id
        themeButtons[activeTheme]?.isChecked = true
        binding.selectionState.text = "التصميم الحالي: ${ThemeCatalog.option(activeTheme).title}."
    }

    private fun buildOptionalFeatureSettings() {
        binding.featureSettingsContainer.removeAllViews()
        FeatureCatalog.options.groupBy { it.section }.forEach { (section, options) ->
            val sectionTitle = android.widget.TextView(this).apply {
                text = section
                setTextColor(Color.parseColor("#D8CA28"))
                textSize = 17f
                setPadding(0, dp(14), 0, dp(6))
            }
            binding.featureSettingsContainer.addView(sectionTitle)

            options.forEach { feature ->
                val toggle = Switch(this).apply {
                    text = "${feature.title}\n${feature.description}"
                    setTextColor(Color.WHITE)
                    textSize = 15f
                    isChecked = prefs.isFeatureEnabled(feature.id)
                    isFocusable = true
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    setBackgroundResource(R.drawable.bg_login_input)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                    setOnCheckedChangeListener { _, enabled ->
                        prefs.setFeatureEnabled(feature.id, enabled)
                        Toast.makeText(
                            this@SettingsActivity,
                            "${feature.title}: ${if (enabled) "مفعّلة" else "متوقفة"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                binding.featureSettingsContainer.addView(toggle)
            }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
