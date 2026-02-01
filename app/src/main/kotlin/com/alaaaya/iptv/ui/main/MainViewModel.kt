package com.alaaaya.iptv.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alaaaya.iptv.data.IptvRepository
import com.alaaaya.iptv.data.models.Category
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.data.models.Movie
import com.alaaaya.iptv.data.models.Series
import com.alaaaya.iptv.utils.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: IptvRepository) : ViewModel() {
    
    private val _contentType = MutableStateFlow("live")
    val contentType: StateFlow<String> = _contentType
    
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    // Categories
    val categories: StateFlow<List<Category>> = _contentType.flatMapLatest { type ->
        repository.getCategoriesByType(type)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Channels
    val channels: StateFlow<List<Channel>> = combine(
        _contentType,
        _selectedCategory
    ) { type, category ->
        if (type == "live") {
            category?.id ?: "all"
        } else {
            null
        }
    }.flatMapLatest { categoryId ->
        if (categoryId != null) {
            if (categoryId == "all") {
                repository.getAllChannels()
            } else {
                repository.getChannelsByCategory(categoryId)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Movies
    val movies: StateFlow<List<Movie>> = combine(
        _contentType,
        _selectedCategory
    ) { type, category ->
        if (type == "movie") {
            category?.id ?: "all"
        } else {
            null
        }
    }.flatMapLatest { categoryId ->
        if (categoryId != null) {
            if (categoryId == "all") {
                repository.getAllMovies()
            } else {
                repository.getMoviesByCategory(categoryId)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Series
    val series: StateFlow<List<Series>> = combine(
        _contentType,
        _selectedCategory
    ) { type, category ->
        if (type == "series") {
            category?.id ?: "all"
        } else {
            null
        }
    }.flatMapLatest { categoryId ->
        if (categoryId != null) {
            if (categoryId == "all") {
                repository.getAllSeries()
            } else {
                repository.getSeriesByCategory(categoryId)
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun setContentType(type: String) {
        _contentType.value = type
        _selectedCategory.value = null
    }
    
    fun selectCategory(category: Category?) {
        _selectedCategory.value = category
    }
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                when (_contentType.value) {
                    "live" -> {
                        when (val result = repository.fetchAndStoreLiveChannels()) {
                            is Result.Success -> {
                                // Data loaded successfully
                            }
                            is Result.Error -> {
                                _error.value = result.exception.message
                            }
                            is Result.Loading -> {
                                // Already loading
                            }
                        }
                    }
                    "movie" -> {
                        when (val result = repository.fetchAndStoreMovies()) {
                            is Result.Success -> {
                                // Data loaded successfully
                            }
                            is Result.Error -> {
                                _error.value = result.exception.message
                            }
                            is Result.Loading -> {
                                // Already loading
                            }
                        }
                    }
                    "series" -> {
                        when (val result = repository.fetchAndStoreSeries()) {
                            is Result.Success -> {
                                // Data loaded successfully
                            }
                            is Result.Error -> {
                                _error.value = result.exception.message
                            }
                            is Result.Loading -> {
                                // Already loading
                            }
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleChannelFavorite(channelId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleChannelFavorite(channelId, !isFavorite)
        }
    }
    
    fun toggleMovieFavorite(movieId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleMovieFavorite(movieId, !isFavorite)
        }
    }
    
    fun toggleSeriesFavorite(seriesId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleSeriesFavorite(seriesId, !isFavorite)
        }
    }
}
