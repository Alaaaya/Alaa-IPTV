package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.RecentEntity

/**
 * DAO for Recent views operations
 */
@Dao
interface RecentDao {
    
    @Query("SELECT * FROM recents ORDER BY viewedAt DESC LIMIT 50")
    suspend fun getAllRecents(): List<RecentEntity>
    
    @Query("SELECT * FROM recents WHERE itemType = :itemType ORDER BY viewedAt DESC LIMIT 50")
    suspend fun getRecentsByType(itemType: String): List<RecentEntity>
    
    @Query("SELECT * FROM recents WHERE itemId = :itemId")
    suspend fun getRecentById(itemId: String): RecentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(recent: RecentEntity)
    
    @Delete
    suspend fun deleteRecent(recent: RecentEntity)
    
    @Query("DELETE FROM recents WHERE itemId = :itemId")
    suspend fun deleteRecentById(itemId: String)
    
    @Query("DELETE FROM recents")
    suspend fun deleteAllRecents()
    
    @Query("DELETE FROM recents WHERE viewedAt < :cutoffTime")
    suspend fun deleteOldRecents(cutoffTime: Long)
}
