package com.alaa.iptv.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URI
import java.util.concurrent.TimeUnit

data class ConnectionDiagnosticResult(
    val networkAvailable: Boolean,
    val serverReachable: Boolean,
    val summary: String
)

object ConnectionDiagnostics {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(7, TimeUnit.SECONDS)
        .build()

    suspend fun inspect(context: Context, serverUrl: String): ConnectionDiagnosticResult = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val online = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = manager.activeNetwork
            val capabilities = network?.let(manager::getNetworkCapabilities)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            manager.activeNetworkInfo?.isConnected == true
        }
        if (!online) return@withContext ConnectionDiagnosticResult(false, false, "لا يوجد اتصال إنترنت متاح للتطبيق.")
        val normalized = runCatching {
            val source = if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) serverUrl else "http://$serverUrl"
            URI(source).let { "${it.scheme}://${it.host}" }
        }.getOrNull()
        if (normalized.isNullOrBlank()) return@withContext ConnectionDiagnosticResult(true, false, "رابط الخادم غير صالح للفحص.")
        val reachable = runCatching {
            client.newCall(Request.Builder().url(normalized).head().build()).execute().use { response ->
                response.code in 200..499
            }
        }.getOrDefault(false)
        ConnectionDiagnosticResult(true, reachable, if (reachable) "الاتصال بالإنترنت والخادم متاح." else "الإنترنت متاح لكن الخادم لم يستجب خلال الفحص.")
    }
}
