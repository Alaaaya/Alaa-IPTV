package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.MovieEntity

/**
 * DAO for Movie operations
 */
@Dao
interface MovieDao {
    
    @Query("SELECT * FROM movies ORDER BY name ASC")
    suspend fun getAllMovies(): List<MovieEntity>
    
    @Query("SELECT * FROM movies WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getMoviesByCategory(categoryId: String): List<MovieEntity>
    
    @Query("SELECT * FROM movies WHERE streamId = :streamId")
    suspend fun getMovieById(streamId: String): MovieEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)
    
    @Update
    suspend fun updateMovie(movie: MovieEntity)
    
    @Delete
    suspend fun deleteMovie(movie: MovieEntity)
    
    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()
    
    @Query("DELETE FROM movies WHERE categoryId = :categoryId")
    suspend fun deleteMoviesByCategory(categoryId: String)
}
