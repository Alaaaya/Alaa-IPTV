package com.alaaaya.iptv.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.data.repository.ChannelRepository
import kotlinx.coroutines.launch

class PlayerViewModel(private val channelRepository: ChannelRepository) : ViewModel() {
    
    private val _currentChannel = MutableLiveData<Channel?>()
    val currentChannel: LiveData<Channel?> = _currentChannel
    
    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState
    
    fun loadChannel(channelId: String) {
        viewModelScope.launch {
            val channel = channelRepository.getChannelById(channelId)
            _currentChannel.value = channel
            
            if (channel != null) {
                _playerState.value = PlayerState.Ready(channel)
            } else {
                _playerState.value = PlayerState.Error("Channel not found")
            }
        }
    }
    
    fun onPlaybackError(error: String) {
        _playerState.value = PlayerState.Error(error)
    }
    
    fun onBuffering() {
        _playerState.value = PlayerState.Buffering
    }
    
    fun onPlaying() {
        _playerState.value = PlayerState.Playing
    }
    
    sealed class PlayerState {
        object Idle : PlayerState()
        object Buffering : PlayerState()
        object Playing : PlayerState()
        data class Ready(val channel: Channel) : PlayerState()
        data class Error(val message: String) : PlayerState()
    }
}
