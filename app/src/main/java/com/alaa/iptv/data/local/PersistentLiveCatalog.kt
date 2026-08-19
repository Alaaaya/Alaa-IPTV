package com.alaa.iptv.data.local

import android.content.Context
import com.alaa.iptv.data.local.entity.CatalogCategoryEntity
import com.alaa.iptv.data.local.entity.CatalogChannelEntity
import com.alaa.iptv.data.local.entity.CatalogSyncStateEntity
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.data.models.PagedContent
import com.alaa.iptv.data.repository.ContentPagingPolicy

/**
 * فهرس Live دائم لكل اشتراك. لا يستقبل إلا بصمة المصدر، لذلك لا يخزن بيانات الاعتماد الخام.
 */
class PersistentLiveCatalog(context: Context, private val accountKey: String) {
    private companion object {
        const val MAX_STALE_CATALOG_AGE_MS = 30L * 24L * 60L * 60L * 1000L
    }

    private val dao = AppDatabase.getInstance(context.applicationContext).persistentCatalogDao()

    suspend fun categories(): List<Category> = dao.categories(accountKey).map {
        Category(categoryId = it.categoryId, categoryName = it.categoryName, parentId = 0)
    }

    suspend fun page(categoryId: String, page: Int, pageSize: Int): PagedContent<Channel>? {
        val total = dao.channelCount(accountKey, categoryId)
        if (total == 0) return null
        val bounds = ContentPagingPolicy.bounds(total, page.coerceAtLeast(0), pageSize)
        val channels = dao.channels(accountKey, categoryId, pageSize, bounds.startIndex).map {
            Channel(
                streamId = it.streamId,
                num = it.num,
                name = it.name,
                streamType = "live",
                streamIcon = it.streamIcon,
                epgChannelId = null,
                added = null,
                categoryId = it.categoryId,
                categoryName = it.categoryName,
                customSid = null,
                directSource = it.directSource,
                position = it.position
            )
        }
        return PagedContent(channels, total, bounds.hasMore)
    }

    suspend fun replaceCategories(items: List<Category>) {
        val now = System.currentTimeMillis()
        dao.upsertCategories(items.mapIndexed { index, category ->
            CatalogCategoryEntity(accountKey, category.categoryId, category.categoryName, index, now)
        })
        dao.upsertSyncState(CatalogSyncStateEntity(accountKey, now, null))
        pruneStaleAccounts(now)
    }

    suspend fun replaceCategoryChannels(categoryId: String, channels: List<Channel>) {
        val now = System.currentTimeMillis()
        dao.clearChannelsInCategory(accountKey, categoryId)
        dao.upsertChannels(channels.mapIndexed { index, channel ->
            CatalogChannelEntity(
                accountKey = accountKey,
                streamId = channel.streamId,
                name = channel.name,
                categoryId = categoryId,
                categoryName = channel.categoryName,
                num = channel.num,
                streamIcon = channel.streamIcon,
                directSource = channel.directSource,
                position = index,
                updatedAt = now
            )
        })
        dao.upsertSyncState(CatalogSyncStateEntity(accountKey, now, null))
        pruneStaleAccounts(now)
    }

    suspend fun lastSuccessfulSyncAt(): Long? = dao.syncState(accountKey)?.lastSuccessfulSyncAt

    private suspend fun pruneStaleAccounts(now: Long) {
        val cutoff = now - MAX_STALE_CATALOG_AGE_MS
        dao.clearStaleAccountChannels(cutoff)
        dao.clearStaleAccountCategories(cutoff)
        dao.clearStaleSyncStates(cutoff)
    }
}
