package com.alaaaya.iptv.data.db

import androidx.room.*
import com.alaaaya.iptv.data.models.WatchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<WatchHistory>>

    @Query("SELECT * FROM watch_history WHERE contentId = :contentId AND contentType = :contentType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getHistoryForContent(contentId: String, contentType: String): WatchHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WatchHistory)

    @Query("DELETE FROM watch_history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM watch_history WHERE contentId = :contentId AND contentType = :contentType")
    suspend fun deleteHistoryForContent(contentId: String, contentType: String)
}
