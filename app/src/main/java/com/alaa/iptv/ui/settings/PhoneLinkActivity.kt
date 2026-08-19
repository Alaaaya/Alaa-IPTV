package com.alaa.iptv.ui.settings

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.BuildConfig
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.remote.TvProvisioningClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** شاشة محلية لعرض رمز ربط قصير العمر؛ لا يحتوي الرمز على بيانات IPTV. */
class PhoneLinkActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var status: TextView
    private lateinit var code: ImageView
    private lateinit var confirm: Button
    private lateinit var progress: ProgressBar
    private var token: String? = null
    private var pollJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        if (!prefs.isFeatureEnabled(FeatureCatalog.PHONE_QR_PAIRING)) {
            Toast.makeText(this, "ربط الهاتف عبر QR غير مفعّل لهذا التلفزيون.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(48), dp(30), dp(48), dp(30))
            setBackgroundColor(Color.parseColor("#09090B"))
        }
        root.addView(TextView(this).apply {
            text = "ربط الهاتف بـ Alaa Player"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        })
        status = TextView(this).apply {
            text = "جاري إنشاء رمز ربط آمن…"
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(14))
        }
        root.addView(status)
        progress = ProgressBar(this)
        root.addView(progress)
        code = ImageView(this).apply {
            visibility = View.GONE
            setBackgroundColor(Color.WHITE)
            contentDescription = "رمز QR لربط الهاتف"
        }
        root.addView(code, LinearLayout.LayoutParams(dp(430), dp(430)))
        confirm = Button(this).apply {
            text = "تأكيد الربط"
            isAllCaps = false
            visibility = View.GONE
            setOnClickListener { confirmPairing() }
        }
        root.addView(confirm)
        root.addView(Button(this).apply { text = "رجوع"; setOnClickListener { finish() } })
        setContentView(root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(root)
        issuePairing()
    }

    private fun issuePairing() {
        lifecycleScope.launch {
            TvProvisioningClient.issuePhonePairing(prefs.getOrCreateTvId()).onSuccess { issued ->
                token = issued.token
                val origin = BuildConfig.PROVISIONING_API_URL.substringBefore("/api/").trimEnd('/')
                val link = "$origin/phone/pair?token=${issued.token}"
                code.setImageBitmap(QrCodeRenderer.render(link))
                code.visibility = View.VISIBLE
                progress.visibility = View.GONE
                status.text = "امسح الرمز بهاتفك ثم سجّل دخول المالك. الرمز صالح لخمس دقائق ولا يتضمن رابط بث أو كلمة مرور."
                startPolling(issued.token)
            }.onFailure {
                progress.visibility = View.GONE
                status.text = "تعذر إنشاء رمز الربط. تحقق من اتصال الإنترنت ثم أعد المحاولة."
            }
        }
    }

    private fun startPolling(activeToken: String) {
        pollJob?.cancel()
        pollJob = lifecycleScope.launch {
            while (isActive) {
                val current = TvProvisioningClient.getPhonePairingStatus(prefs.getOrCreateTvId(), activeToken).getOrNull()
                when (current?.state) {
                    "claimed" -> {
                        status.text = "تم التحقق من هاتف المالك. راجع التلفزيون ثم اضغط «تأكيد الربط»."
                        confirm.visibility = View.VISIBLE
                    }
                    "confirmed" -> {
                        status.text = "تم ربط الهاتف بنجاح."
                        confirm.visibility = View.GONE
                        return@launch
                    }
                    "expired" -> {
                        status.text = "انتهت صلاحية الرمز. ارجع ثم افتح ربط الهاتف لإنشاء رمز جديد."
                        return@launch
                    }
                }
                delay(3_000)
            }
        }
    }

    private fun confirmPairing() {
        val activeToken = token ?: return
        lifecycleScope.launch {
            confirm.isEnabled = false
            TvProvisioningClient.confirmPhonePairing(prefs.getOrCreateTvId(), activeToken).onSuccess {
                status.text = "تم ربط الهاتف بنجاح."
                confirm.visibility = View.GONE
                pollJob?.cancel()
            }.onFailure {
                status.text = "تعذر تأكيد الربط؛ تأكد أن هاتف المالك أكمل التحقق."
                confirm.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        pollJob?.cancel()
        super.onDestroy()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
