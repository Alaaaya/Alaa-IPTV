package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.FavoriteEntity

/**
 * DAO for Favorites operations
 */
@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>
    
    @Query("SELECT * FROM favorites WHERE itemType = :itemType ORDER BY addedAt DESC")
    suspend fun getFavoritesByType(itemType: String): List<FavoriteEntity>
    
    @Query("SELECT * FROM favorites WHERE itemId = :itemId")
    suspend fun getFavoriteById(itemId: String): FavoriteEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId)")
    suspend fun isFavorite(itemId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE itemId = :itemId")
    suspend fun deleteFavoriteById(itemId: String)
    
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()
}
