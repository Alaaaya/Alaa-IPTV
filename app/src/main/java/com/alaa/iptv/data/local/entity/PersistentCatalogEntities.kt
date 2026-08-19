package com.alaa.iptv.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * فهرس تشغيل محلي؛ accountKey بصمة مصدر وليست اسم مستخدم أو كلمة مرور.
 */
@Entity(
    tableName = "catalog_channels",
    primaryKeys = ["accountKey", "streamId"],
    indices = [Index(value = ["accountKey", "categoryId"])]
)
data class CatalogChannelEntity(
    val accountKey: String,
    val streamId: String,
    val name: String,
    val categoryId: String,
    val categoryName: String?,
    val num: String,
    val streamIcon: String?,
    val directSource: String?,
    val position: Int,
    val updatedAt: Long
)

@Entity(
    tableName = "catalog_categories",
    primaryKeys = ["accountKey", "categoryId"],
    indices = [Index(value = ["accountKey"])]
)
data class CatalogCategoryEntity(
    val accountKey: String,
    val categoryId: String,
    val categoryName: String,
    val position: Int,
    val updatedAt: Long
)

@Entity(tableName = "catalog_sync_state")
data class CatalogSyncStateEntity(
    @PrimaryKey
    val accountKey: String,
    val lastSuccessfulSyncAt: Long,
    val sourceVersion: String?
)
