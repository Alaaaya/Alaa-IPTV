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
import kotlinx.coroutines.launch

/** يعرض QR لصفحة وصف آمنة للمحتوى، وليس لرابط تشغيل IPTV. */
class ContentShareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = AppPreferences(this)
        if (!prefs.isFeatureEnabled(FeatureCatalog.CONTENT_QR_SHARE)) {
            Toast.makeText(this, "مشاركة المحتوى عبر QR غير مفعّلة لهذا التلفزيون.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val contentType = intent.getStringExtra(EXTRA_CONTENT_TYPE).orEmpty()
        val contentKey = intent.getStringExtra(EXTRA_CONTENT_KEY).orEmpty()
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val posterUrl = intent.getStringExtra(EXTRA_POSTER_URL)
        if (contentType !in setOf("live", "movie", "series") || contentKey.isBlank() || title.isBlank()) {
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
            text = "مشاركة $title"
            setTextColor(Color.WHITE)
            textSize = 25f
            gravity = Gravity.CENTER
            maxLines = 2
        })
        val status = TextView(this).apply {
            text = "جاري إنشاء بطاقة مشاركة آمنة…"
            setTextColor(Color.LTGRAY)
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(14))
        }
        root.addView(status)
        val progress = ProgressBar(this)
        root.addView(progress)
        val qr = ImageView(this).apply { visibility = View.GONE; setBackgroundColor(Color.WHITE) }
        root.addView(qr, LinearLayout.LayoutParams(dp(430), dp(430)))
        root.addView(Button(this).apply { text = "رجوع"; setOnClickListener { finish() } })
        setContentView(root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(root)
        lifecycleScope.launch {
            TvProvisioningClient.createContentShare(prefs.getOrCreateTvId(), contentType, contentKey, title, posterUrl)
                .onSuccess { issued ->
                    val origin = BuildConfig.PROVISIONING_API_URL.substringBefore("/api/").trimEnd('/')
                    qr.setImageBitmap(QrCodeRenderer.render("$origin/phone/share?token=${issued.token}"))
                    qr.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    status.text = "امسح الرمز بهاتفك لفتح بطاقة المحتوى. لا يتضمن الرمز رابط بث أو بيانات اشتراك، وينتهي خلال عشر دقائق."
                }
                .onFailure {
                    progress.visibility = View.GONE
                    status.text = "تعذر إنشاء بطاقة المشاركة. تحقق من الاتصال ثم أعد المحاولة."
                }
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_CONTENT_TYPE = "content_type"
        const val EXTRA_CONTENT_KEY = "content_key"
        const val EXTRA_TITLE = "content_title"
        const val EXTRA_POSTER_URL = "content_poster_url"
    }
}
