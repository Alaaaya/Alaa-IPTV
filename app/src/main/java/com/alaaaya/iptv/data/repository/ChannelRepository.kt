package com.alaaaya.iptv.data.repository

import com.alaaaya.iptv.data.local.ChannelDao
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.data.remote.IptvApiService
import com.alaaaya.iptv.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class ChannelRepository(private val channelDao: ChannelDao) {
    
    fun getAllChannels(): Flow<List<Channel>> = channelDao.getAllChannels()
    
    fun getChannelsByCategory(category: String): Flow<List<Channel>> = 
        channelDao.getChannelsByCategory(category)
    
    fun getFavoriteChannels(): Flow<List<Channel>> = channelDao.getFavoriteChannels()
    
    suspend fun getChannelById(channelId: String): Channel? = 
        channelDao.getChannelById(channelId)
    
    suspend fun fetchLiveStreams(username: String, password: String): Result<List<Channel>> {
        return try {
            val apiService = RetrofitClient.getApiService()
            val response = apiService.getLiveStreams(username, password)
            
            if (response.isSuccessful && response.body() != null) {
                val streams = response.body()!!
                val channels = streams.map { stream ->
                    Channel(
                        id = stream.stream_id?.toString() ?: "",
                        name = stream.name ?: "Unknown",
                        streamUrl = "", // URL will be constructed when playing
                        logoUrl = stream.stream_icon,
                        category = stream.category_id,
                        epgChannelId = stream.epg_channel_id
                    )
                }
                
                // Insert channels into database
                channelDao.insertChannels(channels)
                
                Result.success(channels)
            } else {
                Result.failure(Exception("Failed to fetch streams: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleFavorite(channelId: String, isFavorite: Boolean) {
        channelDao.updateFavoriteStatus(channelId, isFavorite)
    }
    
    suspend fun insertChannel(channel: Channel) {
        channelDao.insertChannel(channel)
    }
    
    suspend fun updateChannel(channel: Channel) {
        channelDao.updateChannel(channel)
    }
    
    suspend fun deleteAllChannels() {
        channelDao.deleteAllChannels()
    }
}
