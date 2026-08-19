package com.alaa.iptv.ui.library

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.ui.main.SeriesDetailsActivity
import com.alaa.iptv.ui.player.PlayerActivity
import kotlinx.coroutines.launch
import java.util.Locale

/** بحث محلي فوق الفهارس المتاحة: لا يبدأ تحميل كل فئات الكتالوج أو صفحاته. */
class SearchActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private lateinit var queryInput: EditText
    private lateinit var results: LinearLayout
    private lateinit var status: TextView
    private lateinit var progress: ProgressBar
    private lateinit var tabsContainer: LinearLayout
    private var searchable: List<SearchEntry> = emptyList()
    private var selectedType: String? = null
    private val voiceSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val heard = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim().orEmpty()
        if (result.resultCode == RESULT_OK && heard.isNotBlank()) {
            queryInput.setText(heard)
            queryInput.setSelection(heard.length)
            search()
        } else {
            status.text = "لم يتم التقاط عبارة بحث صوتي؛ يمكنك الكتابة في حقل البحث."
        }
    }
    private val microphonePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchVoiceSearch() else status.text = "يلزم السماح بالميكروفون لاستخدام البحث الصوتي."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)
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
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setSelectAllOnFocus(false)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search()
                    true
                } else false
            }
        }
        root.addView(queryInput)
        root.addView(Button(this).apply { text = "بحث"; setOnClickListener { search() } })
        if (prefs.isFeatureEnabled(FeatureCatalog.VOICE_SEARCH)) {
            root.addView(Button(this).apply {
                text = "بحث صوتي"
                isAllCaps = false
                setOnClickListener { requestVoiceSearch() }
            })
        }

        tabsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(4))
        }
        root.addView(tabsContainer)
        createSearchTabs()

        status = TextView(this).apply { setTextColor(Color.LTGRAY); textSize = 14f; setPadding(0, dp(8), 0, dp(8)) }
        root.addView(status)
        progress = ProgressBar(this).apply { visibility = View.GONE }
        root.addView(progress)
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply {
            addView(results)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        })
        root.addView(Button(this).apply { text = "← رجوع"; setOnClickListener { finish() } })
        setContentView(root)
        com.alaa.iptv.ui.common.PremiumNeonFocus.install(root)
        loadSearchIndex()
    }

    private fun createSearchTabs() {
        listOf(null to "الكل", "live" to "القنوات", "movie" to "الأفلام", "series" to "المسلسلات").forEach { (type, title) ->
            tabsContainer.addView(Button(this).apply {
                text = title
                isAllCaps = false
                setOnClickListener {
                    selectedType = type
                    updateTabState()
                    if (queryInput.text.length >= 2) search()
                }
            })
        }
        updateTabState()
    }

    private fun updateTabState() {
        for (index in 0 until tabsContainer.childCount) {
            val button = tabsContainer.getChildAt(index) as? Button ?: continue
            val type = listOf<String?>(null, "live", "movie", "series")[index]
            button.alpha = if (selectedType == type) 1f else 0.55f
            button.isSelected = selectedType == type
        }
    }

    private fun loadSearchIndex() {
        progress.visibility = View.VISIBLE
        status.text = "تجهيز البحث في المحتوى المتاح…"
        lifecycleScope.launch {
            val channels = repository.getLiveStreams(null).getOrDefault(emptyList()).map { SearchEntry(it) }
            val movies = repository.getMovies(null).getOrDefault(emptyList()).map { movie ->
                SearchEntry(
                    channel = Channel(movie.streamId, movie.streamId, movie.name, "movie", movie.streamIcon, null, null, movie.categoryId, null, null, 0, movie.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password), 0),
                    movie = movie
                )
            }
            val series = repository.getSeries(null).getOrDefault(emptyList()).map { item ->
                SearchEntry(
                    channel = Channel(item.seriesId, item.seriesId, item.name, "series", item.cover, null, null, item.categoryId, null, null, 0, null, 0),
                    series = item
                )
            }
            searchable = (channels + movies + series)
                .distinctBy { "${it.channel.streamType}:${it.channel.streamId}" }
            progress.visibility = View.GONE
            status.text = "جاهز للبحث في ${searchable.size} عنصر متاح حالياً."
            prefs.lastSearchQuery.takeIf { it.length >= 2 }?.let { previous ->
                queryInput.setText(previous)
                queryInput.setSelection(previous.length)
                search()
            }
        }
    }

    private fun search() {
        val query = queryInput.text.toString().trim()
        if (query.length < 2) {
            status.text = "اكتب حرفين على الأقل للبحث."
            results.removeAllViews()
            return
        }
        prefs.lastSearchQuery = query
        val found = searchable.asSequence()
            .filter { selectedType == null || it.channel.streamType == selectedType }
            .filter { it.channel.name.contains(query, ignoreCase = true) }
            .sortedWith(compareBy<SearchEntry> { typeRank(it.channel.streamType) }.thenBy { it.channel.name.lowercase() })
            .take(MAX_RESULTS)
            .toList()
        results.removeAllViews()
        status.text = if (found.isEmpty()) "لا توجد نتائج مطابقة ضمن تبويب ${selectedTabLabel()}." else "${found.size} نتيجة ضمن تبويب ${selectedTabLabel()}."
        found.forEach { entry ->
            results.addView(Button(this).apply {
                text = "${typeLabel(entry.channel.streamType)} — ${entry.channel.name}"
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                isAllCaps = false
                contentDescription = "${typeLabel(entry.channel.streamType)} ${entry.channel.name}"
                setOnClickListener { open(entry) }
            })
        }
    }

    private fun requestVoiceSearch() {
        if (!prefs.isFeatureEnabled(FeatureCatalog.VOICE_SEARCH)) {
            status.text = "البحث الصوتي غير مفعّل لهذا التلفزيون."
            return
        }
        if (packageManager.resolveActivity(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0) == null) {
            status.text = "هذا الجهاز لا يدعم التعرف الصوتي؛ استخدم البحث النصي."
            return
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        launchVoiceSearch()
    }

    private fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "قل اسم القناة أو الفيلم أو المسلسل")
        }
        runCatching { voiceSearchLauncher.launch(intent) }
            .onFailure { status.text = "تعذر فتح البحث الصوتي؛ استخدم البحث النصي." }
    }

    private fun open(entry: SearchEntry) {
        entry.series?.let { series ->
            startActivity(Intent(this, SeriesDetailsActivity::class.java).putExtra(SeriesDetailsActivity.EXTRA_SERIES, series))
            return
        }
        val url = entry.channel.directSource ?: entry.channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isBlank()) {
            status.text = "تعذر إنشاء رابط التشغيل لهذا المحتوى."
            return
        }
        startActivity(Intent(this, PlayerActivity::class.java)
            .putExtra("STREAM_URL", url)
            .putExtra("CHANNEL_NAME", entry.channel.name)
            .putExtra("STREAM_TYPE", entry.channel.streamType))
    }

    private fun selectedTabLabel() = when (selectedType) { "live" -> "القنوات"; "movie" -> "الأفلام"; "series" -> "المسلسلات"; else -> "الكل" }
    private fun typeLabel(type: String) = when (type) { "movie" -> "فيلم"; "series" -> "مسلسل"; else -> "قناة" }
    private fun typeRank(type: String) = when (type) { "live" -> 0; "movie" -> 1; "series" -> 2; else -> 3 }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private data class SearchEntry(val channel: Channel, val movie: Movie? = null, val series: Series? = null)

    private companion object {
        const val MAX_RESULTS = 80
    }
}
