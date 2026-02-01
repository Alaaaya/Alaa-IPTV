package com.alaaaya.iptv.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.data.models.PlaybackHistory
import com.alaaaya.iptv.data.models.User

@Database(
    entities = [User::class, Channel::class, PlaybackHistory::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun channelDao(): ChannelDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alaa_iptv_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
