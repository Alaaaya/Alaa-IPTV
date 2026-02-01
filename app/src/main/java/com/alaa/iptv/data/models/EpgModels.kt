package com.alaa.iptv.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model for EPG (Electronic Program Guide) data
 */
@Parcelize
data class EpgProgram(
    val id: String,
    val channelId: String,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val category: String?,
    val icon: String?
) : Parcelable {
    
    /**
     * Check if program is currently airing
     */
    fun isLive(): Boolean {
        val now = System.currentTimeMillis()
        return now in startTime..endTime
    }
    
    /**
     * Check if program is upcoming
     */
    fun isUpcoming(): Boolean {
        return System.currentTimeMillis() < startTime
    }
    
    /**
     * Check if program has ended
     */
    fun isPast(): Boolean {
        return System.currentTimeMillis() > endTime
    }
    
    /**
     * Get program duration in milliseconds
     */
    fun getDuration(): Long {
        return endTime - startTime
    }
}
