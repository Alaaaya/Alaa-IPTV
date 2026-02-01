package com.alaaaya.iptv.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.IptvRepository
import com.alaaaya.iptv.data.api.XtreamCodesApi
import com.alaaaya.iptv.data.db.AppDatabase
import com.alaaaya.iptv.data.models.Category
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.ui.player.PlayerActivity
import com.alaaaya.iptv.utils.Constants
import com.alaaaya.iptv.utils.gone
import com.alaaaya.iptv.utils.showToast
import com.alaaaya.iptv.utils.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnLiveTV: Button
    private lateinit var btnMovies: Button
    private lateinit var btnSeries: Button
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var recyclerContent: RecyclerView
    private lateinit var ivPreview: ImageView
    private lateinit var tvPreviewTitle: TextView
    private lateinit var tvPreviewDescription: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnFavorite: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var viewModel: MainViewModel
    private lateinit var repository: IptvRepository
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var channelAdapter: ChannelAdapter
    
    private var currentContentType = "live"
    private var selectedChannel: Channel? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViewModel()
        initViews()
        setupRecyclerViews()
        setupListeners()
        observeData()
        
        // Load initial data
        viewModel.setContentType("live")
        viewModel.loadData()
    }
    
    private fun initViewModel() {
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val serverUrl = prefs.getString(Constants.KEY_SERVER_URL, "") ?: ""
        val username = prefs.getString(Constants.KEY_USERNAME, "") ?: ""
        val password = prefs.getString(Constants.KEY_PASSWORD, "") ?: ""
        
        val database = AppDatabase.getInstance(this)
        val api = createApiClient(serverUrl)
        
        repository = IptvRepository(api, database, serverUrl, username, password)
        viewModel = MainViewModel(repository)
    }
    
    private fun initViews() {
        btnLiveTV = findViewById(R.id.btnLiveTV)
        btnMovies = findViewById(R.id.btnMovies)
        btnSeries = findViewById(R.id.btnSeries)
        recyclerCategories = findViewById(R.id.recyclerCategories)
        recyclerContent = findViewById(R.id.recyclerContent)
        ivPreview = findViewById(R.id.ivPreview)
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle)
        tvPreviewDescription = findViewById(R.id.tvPreviewDescription)
        btnPlay = findViewById(R.id.btnPlay)
        btnFavorite = findViewById(R.id.btnFavorite)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupRecyclerViews() {
        // Categories RecyclerView
        categoryAdapter = CategoryAdapter { category ->
            viewModel.selectCategory(category)
        }
        recyclerCategories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = categoryAdapter
        }
        
        // Content RecyclerView
        channelAdapter = ChannelAdapter(
            onItemClick = { channel ->
                selectedChannel = channel
                updatePreview(channel)
            },
            onItemLongClick = { channel ->
                // Handle reordering
                showToast("Long press detected: Reorder mode")
            }
        )
        recyclerContent.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = channelAdapter
        }
    }
    
    private fun setupListeners() {
        btnLiveTV.setOnClickListener {
            currentContentType = "live"
            viewModel.setContentType("live")
            viewModel.loadData()
            updateTabSelection()
        }
        
        btnMovies.setOnClickListener {
            currentContentType = "movie"
            viewModel.setContentType("movie")
            viewModel.loadData()
            updateTabSelection()
        }
        
        btnSeries.setOnClickListener {
            currentContentType = "series"
            viewModel.setContentType("series")
            viewModel.loadData()
            updateTabSelection()
        }
        
        btnPlay.setOnClickListener {
            selectedChannel?.let { channel ->
                playContent(channel.streamUrl, channel.name, channel.id)
            }
        }
        
        btnFavorite.setOnClickListener {
            selectedChannel?.let { channel ->
                viewModel.toggleChannelFavorite(channel.id, channel.isFavorite)
                showToast(
                    if (channel.isFavorite) 
                        getString(R.string.removed_from_favorites)
                    else 
                        getString(R.string.added_to_favorites)
                )
            }
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            viewModel.categories.collectLatest { categories ->
                val allCategory = Category("all", getString(R.string.all_channels), currentContentType)
                categoryAdapter.submitList(listOf(allCategory) + categories)
            }
        }
        
        lifecycleScope.launch {
            viewModel.channels.collectLatest { channels ->
                if (currentContentType == "live") {
                    channelAdapter.submitList(channels)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.movies.collectLatest { movies ->
                if (currentContentType == "movie") {
                    // Convert movies to a displayable format
                    val items = movies.map { movie ->
                        Channel(
                            id = movie.id,
                            name = movie.name,
                            streamUrl = movie.streamUrl,
                            streamType = "vod",
                            categoryId = movie.categoryId,
                            iconUrl = movie.iconUrl,
                            isFavorite = movie.isFavorite
                        )
                    }
                    channelAdapter.submitList(items)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.series.collectLatest { series ->
                if (currentContentType == "series") {
                    // Convert series to a displayable format
                    val items = series.map { s ->
                        Channel(
                            id = s.id,
                            name = s.name,
                            streamUrl = "", // Series don't have direct stream URLs
                            streamType = "series",
                            categoryId = s.categoryId,
                            iconUrl = s.iconUrl,
                            isFavorite = s.isFavorite
                        )
                    }
                    channelAdapter.submitList(items)
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (isLoading) {
                    progressBar.visible()
                } else {
                    progressBar.gone()
                }
            }
        }
    }
    
    private fun updateTabSelection() {
        // Reset all buttons
        btnLiveTV.alpha = 0.5f
        btnMovies.alpha = 0.5f
        btnSeries.alpha = 0.5f
        
        // Highlight selected
        when (currentContentType) {
            "live" -> btnLiveTV.alpha = 1.0f
            "movie" -> btnMovies.alpha = 1.0f
            "series" -> btnSeries.alpha = 1.0f
        }
    }
    
    private fun updatePreview(channel: Channel) {
        tvPreviewTitle.text = channel.name
        tvPreviewDescription.text = channel.categoryName.ifEmpty { "No description" }
        
        // Load image if available
        // TODO: Use Glide to load channel.iconUrl into ivPreview
        
        btnPlay.isEnabled = true
        btnFavorite.text = if (channel.isFavorite) {
            getString(R.string.remove_from_favorites)
        } else {
            getString(R.string.add_to_favorites)
        }
    }
    
    private fun playContent(streamUrl: String, title: String, id: String) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(Constants.EXTRA_STREAM_URL, streamUrl)
            putExtra(Constants.EXTRA_STREAM_TITLE, title)
            putExtra(Constants.EXTRA_STREAM_ID, id)
            putExtra(Constants.EXTRA_STREAM_TYPE, currentContentType)
        }
        startActivity(intent)
    }
    
    private fun createApiClient(serverUrl: String): XtreamCodesApi {
        val cleanUrl = serverUrl.removeSuffix("/")
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("$cleanUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(XtreamCodesApi::class.java)
    }
}
