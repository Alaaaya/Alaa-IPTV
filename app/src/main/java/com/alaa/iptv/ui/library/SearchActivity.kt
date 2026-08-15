package com.alaa.iptv.ui.library

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private lateinit var queryInput: EditText
    private lateinit var results: LinearLayout
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private var searchable: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A1426"))
            setPadding(dp(36), dp(28), dp(36), dp(28))
        }
        root.addView(TextView(this).apply { text = "البحث الشامل"; setTextColor(Color.WHITE); textSize = 28f })
        queryInput = EditText(this).apply {
            hint = "اكتب اسم القناة أو الفيلم أو المسلسل"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { _, _, _ -> search(); true }
        }
        root.addView(queryInput)
        root.addView(Button(this).apply { text = "بحث"; setOnClickListener { search() } })
        status = TextView(this).apply { setTextColor(Color.LTGRAY); textSize = 14f; setPadding(0, dp(8), 0, dp(8)) }
        root.addView(status)
        progress = ProgressBar(this).apply { visibility = android.view.View.GONE }
        root.addView(progress)
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            addView(results)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        })
        root.addView(Button(this).apply { text = "← رجوع"; setOnClickListener { finish() } })
        setContentView(root)
        loadSearchIndex()
    }

    private fun loadSearchIndex() {
        progress.visibility = android.view.View.VISIBLE
        status.text = "تجهيز البحث دون تحميل الكتالوج كاملاً…"
        lifecycleScope.launch {
            val channels = repository.getLiveStreams(null).getOrDefault(emptyList())
            val movies = repository.getMovies(null).getOrDefault(emptyList()).map { movie ->
                Channel(movie.streamId, movie.streamId, movie.name, "movie", movie.streamIcon, null, null, movie.categoryId, null, null, 0, null, 0)
            }
            val series = repository.getSeries(null).getOrDefault(emptyList()).map { series ->
                Channel(series.seriesId, series.seriesId, series.name, "series", series.cover, null, null, series.categoryId, null, null, 0, null, 0)
            }
            searchable = channels + movies + series
            progress.visibility = android.view.View.GONE
            status.text = "جاهز للبحث في المحتوى المحمّل حالياً."
        }
    }

    private fun search() {
        val query = queryInput.text.toString().trim()
        if (query.length < 2) {
            status.text = "اكتب حرفين على الأقل للبحث."
            return
        }
        val found = searchable.filter { it.name.contains(query, ignoreCase = true) }.take(80)
        results.removeAllViews()
        status.text = "${found.size} نتيجة"
        found.forEach { channel ->
            results.addView(Button(this).apply {
                text = "${typeLabel(channel.streamType)} — ${channel.name}"
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                isAllCaps = false
                setOnClickListener { open(channel) }
            })
        }
    }

    private fun open(channel: Channel) {
        if (channel.streamType == "series") {
            status.text = "اختر المسلسل من قسم المسلسلات لعرض الحلقات."
            return
        }
        val url = channel.directSource ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", channel.name)
            .putExtra("STREAM_TYPE", channel.streamType))
    }

    private fun typeLabel(type: String) = when (type) { "movie" -> "فيلم"; "series" -> "مسلسل"; else -> "قناة" }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
