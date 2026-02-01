package com.alaaaya.iptv.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.data.repository.ChannelRepository
import com.alaaaya.iptv.data.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainViewModel(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    val channels: LiveData<List<Channel>> = channelRepository.getAllChannels().asLiveData()
    
    private val _loadingState = MutableLiveData<LoadingState>()
    val loadingState: LiveData<LoadingState> = _loadingState
    
    fun loadChannels() {
        viewModelScope.launch {
            _loadingState.value = LoadingState.Loading
            
            // Get current user
            val user = userRepository.getCurrentUser().firstOrNull()
            
            if (user != null) {
                // Fetch channels from API
                val result = channelRepository.fetchLiveStreams(user.username, user.password)
                
                if (result.isSuccess) {
                    _loadingState.value = LoadingState.Success
                } else {
                    _loadingState.value = LoadingState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load channels"
                    )
                }
            } else {
                _loadingState.value = LoadingState.Error("User not logged in")
            }
        }
    }
    
    fun toggleFavorite(channelId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            channelRepository.toggleFavorite(channelId, !isFavorite)
        }
    }
    
    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        object Success : LoadingState()
        data class Error(val message: String) : LoadingState()
    }
}
