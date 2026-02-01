package com.alaaaya.iptv.data.db

import androidx.room.*
import com.alaaaya.iptv.data.models.UserCredentials

@Dao
interface UserCredentialsDao {
    @Query("SELECT * FROM user_credentials ORDER BY lastLogin DESC LIMIT 1")
    suspend fun getLastCredentials(): UserCredentials?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredentials(credentials: UserCredentials)

    @Query("DELETE FROM user_credentials")
    suspend fun deleteAllCredentials()
}
