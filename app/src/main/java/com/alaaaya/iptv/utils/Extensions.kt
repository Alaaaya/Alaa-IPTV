package com.alaaaya.iptv.utils

import android.content.Context
import android.widget.Toast

// Extension function for showing toast messages
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// Extension function for converting timestamp to readable date
fun Long.toDateString(): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}

// Extension function to check if string is a valid URL
fun String.isValidUrl(): Boolean {
    return try {
        val url = java.net.URL(this)
        url.protocol == "http" || url.protocol == "https"
    } catch (e: Exception) {
        false
    }
}

// Extension function to build stream URL
fun buildStreamUrl(serverUrl: String, username: String, password: String, streamId: String, extension: String = "m3u8"): String {
    val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
    return "${baseUrl}live/$username/$password/$streamId.$extension"
}

// Extension function to build VOD URL
fun buildVodUrl(serverUrl: String, username: String, password: String, streamId: String, extension: String = "mp4"): String {
    val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
    return "${baseUrl}movie/$username/$password/$streamId.$extension"
}

// Extension function to build series URL
fun buildSeriesUrl(serverUrl: String, username: String, password: String, streamId: String, extension: String = "mp4"): String {
    val baseUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
    return "${baseUrl}series/$username/$password/$streamId.$extension"
}
