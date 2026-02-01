package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.SeriesEntity
import com.alaa.iptv.data.local.entity.EpisodeEntity

/**
 * DAO for Series operations
 */
@Dao
interface SeriesDao {
    
    @Query("SELECT * FROM series ORDER BY name ASC")
    suspend fun getAllSeries(): List<SeriesEntity>
    
    @Query("SELECT * FROM series WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getSeriesByCategory(categoryId: String): List<SeriesEntity>
    
    @Query("SELECT * FROM series WHERE seriesId = :seriesId")
    suspend fun getSeriesById(seriesId: String): SeriesEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleSeries(series: List<SeriesEntity>)
    
    @Update
    suspend fun updateSeries(series: SeriesEntity)
    
    @Delete
    suspend fun deleteSeries(series: SeriesEntity)
    
    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()
    
    @Query("DELETE FROM series WHERE categoryId = :categoryId")
    suspend fun deleteSeriesByCategory(categoryId: String)
    
    // ==================== Search ====================
    
    /**
     * Search series by title
     */
    @Query("""
        SELECT * FROM series 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """)
    suspend fun searchSeriesByTitle(query: String): List<SeriesEntity>
    
    /**
     * Search series by title within a category
     */
    @Query("""
        SELECT * FROM series 
        WHERE name LIKE '%' || :query || '%' 
        AND categoryId = :categoryId 
        ORDER BY name ASC
    """)
    suspend fun searchSeriesByTitleInCategory(query: String, categoryId: String): List<SeriesEntity>
    
    /**
     * Search series by genre
     */
    @Query("""
        SELECT * FROM series 
        WHERE genre LIKE '%' || :genre || '%' 
        ORDER BY name ASC
    """)
    suspend fun searchSeriesByGenre(genre: String): List<SeriesEntity>
}

/**
 * DAO for Episode operations
 */
@Dao
interface EpisodeDao {
    
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNum ASC")
    suspend fun getEpisodesBySeriesId(seriesId: String): List<EpisodeEntity>
    
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId AND seasonNumber = :seasonNumber ORDER BY episodeNum ASC")
    suspend fun getEpisodesBySeasonNumber(seriesId: String, seasonNumber: Int): List<EpisodeEntity>
    
    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: String): EpisodeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)
    
    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)
    
    @Delete
    suspend fun deleteEpisode(episode: EpisodeEntity)
    
    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesBySeriesId(seriesId: String)
    
    @Query("DELETE FROM episodes")
    suspend fun deleteAllEpisodes()
}
