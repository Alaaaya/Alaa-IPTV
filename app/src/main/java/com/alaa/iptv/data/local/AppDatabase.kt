package com.alaa.iptv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alaa.iptv.data.local.dao.*
import com.alaa.iptv.data.local.entity.*

/**
 * Room database for IPTV app
 * Version 1: Initial schema with all entities
 */
@Database(
    entities = [
        ChannelEntity::class,
        CategoryEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        EpisodeEntity::class,
        FavoriteEntity::class,
        RecentEntity::class
    ],
    version = 1,
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
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "alaa_iptv_database"
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development, remove in production
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        // Future migrations will be added here
        // Example:
        // val MIGRATION_1_2 = object : Migration(1, 2) {
        //     override fun migrate(database: SupportSQLiteDatabase) {
        //         // Migration code
        //     }
        // }
    }
}
