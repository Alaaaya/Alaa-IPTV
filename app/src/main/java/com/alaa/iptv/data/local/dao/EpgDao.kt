package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.EpgProgramEntity

/**
 * DAO for EPG Program operations
 */
@Dao
interface EpgDao {
    
    /**
     * Get all EPG programs for a specific channel
     */
    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId ORDER BY startTime ASC")
    suspend fun getProgramsByChannel(channelId: String): List<EpgProgramEntity>
    
    /**
     * Get EPG programs for a channel within a time range
     */
    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelId = :channelId 
        AND endTime >= :startTime 
        AND startTime <= :endTime 
        ORDER BY startTime ASC
    """)
    suspend fun getProgramsByChannelAndTimeRange(
        channelId: String,
        startTime: Long,
        endTime: Long
    ): List<EpgProgramEntity>
    
    /**
     * Get currently airing program for a channel
     */
    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelId = :channelId 
        AND startTime <= :currentTime 
        AND endTime > :currentTime 
        LIMIT 1
    """)
    suspend fun getCurrentProgram(channelId: String, currentTime: Long): EpgProgramEntity?
    
    /**
     * Get upcoming programs for a channel
     */
    @Query("""
        SELECT * FROM epg_programs 
        WHERE channelId = :channelId 
        AND startTime > :currentTime 
        ORDER BY startTime ASC 
        LIMIT :limit
    """)
    suspend fun getUpcomingPrograms(
        channelId: String,
        currentTime: Long,
        limit: Int = 10
    ): List<EpgProgramEntity>
    
    /**
     * Get EPG programs across all channels within a time range
     */
    @Query("""
        SELECT * FROM epg_programs 
        WHERE endTime >= :startTime 
        AND startTime <= :endTime 
        ORDER BY channelId, startTime ASC
    """)
    suspend fun getProgramsByTimeRange(
        startTime: Long,
        endTime: Long
    ): List<EpgProgramEntity>
    
    /**
     * Insert a single EPG program
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: EpgProgramEntity)
    
    /**
     * Insert multiple EPG programs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)
    
    /**
     * Update an EPG program
     */
    @Update
    suspend fun updateProgram(program: EpgProgramEntity)
    
    /**
     * Delete an EPG program
     */
    @Delete
    suspend fun deleteProgram(program: EpgProgramEntity)
    
    /**
     * Delete all EPG programs for a channel
     */
    @Query("DELETE FROM epg_programs WHERE channelId = :channelId")
    suspend fun deleteProgramsByChannel(channelId: String)
    
    /**
     * Delete old EPG programs (cleanup)
     */
    @Query("DELETE FROM epg_programs WHERE endTime < :cutoffTime")
    suspend fun deleteOldPrograms(cutoffTime: Long)
    
    /**
     * Delete all EPG programs
     */
    @Query("DELETE FROM epg_programs")
    suspend fun deleteAllPrograms()
    
    /**
     * Get count of EPG programs for a channel
     */
    @Query("SELECT COUNT(*) FROM epg_programs WHERE channelId = :channelId")
    suspend fun getProgramCountByChannel(channelId: String): Int
}
