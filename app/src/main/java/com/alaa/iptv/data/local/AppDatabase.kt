package com.alaa.iptv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alaa.iptv.data.local.dao.*
import com.alaa.iptv.data.local.entity.*

@Database(
    entities = [
        ChannelEntity::class,
        CategoryEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        FavoriteEntity::class,
        RecentEntity::class,
        EpgProgramEntity::class,
        CatalogChannelEntity::class,
        CatalogCategoryEntity::class,
        CatalogSyncStateEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun categoryDao(): CategoryDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao
    abstract fun epgDao(): EpgDao
    abstract fun persistentCatalogDao(): PersistentCatalogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "alaa_iptv_database"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `epg_programs` (`id` TEXT NOT NULL, `channelId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `category` TEXT, `icon` TEXT, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_epg_programs_channelId` ON `epg_programs` (`channelId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_epg_programs_startTime` ON `epg_programs` (`startTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_epg_programs_endTime` ON `epg_programs` (`endTime`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `catalog_channels` (`accountKey` TEXT NOT NULL, `streamId` TEXT NOT NULL, `name` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `categoryName` TEXT, `num` TEXT NOT NULL, `streamIcon` TEXT, `directSource` TEXT, `position` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`accountKey`, `streamId`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_catalog_channels_accountKey_categoryId` ON `catalog_channels` (`accountKey`, `categoryId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `catalog_categories` (`accountKey` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `categoryName` TEXT NOT NULL, `position` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`accountKey`, `categoryId`))")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_catalog_categories_accountKey` ON `catalog_categories` (`accountKey`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `catalog_sync_state` (`accountKey` TEXT NOT NULL, `lastSuccessfulSyncAt` INTEGER NOT NULL, `sourceVersion` TEXT, PRIMARY KEY(`accountKey`))")
            }
        }

        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
        }
    }
}
