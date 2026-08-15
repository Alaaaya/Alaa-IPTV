package com.alaa.iptv.data.remote

import android.os.Build
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

    suspend fun syncControlPlane(tvId: String, appVersion: String): Result<DeviceControlPlaneSnapshot> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(tvId.length >= 16) { "يرجى إدخال TV ID صحيح" }
                val deviceType = "${Build.MANUFACTURER} ${Build.MODEL}".trim().take(80)
                val payload = JSONObject()
                    .put("json", JSONObject().put("tvId", tvId).put("appVersion", appVersion).put("deviceType", deviceType))
                    .toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                val endpoint = "${BuildConfig.PROVISIONING_API_URL.trimEnd('/')}/devices.sync"
                val request = Request.Builder()
                    .url(endpoint)
                    .post(payload)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw IOException("تعذر مزامنة حالة الجهاز")
                    val data = JSONObject(body).optJSONObject("result")
                        ?.optJSONObject("data")?.optJSONObject("json")
                        ?: throw IOException("استجابة لوحة التحكم غير صالحة")
                    parseControlPlaneSnapshot(data, tvId)
                }
            }
        }

    suspend fun acknowledgeRemoteLogout(tvId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().put("json", JSONObject().put("tvId", tvId)).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("${BuildConfig.PROVISIONING_API_URL.trimEnd('/')}/devices.acknowledgeRemoteLogout")
                .post(payload)
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("تعذر تأكيد الخروج البعيد")
            }
        }
    }

    internal fun parseControlPlaneSnapshot(data: JSONObject, fallbackTvId: String): DeviceControlPlaneSnapshot {
        val device = data.optJSONObject("device") ?: throw IOException("حالة الجهاز غير متاحة")
        return buildControlPlaneSnapshot(
            tvId = device.optString("tvId", fallbackTvId),
            deviceStatus = device.optString("status", "unknown"),
            remoteLogoutRequested = device.optBoolean("remoteLogoutRequested", false),
            remoteConfig = parseRemoteConfig(data.optJSONObject("remoteConfig")),
            featureFlags = parseFeatureFlags(data.optJSONObject("featureFlags"))
        )
    }

    internal fun buildControlPlaneSnapshot(
        tvId: String,
        deviceStatus: String,
        remoteLogoutRequested: Boolean,
        remoteConfig: Map<String, RemoteConfigValue>,
        featureFlags: Map<String, RemoteFeatureFlag>
    ): DeviceControlPlaneSnapshot = DeviceControlPlaneSnapshot(
        tvId = tvId,
        deviceStatus = deviceStatus,
        remoteLogoutRequested = remoteLogoutRequested,
        remoteConfig = remoteConfig,
        featureFlags = featureFlags
    )

    internal fun parseRemoteConfig(source: JSONObject?): Map<String, RemoteConfigValue> {
        if (source == null) return emptyMap()
        return buildMap {
            val keys = source.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = source.optJSONObject(key) ?: continue
                put(key, RemoteConfigValue(item.optString("value"), item.optString("type", "string")))
            }
        }
    }

    internal fun parseFeatureFlags(source: JSONObject?): Map<String, RemoteFeatureFlag> {
        if (source == null) return emptyMap()
        return buildMap {
            val keys = source.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = source.optJSONObject(key) ?: continue
                put(key, RemoteFeatureFlag(item.optBoolean("enabled", false), item.optInt("rolloutPercent", 100).coerceIn(0, 100)))
            }
        }
    }
}
