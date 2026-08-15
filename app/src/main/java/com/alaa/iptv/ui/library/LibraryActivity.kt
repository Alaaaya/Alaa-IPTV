package com.alaa.iptv.ui.library

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.preferences.MediaLibraryEntry
import com.alaa.iptv.ui.player.PlayerActivity

class LibraryActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A1426"))
            setPadding(dp(36), dp(28), dp(36), dp(28))
        }
        root.addView(TextView(this).apply {
            text = "مكتبتي"
            setTextColor(Color.WHITE)
            textSize = 28f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            addView(content)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        })
        root.addView(Button(this).apply { text = "← رجوع"; setOnClickListener { finish() } })
        setContentView(root)
        render()
    }

    private fun render() {
        content.removeAllViews()
        if (prefs.isFeatureEnabled(FeatureCatalog.WATCHLIST)) {
            section("المشاهدة لاحقاً", prefs.getWatchlist())
        }
        if (prefs.isFeatureEnabled(FeatureCatalog.WATCH_HISTORY)) {
            section("استئناف المشاهدة", prefs.getPlaybackHistory())
        }
        if (prefs.isFeatureEnabled(FeatureCatalog.RECENT_CHANNELS)) {
            section("القنوات الحديثة", prefs.getRecentChannels())
        }
        if (content.childCount == 0) empty("فعّل ميزات المكتبة من الإعدادات أولاً.")
    }

    private fun section(title: String, entries: List<MediaLibraryEntry>) {
        content.addView(TextView(this).apply {
            text = title
            setTextColor(Color.parseColor("#D8CA28"))
            textSize = 20f
            setPadding(0, dp(18), 0, dp(8))
        })
        if (entries.isEmpty()) {
            empty("لا توجد عناصر محفوظة بعد.")
        } else {
            entries.forEach { entry ->
                content.addView(Button(this).apply {
                    text = entry.title
                    gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    isAllCaps = false
                    setOnClickListener { play(entry) }
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)).apply {
                        bottomMargin = dp(8)
                    }
                })
            }
        }
    }

    private fun empty(text: String) = content.addView(TextView(this).apply {
        this.text = text
        setTextColor(Color.LTGRAY)
        textSize = 15f
        setPadding(0, 0, 0, dp(10))
    })

    private fun play(entry: MediaLibraryEntry) {
        if (entry.streamType == "series") {
            empty("اختر الحلقة من قسم المسلسلات لتشغيلها.")
            return
        }
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", entry.streamUrl)
            .putExtra("CHANNEL_NAME", entry.title)
            .putExtra("STREAM_TYPE", entry.streamType)
            .putExtra(PlayerActivity.EXTRA_RESUME_POSITION_MS, entry.positionMs))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
