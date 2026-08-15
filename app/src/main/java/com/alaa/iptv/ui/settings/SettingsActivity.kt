package com.alaa.iptv.ui.settings

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import android.os.StatFs
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.databinding.ActivitySettingsBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity
import com.alaa.iptv.ui.theme.ThemeCatalog
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import com.alaa.iptv.utils.ConnectionDiagnostics
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

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
        updateSyncState()
        buildThemeOptions()
        buildOptionalFeatureSettings()
        addPermissionsInfoAction()
        addAdvancedActions()
        updateDiagnosticsVisibility()
        runAutoCacheCleanIfNeeded()
        showStorageWarningIfNeeded()
        if (prefs.isFeatureEnabled(FeatureCatalog.SETTINGS_LOCK) && prefs.getActiveProfile().pinHash.isNotBlank()) {
            requestSettingsUnlock()
        }

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
        binding.manualSyncButton.setOnClickListener {
            lifecycleScope.launch {
                binding.manualSyncButton.isEnabled = false
                val allowed = ControlPlaneActivityGuard.refreshAndEnforce(this@SettingsActivity, prefs, force = true)
                if (allowed) {
                    updateSyncState()
                    Toast.makeText(this@SettingsActivity, "تمت مزامنة حالة الجهاز والإعدادات.", Toast.LENGTH_SHORT).show()
                }
                binding.manualSyncButton.isEnabled = true
            }
        }
        binding.connectionTestButton.setOnClickListener {
            lifecycleScope.launch {
                binding.connectionTestButton.isEnabled = false
                binding.connectionTestState.text = "جاري فحص الاتصال…"
                val result = ConnectionDiagnostics.inspect(this@SettingsActivity, prefs.serverUrl)
                binding.connectionTestState.text = result.summary
                if (!result.serverReachable && prefs.isFeatureEnabled(FeatureCatalog.SAFE_ERROR_LOG)) {
                    prefs.addSafeDiagnostic("connection-diagnostics")
                }
                binding.connectionTestButton.isEnabled = true
            }
        }
        binding.clearImageCacheButton.setOnClickListener {
            lifecycleScope.launch {
                binding.clearImageCacheButton.isEnabled = false
                withContext(Dispatchers.IO) { Glide.get(applicationContext).clearDiskCache() }
                Glide.get(applicationContext).clearMemory()
                Toast.makeText(this@SettingsActivity, "تم مسح صور البوسترات المؤقتة.", Toast.LENGTH_SHORT).show()
                binding.clearImageCacheButton.isEnabled = true
            }
        }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun updateSyncState() {
        val at = prefs.lastControlPlaneSyncAt()
        binding.syncStateValue.text = if (!prefs.isControlPlaneEnrolled) {
            "غير مربوط بلوحة الإدارة"
        } else if (at <= 0L) {
            "لم تتم أي مزامنة بعد"
        } else {
            "آخر مزامنة: ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(at))}"
        }
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
                        if (feature.id == FeatureCatalog.HIDE_DIAGNOSTICS) updateDiagnosticsVisibility()
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

    private fun requestSettingsUnlock() {
        val input = EditText(this).apply {
            hint = "رمز PIN للمالك"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        AlertDialog.Builder(this)
            .setTitle("الإعدادات مقفلة")
            .setMessage("أدخل رمز PIN للملف الحالي لفتح إعدادات Alaa Player.")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("فتح") { _, _ ->
                if (!prefs.switchProfile(prefs.activeProfileId, input.text.toString())) {
                    Toast.makeText(this, "رمز PIN غير صحيح", Toast.LENGTH_SHORT).show()
                    requestSettingsUnlock()
                }
            }
            .setNegativeButton("خروج") { _, _ -> finish() }
            .show()
    }

    private fun addPermissionsInfoAction() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.PERMISSIONS_INFO)) return
        binding.featureSettingsContainer.addView(android.widget.Button(this).apply {
            text = "لماذا يطلب التطبيق هذه الأذونات؟"
            isAllCaps = false
            setOnClickListener {
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("الأذونات والخصوصية")
                    .setMessage("الاتصال بالإنترنت ضروري لجلب قوائم IPTV وتشغيل البث. لا يطلب Alaa Player صلاحيات الموقع أو جهات الاتصال أو الرسائل. تستخدم مساحة التخزين المؤقتة فقط لحفظ صور البوسترات والبيانات المؤقتة ويمكن مسحها من هذه الصفحة.")
                    .setPositiveButton("حسناً", null)
                    .show()
            }
        })
    }

    private fun updateDiagnosticsVisibility() {
        val visibility = if (prefs.isFeatureEnabled(FeatureCatalog.HIDE_DIAGNOSTICS)) View.GONE else View.VISIBLE
        binding.tvIdValue.visibility = visibility
        binding.syncStateValue.visibility = visibility
        binding.manualSyncButton.visibility = visibility
        binding.connectionTestState.visibility = visibility
        binding.connectionTestButton.visibility = visibility
    }

    private fun addAdvancedActions() {
        fun action(title: String, onClick: () -> Unit) {
            binding.featureSettingsContainer.addView(android.widget.Button(this).apply {
                text = title
                isAllCaps = false
                setOnClickListener { onClick() }
            })
        }
        if (prefs.isFeatureEnabled(FeatureCatalog.REMOTE_TEST)) action("بدء اختبار الريموت") { showRemoteTest() }
        if (prefs.isFeatureEnabled(FeatureCatalog.REMOTE_GUIDE)) action("عرض دليل اختصارات الريموت") { showRemoteGuide() }
        if (prefs.isFeatureEnabled(FeatureCatalog.RESET_PREFERENCES)) action("استعادة إعدادات العرض الافتراضية") {
            AlertDialog.Builder(this)
                .setTitle("استعادة الإعدادات")
                .setMessage("سيعاد التصميم والخيارات المحلية فقط. لن تُحذف بيانات الاشتراك أو TV ID أو المفضلة.")
                .setPositiveButton("استعادة") { _, _ ->
                    prefs.resetCustomization()
                    Toast.makeText(this, "تمت استعادة إعدادات العرض والخيارات المحلية", Toast.LENGTH_SHORT).show()
                    recreate()
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }
        if (prefs.isFeatureEnabled(FeatureCatalog.SAFE_SUPPORT_REPORT)) action("نسخ تقرير دعم آمن") {
            val report = prefs.buildSafeSupportReport(packageManager.getPackageInfo(packageName, 0).versionName ?: "غير معروف")
            (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Alaa Player Support", report))
            Toast.makeText(this, "تم نسخ تقرير الدعم الآمن دون بيانات الاشتراك", Toast.LENGTH_SHORT).show()
        }
        if (prefs.isFeatureEnabled(FeatureCatalog.WHATS_NEW)) action("ما الجديد في Alaa Player") {
            AlertDialog.Builder(this)
                .setTitle("ما الجديد")
                .setMessage("يتضمن هذا الإصدار بدءاً سريعاً، بحثاً موحداً، تفاصيل محتوى أوضح، أدوات اتصال ودعم آمنة، وخيارات خصوصية وأداء اختيارية. لا يحتوي التطبيق على EPG.")
                .setPositiveButton("حسناً", null)
                .show()
        }
    }

    private fun runAutoCacheCleanIfNeeded() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.AUTO_CACHE_CLEAN) || !prefs.shouldAutoCleanImageCache()) return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { Glide.get(applicationContext).clearDiskCache() }
            Glide.get(applicationContext).clearMemory()
            prefs.markImageCacheCleaned()
            Toast.makeText(this@SettingsActivity, "تم تنظيف صور البوسترات القديمة تلقائياً", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStorageWarningIfNeeded() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.STORAGE_WARNING)) return
        val freeBytes = StatFs(filesDir.absolutePath).availableBytes
        if (freeBytes < 500L * 1024L * 1024L) {
            binding.selectionState.text = "مساحة التخزين منخفضة. استخدم «مسح الصور المؤقتة» لتفريغ مساحة."
        }
    }

    private fun showRemoteTest() {
        val result = android.widget.TextView(this).apply {
            text = "اضغط أزرار الريموت هنا؛ سيظهر آخر زر مستلم."
            setTextColor(Color.WHITE)
            textSize = 16f
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, code, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN) resultText(code, this)
                true
            }
        }
        AlertDialog.Builder(this)
            .setTitle("اختبار الريموت")
            .setView(result)
            .setPositiveButton("إنهاء", null)
            .show()
    }

    private fun resultText(code: Int, view: android.widget.TextView) {
        view.text = "تم رصد: ${android.view.KeyEvent.keyCodeToString(code)}"
    }

    private fun showRemoteGuide() {
        AlertDialog.Builder(this)
            .setTitle("دليل اختصارات الريموت")
            .setMessage("الأسهم: تنقل بين الفئات والمحتوى.\nOK: فتح أو تشغيل.\nضغطة مطولة على قناة/محتوى: تفاصيل وخيارات إضافية.\nMENU في المكتبة: فلاتر النوع عند تفعيلها.\nضغطة مطولة على زر المسارات داخل المشغل: مؤقت النوم عند تفعيله.")
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
