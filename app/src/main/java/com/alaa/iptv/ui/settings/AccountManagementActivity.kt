package com.alaa.iptv.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.repository.MediaRepository
import kotlinx.coroutines.launch

class AccountManagementActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var content: LinearLayout
    private lateinit var repository: MediaRepository
    private val backupExport = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(prefs.exportEncryptedSettingsBackup()) }
                ?: error("تعذر فتح الملف")
        }.onSuccess { toast("تم إنشاء النسخة الاحتياطية المشفّرة") }
            .onFailure { toast("تعذر حفظ النسخة: ${it.message}") }
    }
    private val backupImport = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        val value = runCatching { contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty() }.getOrDefault("")
        if (prefs.importEncryptedSettingsBackup(value)) {
            toast("تمت استعادة إعدادات الملف الحالي")
            render()
        } else toast("الملف غير صالح أو لا ينتمي لهذا الجهاز")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A1426"))
            setPadding(dp(36), dp(28), dp(36), dp(28))
            layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
        root.addView(TextView(this).apply {
            text = "الملفات والاشتراكات"
            setTextColor(Color.WHITE)
            textSize = 28f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "تظهر أدوات الملف أو الاشتراك فقط عند تفعيل ميزتها من الإعدادات."
            setTextColor(Color.LTGRAY)
            textSize = 14f
            setPadding(0, dp(8), 0, dp(12))
        })
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            addView(content)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        })
        root.addView(Button(this).apply {
            text = "← رجوع"
            isFocusable = true
            setOnClickListener { finish() }
        })
        setContentView(root)
        render()
    }

    private fun render() {
        content.removeAllViews()
        if (prefs.isFeatureEnabled(FeatureCatalog.PROFILES)) renderProfiles()
        if (prefs.isFeatureEnabled(FeatureCatalog.MULTI_SUBSCRIPTIONS)) renderSubscriptions()
        if (prefs.isFeatureEnabled(FeatureCatalog.PARENTAL_PIN)) renderPin()
        if (prefs.isFeatureEnabled(FeatureCatalog.CONNECTION_TEST)) renderConnectionTest()
        if (prefs.isFeatureEnabled(FeatureCatalog.HIDE_CONTENT)) renderHiddenContentControls()
        if (prefs.isFeatureEnabled(FeatureCatalog.HOME_CUSTOMIZATION) || prefs.isFeatureEnabled(FeatureCatalog.CATEGORY_ORDER)) renderHomeCustomization()
        if (prefs.isFeatureEnabled(FeatureCatalog.ENCRYPTED_BACKUP)) renderBackup()
        if (content.childCount == 0) {
            content.addView(TextView(this).apply {
                text = "فعّل «ملفات تعريف»، «اشتراكات متعددة» أو «قفل PIN» من الإعدادات أولاً."
                setTextColor(Color.WHITE)
                textSize = 18f
            })
        }
    }

    private fun renderProfiles() {
        section("ملفات التعريف")
        val active = prefs.getActiveProfile()
        item("الملف الحالي: ${active.name}${if (active.isKidsProfile) " (أطفال)" else ""}") { chooseProfile() }
        item("إضافة ملف تعريف") { createProfile() }
    }

    private fun renderSubscriptions() {
        section("اشتراكات IPTV")
        val activeId = prefs.getSubscriptions().firstOrNull { it.serverUrl == prefs.serverUrl }?.id
        item("الاشتراك الحالي: ${prefs.getSubscriptions().firstOrNull { it.id == activeId }?.title ?: "غير محفوظ"}") { chooseSubscription() }
        item("حفظ الاشتراك الحالي") { saveCurrentSubscription() }
    }

    private fun renderPin() {
        section("قفل PIN")
        item("تعيين أو تغيير رمز PIN للملف الحالي") { setPin() }
        item("إزالة رمز PIN للملف الحالي") {
            prefs.setActiveProfilePin(null)
            toast("تمت إزالة رمز PIN")
            render()
        }
    }

    private fun renderConnectionTest() {
        section("فحص الاتصال")
        item("اختبار الاشتراك الحالي") {
            toast("جارٍ اختبار الاتصال…")
            lifecycleScope.launch {
                repository.validateLogin(prefs.serverUrl, prefs.username, prefs.password)
                    .onSuccess { toast("الاتصال بالاشتراك يعمل") }
                    .onFailure { toast("فشل الاختبار: ${it.message ?: "تحقق من بيانات الاشتراك"}") }
            }
        }
    }

    private fun renderHiddenContentControls() {
        section("المحتوى المخفي")
        item("إظهار كل الفئات والقنوات") {
            prefs.clearHiddenContent()
            toast("تمت استعادة كل المحتويات المخفية للملف الحالي")
        }
    }

    private fun renderHomeCustomization() {
        section("تخصيص الصفحة الرئيسية")
        item("اختيار وترتيب فئات الصفحة الرئيسية") { editHomeCategories() }
    }

    private fun renderBackup() {
        section("نسخ احتياطي مشفّر")
        item("تصدير إعدادات الملف الحالي") { backupExport.launch("alaa-player-settings-backup.json") }
        item("استيراد إعدادات محفوظة") { backupImport.launch(arrayOf("application/json", "text/plain")) }
    }

    private fun editHomeCategories() {
        val types = listOf("live", "sports", "news", "movie", "series", "kids", "documentary", "music")
        val labels = arrayOf("كل القنوات", "الرياضة", "الأخبار", "الأفلام", "المسلسلات", "الأطفال", "الوثائقيات", "الموسيقى")
        val visible = prefs.getHomeCategoryTypes(types)
        val checked = BooleanArray(types.size) { types[it] in visible }
        AlertDialog.Builder(this)
            .setTitle("الفئات الظاهرة")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("حفظ") { _, _ ->
                prefs.saveHomeCategoryTypes(types.filterIndexed { index, _ -> checked[index] })
                toast("تم حفظ تخصيص الصفحة الرئيسية")
            }
            .setNeutralButton("إعادة الافتراضي") { _, _ ->
                prefs.saveHomeCategoryTypes(types)
                toast("تمت استعادة الفئات الافتراضية")
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun chooseProfile() {
        val profiles = prefs.getProfiles()
        AlertDialog.Builder(this)
            .setTitle("اختر الملف")
            .setItems(profiles.map { it.name }.toTypedArray()) { _, index ->
                val profile = profiles[index]
                if (profile.pinHash.isBlank()) {
                    prefs.switchProfile(profile.id)
                    render()
                } else {
                    prompt("أدخل رمز PIN") { pin ->
                        if (prefs.switchProfile(profile.id, pin)) {
                            toast("تم التبديل إلى ${profile.name}")
                            render()
                        } else toast("رمز PIN غير صحيح")
                    }
                }
            }.show()
    }

    private fun createProfile() {
        val input = EditText(this).apply { hint = "اسم الملف" }
        val kids = CheckBox(this).apply { text = "ملف أطفال"; setTextColor(Color.WHITE) }
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), 0, dp(24), 0)
            addView(input)
            addView(kids)
        }
        AlertDialog.Builder(this)
            .setTitle("ملف تعريف جديد")
            .setView(panel)
            .setPositiveButton("إنشاء") { _, _ ->
                prefs.createProfile(input.text.toString(), kids.isChecked)
                render()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun chooseSubscription() {
        val subscriptions = prefs.getSubscriptions()
        if (subscriptions.isEmpty()) {
            toast("احفظ الاشتراك الحالي أولاً")
            return
        }
        AlertDialog.Builder(this)
            .setTitle("اختر الاشتراك")
            .setItems(subscriptions.map { it.title }.toTypedArray()) { _, index ->
                if (prefs.activateSubscription(subscriptions[index].id)) {
                    toast("تم تفعيل ${subscriptions[index].title}")
                    render()
                }
            }.show()
    }

    private fun saveCurrentSubscription() = prompt("اسم الاشتراك") { title ->
        prefs.saveCurrentSubscription(title)
        toast("تم حفظ الاشتراك")
        render()
    }

    private fun setPin() = prompt("رمز PIN جديد", numeric = true) { pin ->
        if (pin.length < 4) {
            toast("يجب أن يحتوي PIN على 4 أرقام على الأقل")
        } else {
            prefs.setActiveProfilePin(pin)
            toast("تم حفظ رمز PIN")
            render()
        }
    }

    private fun section(title: String) {
        content.addView(TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#D8CA28"))
            textSize = 20f
            setPadding(0, dp(18), 0, dp(8))
        })
    }

    private fun item(title: String, onClick: () -> Unit) {
        content.addView(Button(this).apply {
            text = title
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            isAllCaps = false
            isFocusable = true
            setOnClickListener { onClick() }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply {
                bottomMargin = dp(8)
            }
        })
    }

    private fun prompt(title: String, numeric: Boolean = false, onValue: (String) -> Unit) {
        val input = EditText(this).apply {
            inputType = if (numeric) InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD else InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(this).setTitle(title).setView(input)
            .setPositiveButton("حفظ") { _, _ -> onValue(input.text.toString()) }
            .setNegativeButton("إلغاء", null).show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
