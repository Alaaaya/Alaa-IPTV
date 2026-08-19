package com.alaa.iptv.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alaa.iptv.data.local.entity.CatalogCategoryEntity
import com.alaa.iptv.data.local.entity.CatalogChannelEntity
import com.alaa.iptv.data.local.entity.CatalogSyncStateEntity

@Dao
interface PersistentCatalogDao {
    @Query("SELECT * FROM catalog_categories WHERE accountKey = :accountKey ORDER BY position, categoryName")
    suspend fun categories(accountKey: String): List<CatalogCategoryEntity>

    @Query("SELECT * FROM catalog_channels WHERE accountKey = :accountKey AND categoryId = :categoryId ORDER BY position, num, name LIMIT :limit OFFSET :offset")
    suspend fun channels(accountKey: String, categoryId: String, limit: Int, offset: Int): List<CatalogChannelEntity>

    @Query("SELECT COUNT(*) FROM catalog_channels WHERE accountKey = :accountKey AND categoryId = :categoryId")
    suspend fun channelCount(accountKey: String, categoryId: String): Int

    @Query("SELECT * FROM catalog_sync_state WHERE accountKey = :accountKey LIMIT 1")
    suspend fun syncState(accountKey: String): CatalogSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(items: List<CatalogCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChannels(items: List<CatalogChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(state: CatalogSyncStateEntity)

    @Query("DELETE FROM catalog_channels WHERE accountKey = :accountKey")
    suspend fun clearChannels(accountKey: String)

    @Query("DELETE FROM catalog_channels WHERE accountKey = :accountKey AND categoryId = :categoryId")
    suspend fun clearChannelsInCategory(accountKey: String, categoryId: String)

    @Query("DELETE FROM catalog_categories WHERE accountKey = :accountKey")
    suspend fun clearCategories(accountKey: String)

    @Query("DELETE FROM catalog_sync_state WHERE accountKey = :accountKey")
    suspend fun clearSyncState(accountKey: String)

    @Query("DELETE FROM catalog_channels WHERE accountKey IN (SELECT accountKey FROM catalog_sync_state WHERE lastSuccessfulSyncAt < :cutoff)")
    suspend fun clearStaleAccountChannels(cutoff: Long)

    @Query("DELETE FROM catalog_categories WHERE accountKey IN (SELECT accountKey FROM catalog_sync_state WHERE lastSuccessfulSyncAt < :cutoff)")
    suspend fun clearStaleAccountCategories(cutoff: Long)

    @Query("DELETE FROM catalog_sync_state WHERE lastSuccessfulSyncAt < :cutoff")
    suspend fun clearStaleSyncStates(cutoff: Long)
}
