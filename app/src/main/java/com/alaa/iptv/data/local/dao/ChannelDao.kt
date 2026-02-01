package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.ChannelEntity

/**
 * DAO for Channel operations
 */
@Dao
interface ChannelDao {
    
    @Query("SELECT * FROM channels ORDER BY position ASC, num ASC")
    suspend fun getAllChannels(): List<ChannelEntity>
    
    @Query("SELECT * FROM channels WHERE categoryId = :categoryId ORDER BY position ASC, num ASC")
    suspend fun getChannelsByCategory(categoryId: String): List<ChannelEntity>
    
    @Query("SELECT * FROM channels WHERE streamId = :streamId")
    suspend fun getChannelById(streamId: String): ChannelEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)
    
    @Update
    suspend fun updateChannel(channel: ChannelEntity)
    
    @Query("UPDATE channels SET position = :position WHERE streamId = :streamId")
    suspend fun updateChannelPosition(streamId: String, position: Int)
    
    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
    
    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
    
    @Query("DELETE FROM channels WHERE categoryId = :categoryId")
    suspend fun deleteChannelsByCategory(categoryId: String)
    
    // ==================== Search ====================
    
    /**
     * Search channels by name
     */
    @Query("""
        SELECT * FROM channels 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY position ASC, num ASC
    """)
    suspend fun searchChannelsByName(query: String): List<ChannelEntity>
    
    /**
     * Search channels by name within a category
     */
    @Query("""
        SELECT * FROM channels 
        WHERE name LIKE '%' || :query || '%' 
        AND categoryId = :categoryId 
        ORDER BY position ASC, num ASC
    """)
    suspend fun searchChannelsByNameInCategory(query: String, categoryId: String): List<ChannelEntity>
}
