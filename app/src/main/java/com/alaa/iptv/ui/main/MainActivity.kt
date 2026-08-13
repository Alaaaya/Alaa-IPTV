package com.alaa.iptv.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityMainBinding
import com.alaa.iptv.ui.player.PlayerActivity
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
    private var allChannels: List<Channel> = emptyList()
    private var currentMode = MODE_LIVE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)
        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_LIVE

        setupChannelsList()
        loadContent()
    }

    private fun setupChannelsList() {
        channelAdapter = ChannelAdapter(
            emptyList(),
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

    private fun loadContent() {
        lifecycleScope.launch {
            try {
                val result = when (currentMode) {
                    MODE_MOVIES -> repository.getMovies(null).map { list -> list.map { it.toChannel() } }
                    MODE_SERIES -> repository.getSeries(null).map { list -> list.map { it.toChannel() } }
                    else -> repository.getLiveStreams(null)
                }

                result.onSuccess { loadedChannels ->
                    val decoratedChannels = decorateChannels(loadedChannels)
                    allChannels = if (currentMode == MODE_FAVORITES) {
                        decoratedChannels.filter { it.isFavorite }
                    } else {
                        decoratedChannels
                    }
                    updateChannelList()
                }.onFailure { error ->
                    Log.e(TAG, "Unable to load content", error)
                    allChannels = emptyList()
                    binding.previewTitle.text = "تعذر تحميل القنوات"
                    binding.previewSubtitle.text = error.message ?: "تحقق من اتصال الإنترنت والسيرفر"
                    binding.channelCounterFooter.text = "0 / 0"
                    channelAdapter.updateChannels(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading content", e)
            }
        }
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
                binding.channelsRecyclerView.findViewHolderForAdapterPosition(position)?.itemView?.requestFocus()
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
        binding.channelCounterFooter.text = "$pos / ${allChannels.size}"
    }

    private fun showChannelOptions(channel: Channel) {
        val isFavorite = channel.isFavorite
        val channelPosition = allChannels.indexOfFirst { channelKey(it) == channelKey(channel) }
        val actions = mutableListOf<Pair<String, () -> Unit>>()

        actions += if (isFavorite) {
            "إزالة من المفضلة" to { toggleFavorite(channel) }
        } else {
            "إضافة إلى المفضلة" to { toggleFavorite(channel) }
        }

        if (currentMode != MODE_FAVORITES && channelPosition > 0) {
            actions += "نقل للأعلى" to { moveChannel(channel, -1) }
        }
        if (currentMode != MODE_FAVORITES && channelPosition in 0 until allChannels.lastIndex) {
            actions += "نقل للأسفل" to { moveChannel(channel, 1) }
        }

        AlertDialog.Builder(this)
            .setTitle(channel.name)
            .setItems(actions.map { it.first }.toTypedArray()) { _, which -> actions[which].second.invoke() }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun toggleFavorite(channel: Channel) {
        val key = channelKey(channel)
        val favorites = prefs.getFavorites().toMutableSet()
        val adding = favorites.add(key)
        if (!adding) favorites.remove(key)
        prefs.saveFavorites(favorites)

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

    private fun moveChannel(channel: Channel, offset: Int) {
        val oldPosition = allChannels.indexOfFirst { channelKey(it) == channelKey(channel) }
        val newPosition = oldPosition + offset
        if (oldPosition !in allChannels.indices || newPosition !in allChannels.indices) return

        val reordered = allChannels.toMutableList()
        val moved = reordered.removeAt(oldPosition)
        reordered.add(newPosition, moved)
        allChannels = reordered.mapIndexed { position, item -> item.copy(position = position) }
        prefs.saveChannelOrder(allChannels.map(::channelKey))
        updateChannelList(moved)
        Toast.makeText(this, "تم حفظ ترتيب القنوات", Toast.LENGTH_SHORT).show()
    }

    private fun channelKey(channel: Channel): String = "${channel.streamType.lowercase()}:${channel.streamId}"

    private fun playChannel(channel: Channel) {
        val url = channel.directSource ?: channel.getStreamUrl(prefs.serverUrl, prefs.username, prefs.password)
        if (url.isNullOrBlank()) return

        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra("STREAM_URL", url)
                .putExtra("CHANNEL_NAME", channel.name)
                .putExtra("STREAM_TYPE", channel.streamType)
        )
    }

    private fun com.alaa.iptv.data.models.Movie.toChannel() = Channel(
        streamId = streamId, num = streamId, name = name, streamType = "movie",
        streamIcon = streamIcon, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )

    private fun com.alaa.iptv.data.models.Series.toChannel() = Channel(
        streamId = seriesId, num = seriesId, name = name, streamType = "series",
        streamIcon = cover, epgChannelId = null, added = null, categoryId = categoryId,
        categoryName = null, customSid = null, tvArchive = 0, directSource = null, isFavorite = isFavorite
    )
}
