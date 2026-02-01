package com.alaa.iptv.data.local.dao

import androidx.room.*
import com.alaa.iptv.data.local.entity.CategoryEntity

/**
 * DAO for Category operations
 */
@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories ORDER BY categoryName ASC")
    suspend fun getAllCategories(): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE categoryType = :type ORDER BY categoryName ASC")
    suspend fun getCategoriesByType(type: String): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
    
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
    
    @Query("DELETE FROM categories WHERE categoryType = :type")
    suspend fun deleteCategoriesByType(type: String)
}
