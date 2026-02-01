package com.alaaaya.iptv.data.local

import androidx.room.*
import com.alaaaya.iptv.data.models.PlaybackHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY lastPlayedAt DESC")
    fun getAllHistory(): Flow<List<PlaybackHistory>>
    
    @Query("SELECT * FROM playback_history WHERE channelId = :channelId ORDER BY lastPlayedAt DESC LIMIT 1")
    suspend fun getHistoryByChannelId(channelId: String): PlaybackHistory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaybackHistory)
    
    @Update
    suspend fun updateHistory(history: PlaybackHistory)
    
    @Delete
    suspend fun deleteHistory(history: PlaybackHistory)
    
    @Query("DELETE FROM playback_history")
    suspend fun deleteAllHistory()
    
    @Query("DELETE FROM playback_history WHERE lastPlayedAt < :timestamp")
    suspend fun deleteOldHistory(timestamp: Long)
}
