package com.alaaaya.iptv.data.db

import androidx.room.*
import com.alaaaya.iptv.data.models.Series
import com.alaaaya.iptv.data.models.Episode
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {
    @Query("SELECT * FROM series ORDER BY customOrder ASC, name ASC")
    fun getAllSeries(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE categoryId = :categoryId ORDER BY customOrder ASC, name ASC")
    fun getSeriesByCategory(categoryId: String): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE isFavorite = 1 ORDER BY customOrder ASC, name ASC")
    fun getFavoriteSeries(): Flow<List<Series>>

    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: String): Series?

    @Query("SELECT * FROM series WHERE name LIKE '%' || :query || '%'")
    fun searchSeries(query: String): Flow<List<Series>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: Series)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleSeries(series: List<Series>)

    @Update
    suspend fun updateSeries(series: Series)

    @Delete
    suspend fun deleteSeries(series: Series)

    @Query("DELETE FROM series")
    suspend fun deleteAllSeries()

    @Query("UPDATE series SET isFavorite = :isFavorite WHERE id = :seriesId")
    suspend fun updateFavoriteStatus(seriesId: String, isFavorite: Boolean)

    @Query("UPDATE series SET customOrder = :order WHERE id = :seriesId")
    suspend fun updateSeriesOrder(seriesId: String, order: Int)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY season ASC, episodeNum ASC")
    fun getEpisodesBySeries(seriesId: String): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisodeById(episodeId: String): Episode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: Episode)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Delete
    suspend fun deleteEpisode(episode: Episode)

    @Query("DELETE FROM episodes WHERE seriesId = :seriesId")
    suspend fun deleteEpisodesBySeries(seriesId: String)
}
