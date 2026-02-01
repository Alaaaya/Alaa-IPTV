package com.alaaaya.iptv.data.repository

import com.alaaaya.iptv.data.local.UserDao
import com.alaaaya.iptv.data.models.User
import com.alaaaya.iptv.data.remote.IptvApiService
import com.alaaaya.iptv.data.remote.LoginRequest
import com.alaaaya.iptv.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getCurrentUser(): Flow<User?> = userDao.getCurrentUser()
    
    suspend fun login(username: String, password: String, serverUrl: String): Result<User> {
        return try {
            // Set the base URL for API calls
            RetrofitClient.setBaseUrl(serverUrl)
            val apiService = RetrofitClient.getApiService()
            
            // Attempt login
            val response = apiService.login(username, password)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Check if login was successful
                if (loginResponse.user_info?.auth == 1 || loginResponse.user_info?.status == "Active") {
                    val user = User(
                        username = username,
                        password = password,
                        serverUrl = serverUrl,
                        token = null,
                        expiryDate = loginResponse.user_info.exp_date?.toLongOrNull()
                    )
                    
                    // Delete old users and insert new one
                    userDao.deleteAllUsers()
                    userDao.insertUser(user)
                    
                    Result.success(user)
                } else {
                    Result.failure(Exception("Authentication failed: ${loginResponse.user_info?.message ?: "Unknown error"}"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        userDao.deleteAllUsers()
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}
