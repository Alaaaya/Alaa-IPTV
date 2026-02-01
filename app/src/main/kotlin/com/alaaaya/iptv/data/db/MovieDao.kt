package com.alaaaya.iptv.data.db

import androidx.room.*
import com.alaaaya.iptv.data.models.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY customOrder ASC, name ASC")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE categoryId = :categoryId ORDER BY customOrder ASC, name ASC")
    fun getMoviesByCategory(categoryId: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY customOrder ASC, name ASC")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: String): Movie?

    @Query("SELECT * FROM movies WHERE name LIKE '%' || :query || '%'")
    fun searchMovies(query: String): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Delete
    suspend fun deleteMovie(movie: Movie)

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun updateFavoriteStatus(movieId: String, isFavorite: Boolean)

    @Query("UPDATE movies SET customOrder = :order WHERE id = :movieId")
    suspend fun updateMovieOrder(movieId: String, order: Int)
}
