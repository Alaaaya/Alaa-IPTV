package com.alaaaya.iptv.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

// Extension functions for easier usage

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })
}

// Network result wrapper
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Flow error handling
fun <T> Flow<T>.asResult(): Flow<Result<T>> = flow {
    emit(Result.Loading)
    this@asResult.collect { value ->
        emit(Result.Success(value))
    }
}.catch { e ->
    emit(Result.Error(e))
}

// Build stream URLs
object StreamUrlBuilder {
    fun buildLiveUrl(serverUrl: String, username: String, password: String, streamId: String, extension: String = "ts"): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return "$cleanUrl/live/$username/$password/$streamId.$extension"
    }

    fun buildVodUrl(serverUrl: String, username: String, password: String, streamId: String, extension: String = "mp4"): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return "$cleanUrl/movie/$username/$password/$streamId.$extension"
    }

    fun buildSeriesUrl(serverUrl: String, username: String, password: String, episodeId: String, extension: String = "mp4"): String {
        val cleanUrl = serverUrl.removeSuffix("/")
        return "$cleanUrl/series/$username/$password/$episodeId.$extension"
    }
}

// Format time durations
object TimeFormatter {
    fun formatDuration(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            hours > 0 -> String.format("%dh %dm", hours, minutes)
            minutes > 0 -> String.format("%dm", minutes)
            else -> "< 1m"
        }
    }

    fun formatMillis(millis: Long): String {
        val seconds = (millis / 1000).toInt()
        return formatDuration(seconds)
    }
}
