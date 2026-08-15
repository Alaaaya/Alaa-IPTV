package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.ui.player.PlayerActivity
import com.alaa.iptv.ui.player.PlayableChannel
import com.alaa.iptv.ui.player.PlayerChannelNavigator
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.ui.common.ControlPlaneActivityGuard
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_MODE = "MODE"
        const val MODE_LIVE = "live"
        const val MODE_MOVIES = "movies"
        const val MODE_SERIES = "series"
        const val MODE_FAVORITES = "favorites"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private lateinit var channelAdapter: ChannelAdapter
    private lateinit var liveCategoryAdapter: LiveCategoryAdapter
    private var allChannels: List<Channel> = emptyList()
    private var currentMode = MODE_LIVE
    private var liveCategories: List<Category> = emptyList()
    private var selectedLiveCategory: Category? = null
    private var currentLivePage = 0
    private var hasMoreLivePages = false
    private var movingChannelKey: String? = null
    private var moveSnapshot: List<Channel> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        DisplayTheme.applyLive(binding, prefs)
        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_LIVE
        prefs.lastVisitedSection = currentMode

        when (currentMode) {
            MODE_MOVIES -> {
                startActivity(Intent(this, MoviesActivity::class.java))
                finish()
                return
            }
            MODE_SERIES -> {
                startActivity(Intent(this, SeriesActivity::class.java))
                finish()
                return
            }
        }

        setupChannelsList()
        setupLiveCategoriesList()
        binding.filterAll.setOnClickListener { focusSelectedCategory() }
        binding.channelCounterFooter.setOnClickListener { loadMoreChannels() }
        lifecycleScope.launch {
            if (ControlPlaneActivityGuard.refreshAndEnforce(this@MainActivity, prefs, force = true)) {
                if (currentMode == MODE_FAVORITES) loadFavoriteChannels() else loadLiveCategories()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            ControlPlaneActivityGuard.refreshAndEnforce(this@MainActivity, prefs)
        }
    }

    private fun setupChannelsList() {
        channelAdapter = ChannelAdapter(
            emptyList(),
            prefs.displayTheme,
            onChannelClick = { channel -> playChannel(channel) },
            onChannelLongClick = { channel -> showChannelOptions(channel) }
        )

        channelAdapter.setOnChannelFocusListener { channel ->
            updatePreview(channel)
        }

        binding.channelsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }

    private fun setupLiveCategoriesList() {
        liveCategoryAdapter = LiveCategoryAdapter(prefs.displayTheme) { category ->
            if (currentMode == MODE_FAVORITES) {
                binding.channelsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                    ?: binding.channelsRecyclerView.requestFocus()
            } else {
                selectLiveCategory(category)
            }
        }
        val categorySpec = DisplayTheme.liveCategorySpec(prefs.displayTheme)
        binding.liveCategoriesRecyclerView.apply {
            layoutManager = when (categorySpec.placement) {
                DisplayTheme.LiveCategoryPlacement.TOP_RAIL ->
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                DisplayTheme.LiveCategoryPlacement.SIDE_GRID ->
                    GridLayoutManager(this@MainActivity, categorySpec.spanCount)
                DisplayTheme.LiveCategoryPlacement.SIDE_LIST ->
                    LinearLayoutManager(this@MainActivity)
            }
            adapter = liveCategoryAdapter
        }
    }

    private fun loadLiveCategories() {
        lifecycleScope.launch {
            try {
                val categoriesResult = repository.getLiveCategories()
                if (categoriesResult.isFailure) {
                    showContentError(categoriesResult.exceptionOrNull())
                    return@launch
                }
                liveCategories = categoriesResult.getOrDefault(emptyList())
                liveCategoryAdapter.submit(liveCategories, prefs.lastLiveCategoryId)
                val selected = liveCategories.firstOrNull { it.categoryId == prefs.lastLiveCategoryId }
                    ?: liveCategories.firstOrNull()
                if (selected == null) {
                    binding.previewTitle.text = "لا توجد فئات قنوات"
                    binding.previewSubtitle.text = "تحقق من اشتراك IPTV"
                    return@launch
                }
                selectLiveCategory(selected)
            } catch (e: Exception) {
                showContentError(e)
            }
        }
    }

    private fun loadFavoriteChannels() {
        val favoriteChannels = prefs.getFavoriteChannels().map { it.copy(isFavorite = true) }
        val favoritesCategory = Category(
            categoryId = "favorites",
            categoryName = "المفضلة",
            channelCount = favoriteChannels.size
        )
        liveCategories = listOf(favoritesCategory)
        selectedLiveCategory = favoritesCategory
        currentLivePage = 0
        hasMoreLivePages = false
        binding.filterAll.text = "المفضلة"
        binding.categoryTitle.text = "القنوات المفضلة"
        liveCategoryAdapter.submit(liveCategories, favoritesCategory.categoryId)
        allChannels = decorateChannels(favoriteChannels).filter { it.isFavorite }
        updateChannelList()
    }

    private fun selectLiveCategory(category: Category) {
        prefs.lastLiveCategoryId = category.categoryId
        selectedLiveCategory = category
        currentLivePage = 0
        hasMoreLivePages = false
        binding.filterAll.text = "الفئات"
        binding.categoryTitle.text = "البث المباشر / ${category.categoryName}"
        liveCategoryAdapter.submit(liveCategories, category.categoryId)
        loadContent(category.categoryId, page = 0, append = false)
    }

    private fun loadMoreChannels() {
        val category = selectedLiveCategory ?: return
        if (!hasMoreLivePages) {
            Toast.makeText(this, "تم تحميل كل قنوات هذه الفئة", Toast.LENGTH_SHORT).show()
            return
        }
        loadContent(category.categoryId, page = currentLivePage + 1, append = true)
    }

    private fun loadContent(categoryId: String, page: Int, append: Boolean) {
        lifecycleScope.launch {
            try {
                val result = repository.getLiveStreams(categoryId, page)

                result.onSuccess { loadedChannels ->
                    val namedChannels = loadedChannels.map { channel ->
                        channel.copy(categoryName = selectedLiveCategory?.categoryName)
                    }
                    val decoratedChannels = decorateChannels(if (append) allChannels + namedChannels else namedChannels)
                    allChannels = if (currentMode == MODE_FAVORITES) {
                        decoratedChannels.filter { it.isFavorite }
                    } else {
                        decoratedChannels
                    }
                    currentLivePage = page
                    hasMoreLivePages = loadedChannels.size >= 100
                    updateChannelList(if (append) allChannels.firstOrNull() else null)
                }.onFailure(::showContentError)
            } catch (e: Exception) {
                showContentError(e)
            }
        }
    }

    private fun showContentError(error: Throwable?) {
        Log.e(TAG, "Unable to load content", error)
        allChannels = emptyList()
        binding.previewTitle.text = "تعذر تحميل القنوات"
        binding.previewSubtitle.text = error?.message ?: "تحقق من اتصال الإنترنت والسيرفر"
        binding.channelCounterFooter.text = "0 / 0"
        channelAdapter.updateChannels(emptyList())
    }

    private fun decorateChannels(channels: List<Channel>): List<Channel> {
        val favoriteKeys = prefs.getFavorites()
        val orderedKeys = prefs.getChannelOrder()
        val orderMap = orderedKeys.withIndex().associate { it.value to it.index }

        return channels
            .sortedWith(compareBy<Channel> { orderMap[channelKey(it)] ?: Int.MAX_VALUE }.thenBy { it.position })
            .map { channel -> channel.copy(isFavorite = favoriteKeys.contains(channelKey(channel))) }
    }

    private fun updateChannelList(focusChannel: Channel? = null) {
        channelAdapter.updateChannels(allChannels)

        if (allChannels.isNotEmpty()) {
            val target = focusChannel?.let { selected ->
                allChannels.firstOrNull { channelKey(it) == channelKey(selected) }
            } ?: allChannels.first()
            updatePreview(target)
            val position = allChannels.indexOf(target)
            binding.channelsRecyclerView.post {
                binding.channelsRecyclerView.scrollToPosition(position)
                val requestVisibleChannelFocus = {
                    binding.channelsRecyclerView
                        .findViewHolderForAdapterPosition(position)
                        ?.itemView
                        ?.requestFocus()
                        ?: false
                }
                if (!requestVisibleChannelFocus()) {
                    binding.channelsRecyclerView.post { requestVisibleChannelFocus() }
                }
            }
        } else {
            binding.previewTitle.text = if (currentMode == MODE_FAVORITES) "لا توجد قنوات مفضلة" else "لا توجد قنوات متاحة"
            binding.previewSubtitle.text = if (currentMode == MODE_FAVORITES) {
                "اضغط مطولاً على أي قناة لإضافتها إلى المفضلة"
            } else {
                "تحقق من الاشتراك أو الفئة المختارة"
            }
            binding.channelCounterFooter.text = "0 / 0"
        }
    }

    private fun updatePreview(channel: Channel) {
        binding.previewTitle.text = channel.name
        binding.previewSubtitle.text = channel.categoryName ?: "بث مباشر"

        Glide.with(this)
            .load(channel.streamIcon)
            .placeholder(R.drawable.bg_hero_sports)
            .error(R.drawable.bg_hero_sports)
            .into(binding.previewImage)

        val pos = allChannels.indexOfFirst { channelKey(it) == channelKey(channel) } + 1
        binding.channelCounterFooter.text = if (hasMoreLivePages) {
            "$pos / ${allChannels.size}  •  اضغط هنا لتحميل المزيد"
        } else {
            "$pos / ${allChannels.size}"
        }
    }

    private fun showChannelOptions(channel: Channel) {
        val isFavorite = channel.isFavorite
        val actions = mutableListOf<Pair<String, () -> Unit>>()

        actions += if (isFavorite) {
            "إزالة من المفضلة" to { toggleFavorite(channel) }
        } else {
            "إضافة إلى المفضلة" to { toggleFavorite(channel) }
        }

        if (currentMode != MODE_FAVORITES && allChannels.size > 1) {
            actions += "نقل القناة" to { startChannelMove(channel) }
        }
        actions += "تفاصيل القناة" to { showChannelDetails(channel) }

        AlertDialog.Builder(this)
            .setTitle(channel.name)
            .setItems(actions.map { it.first }.toTypedArray()) { _, which -> actions[which].second.invoke() }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showChannelDetails(channel: Channel) {
        val quality = when {
            channel.name.contains("4k", ignoreCase = true) || channel.name.contains("uhd", ignoreCase = true) -> "4K / UHD"
            channel.name.contains("fhd", ignoreCase = true) || channel.name.contains("1080", ignoreCase = true) -> "FHD"
            channel.name.contains("hd", ignoreCase = true) -> "HD"
            else -> "SD أو غير محددة"
        }
        AlertDialog.Builder(this)
            .setTitle(channel.name)
            .setMessage(listOf(
                channel.categoryName?.let { "الفئة: $it" },
                "الجودة: $quality",
                channel.num.takeIf { it.isNotBlank() }?.let { "رقم القناة: $it" }
            ).filterNotNull().joinToString("\n"))
            .setPositiveButton("تشغيل") { _, _ -> playChannel(channel) }
            .setNegativeButton("إغلاق", null)
            .show()
    }

    private fun toggleFavorite(channel: Channel) {
        val key = channelKey(channel)
        val favorites = prefs.getFavorites().toMutableSet()
        val adding = favorites.add(key)
        if (!adding) favorites.remove(key)
        prefs.saveFavorites(favorites)
        if (adding) prefs.saveFavoriteChannel(channel) else prefs.removeFavoriteChannel(channel)

        allChannels = allChannels
            .map { item -> if (channelKey(item) == key) item.copy(isFavorite = adding) else item }
            .let { items -> if (currentMode == MODE_FAVORITES && !adding) items.filter { it.isFavorite } else items }

        updateChannelList(channel)
        Toast.makeText(
            this,
            if (adding) "تمت إضافة ${channel.name} إلى المفضلة" else "تمت إزالة ${channel.name} من المفضلة",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startChannelMove(channel: Channel) {
        movingChannelKey = channelKey(channel)
        moveSnapshot = allChannels
        binding.channelCounterFooter.text = "وضع النقل: ↑ ↓ تحريك  •  OK حفظ  •  رجوع إلغاء"
        binding.previewSubtitle.text = "حرّك ${channel.name} بالأسهم ثم اضغط OK للحفظ"
        focusChannelForMove(channelKey(channel))
    }

    private fun moveChannelStep(offset: Int) {
        val key = movingChannelKey ?: return
        val moved = ChannelOrderMover.move(allChannels, key, offset)
        if (moved === allChannels || moved == allChannels) return
        allChannels = moved
        focusChannelForMove(key)
    }

    private fun focusChannelForMove(key: String) {
        val position = allChannels.indexOfFirst { channelKey(it) == key }
        if (position < 0) return
        channelAdapter.updateChannels(allChannels)
        binding.channelsRecyclerView.post {
            binding.channelsRecyclerView.scrollToPosition(position)
            binding.channelsRecyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
        }
    }

    private fun finishChannelMove(save: Boolean) {
        val key = movingChannelKey ?: return
        if (save) {
            prefs.saveChannelOrder(allChannels.map(::channelKey))
            Toast.makeText(this, "تم حفظ ترتيب القناة", Toast.LENGTH_SHORT).show()
        } else {
            allChannels = moveSnapshot
            Toast.makeText(this, "تم إلغاء نقل القناة", Toast.LENGTH_SHORT).show()
        }
        movingChannelKey = null
        moveSnapshot = emptyList()
        updateChannelList(allChannels.firstOrNull { channelKey(it) == key })
    }

    private fun channelKey(channel: Channel): String = ChannelOrderMover.keyFor(channel)

    private fun playChannel(channel: Channel) {
        val url = channel.directSource ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isNullOrBlank()) return

        val liveChannels = if (channel.streamType.equals("live", ignoreCase = true)) {
            allChannels.mapNotNull { item ->
                if (!item.streamType.equals("live", ignoreCase = true)) return@mapNotNull null
                val itemUrl = item.directSource ?: item.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
                itemUrl.takeIf { it.isNotBlank() }?.let {
                    PlayableChannel(item.name, it, item.streamType)
                }
            }
        } else {
            emptyList()
        }
        val playerIndex = liveChannels.indexOfFirst { it.streamUrl == url }
        if (playerIndex >= 0) PlayerChannelNavigator.setChannels(liveChannels)

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
                .putExtra("STREAM_TYPE", channel.streamType)
                .putExtra(PlayerActivity.EXTRA_CHANNEL_INDEX, playerIndex)
        )
    }

    private fun focusSelectedCategory() {
        if (liveCategories.isEmpty()) return
        val position = liveCategories.indexOfFirst { it.categoryId == selectedLiveCategory?.categoryId }
            .takeIf { it >= 0 } ?: 0
        binding.liveCategoriesRecyclerView.scrollToPosition(position)
        binding.liveCategoriesRecyclerView.post {
            binding.liveCategoriesRecyclerView.findViewHolderForAdapterPosition(position)
                ?.itemView?.requestFocus()
                ?: binding.liveCategoriesRecyclerView.requestFocus()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && movingChannelKey != null) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    moveChannelStep(-1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    moveChannelStep(1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    finishChannelMove(save = true)
                    return true
                }
                KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE -> {
                    finishChannelMove(save = false)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val categorySpec = DisplayTheme.liveCategorySpec(prefs.displayTheme)
        if (categorySpec.placement == DisplayTheme.LiveCategoryPlacement.TOP_RAIL) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && binding.channelsRecyclerView.hasFocus()) {
                focusSelectedCategory()
                return true
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && binding.liveCategoriesRecyclerView.hasFocus()) {
                binding.channelsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                    ?: binding.channelsRecyclerView.requestFocus()
                return true
            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && binding.channelsRecyclerView.hasFocus()) {
            focusSelectedCategory()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && binding.liveCategoriesRecyclerView.hasFocus()) {
            binding.channelsRecyclerView.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
                ?: binding.channelsRecyclerView.requestFocus()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
