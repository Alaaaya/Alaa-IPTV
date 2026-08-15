package com.alaa.iptv.data.remote

import com.alaa.iptv.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ProvisionedIptvSubscription(
    val tvId: String,
    val serverUrl: String,
    val username: String,
    val password: String
)

object TvProvisioningClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchSubscription(tvId: String, notifyOwner: Boolean = false): Result<ProvisionedIptvSubscription> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(tvId.length >= 16) { "يرجى إدخال TV ID صحيح" }

                val payload = JSONObject()
                    .put("json", JSONObject().put("tvId", tvId).put("notifyOwner", notifyOwner))
                    .toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val endpoint = "${BuildConfig.PROVISIONING_API_URL.trimEnd('/')}/devices.provision"
                val request = Request.Builder()
                    .url(endpoint)
                    .post(payload)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        throw IOException("تعذر جلب بيانات الجهاز. تحقق من TV ID أو حالة التفعيل")
                    }

                    val root = JSONObject(body)
                    val result = root.optJSONObject("result")
                        ?: throw IOException("استجابة لوحة التحكم غير صالحة")
                    val data = result.optJSONObject("data")?.optJSONObject("json")
                        ?: throw IOException("بيانات الاشتراك غير متاحة")
                    val subscription = data.optJSONObject("subscription")
                        ?: throw IOException("بيانات الاشتراك غير متاحة")

                    ProvisionedIptvSubscription(
                        tvId = data.optString("tvId"),
                        serverUrl = subscription.optString("serverUrl"),
                        username = subscription.optString("username"),
                        password = subscription.optString("password")
                    ).also {
                        require(it.serverUrl.isNotBlank() && it.username.isNotBlank() && it.password.isNotBlank()) {
                            "بيانات الاشتراك المستلمة غير مكتملة"
                        }
                    }
                }
            }
        }
}
