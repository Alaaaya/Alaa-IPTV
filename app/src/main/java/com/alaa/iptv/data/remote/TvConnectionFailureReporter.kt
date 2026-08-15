package com.alaa.iptv.data.remote

import com.alaa.iptv.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** يرسل بلاغاً خالياً من كلمات المرور بعد تجاوز عتبة الفشل داخل التطبيق. */
object TvConnectionFailureReporter {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun report(tvId: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().put("json", JSONObject()
                .put("tvId", tvId)
                .put("reason", reason.take(120))
            ).toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("${BuildConfig.PROVISIONING_API_URL.trimEnd('/')}/devices.connectionFailure")
                .post(payload)
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }
}
