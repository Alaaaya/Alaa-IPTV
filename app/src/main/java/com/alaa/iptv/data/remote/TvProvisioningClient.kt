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
    val sourceType: String,
    val serverUrl: String,
    val username: String,
    val password: String
)

data class QrIssuedSession(
    val token: String,
    val expiresAt: String
)

data class QrPairingStatus(
    val state: String,
    val expiresAt: String? = null
)

data class DevicePairingCode(
    val code: String,
    val expiresAt: String? = null,
    val alreadyRegistered: Boolean = false
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
                        sourceType = subscription.optString("sourceType", "xtream").lowercase(),
                        serverUrl = subscription.optString("serverUrl"),
                        username = subscription.optString("username"),
                        password = subscription.optString("password")
                    ).also {
                        require(it.sourceType == "xtream" || it.sourceType == "m3u") {
                            "نوع مصدر الاشتراك غير صالح"
                        }
                        require(it.serverUrl.isNotBlank() && (it.sourceType == "m3u" || (it.username.isNotBlank() && it.password.isNotBlank()))) {
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

    suspend fun issueDevicePairingCode(tvId: String): Result<DevicePairingCode> = withContext(Dispatchers.IO) {
        runCatching {
            require(tvId.length >= 16) { "تعذر تجهيز رمز الاقتران" }
            val data = postJson("devices.issuePairingCode", JSONObject().put("tvId", tvId))
            val code = data.optString("code").trim().uppercase()
            require(code.matches(Regex("ALAA-[0-9]{8}"))) { "استجابة رمز الاقتران غير صالحة" }
            DevicePairingCode(
                code = code,
                expiresAt = data.optString("expiresAt").ifBlank { null },
                alreadyRegistered = data.optBoolean("alreadyRegistered", false)
            )
        }
    }

    suspend fun issuePhonePairing(tvId: String): Result<QrIssuedSession> = withContext(Dispatchers.IO) {
        runCatching {
            require(tvId.length >= 16) { "يرجى إدخال TV ID صحيح" }
            val data = postJson("qr.issuePairing", JSONObject().put("tvId", tvId))
            val token = data.optString("token")
            require(token.length == 43) { "استجابة رمز QR غير صالحة" }
            QrIssuedSession(token, data.optString("expiresAt"))
        }
    }

    suspend fun getPhonePairingStatus(tvId: String, token: String): Result<QrPairingStatus> = withContext(Dispatchers.IO) {
        runCatching {
            val data = postJson("qr.pairingStatus", JSONObject().put("tvId", tvId).put("token", token))
            QrPairingStatus(data.optString("state", "expired"), data.optString("expiresAt").ifBlank { null })
        }
    }

    suspend fun confirmPhonePairing(tvId: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            postJson("qr.confirmPairing", JSONObject().put("tvId", tvId).put("token", token))
            Unit
        }
    }

    suspend fun createContentShare(
        tvId: String,
        contentType: String,
        contentKey: String,
        title: String,
        posterUrl: String? = null
    ): Result<QrIssuedSession> = withContext(Dispatchers.IO) {
        runCatching {
            val content = JSONObject()
                .put("contentType", contentType)
                .put("contentKey", contentKey)
                .put("title", title)
            posterUrl?.takeIf { it.isNotBlank() }?.let { content.put("posterUrl", it) }
            val data = postJson("qr.createContentShare", JSONObject().put("tvId", tvId).put("content", content))
            val token = data.optString("token")
            require(token.length == 43) { "استجابة رمز المشاركة غير صالحة" }
            QrIssuedSession(token, data.optString("expiresAt"))
        }
    }

    private fun postJson(procedure: String, payloadJson: JSONObject): JSONObject {
        val payload = JSONObject().put("json", payloadJson).toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("${BuildConfig.PROVISIONING_API_URL.trimEnd('/')}/$procedure")
            .post(payload)
            .header("Accept", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IOException("تعذر إتمام طلب رمز QR")
            return JSONObject(body).optJSONObject("result")?.optJSONObject("data")?.optJSONObject("json")
                ?: throw IOException("استجابة لوحة التحكم غير صالحة")
        }
    }

    internal fun parseControlPlaneSnapshot(data: JSONObject, fallbackTvId: String): DeviceControlPlaneSnapshot {
        val device = data.optJSONObject("device") ?: throw IOException("حالة الجهاز غير متاحة")
        return buildControlPlaneSnapshot(
            tvId = device.optString("tvId", fallbackTvId),
            deviceStatus = device.optString("status", "unknown"),
            remoteLogoutRequested = device.optBoolean("remoteLogoutRequested", false),
            updateChannel = device.optString("updateChannel", "stable"),
            remoteConfig = parseRemoteConfig(data.optJSONObject("remoteConfig")),
            featureFlags = parseFeatureFlags(data.optJSONObject("featureFlags")),
            smartFavorites = parseSmartFavorites(data.optJSONArray("smartFavorites"))
        )
    }

    internal fun buildControlPlaneSnapshot(
        tvId: String,
        deviceStatus: String,
        remoteLogoutRequested: Boolean,
        updateChannel: String = "stable",
        remoteConfig: Map<String, RemoteConfigValue>,
        featureFlags: Map<String, RemoteFeatureFlag>,
        smartFavorites: List<RemoteSmartFavoriteGroup> = emptyList()
    ): DeviceControlPlaneSnapshot = DeviceControlPlaneSnapshot(
        tvId = tvId,
        deviceStatus = deviceStatus,
        remoteLogoutRequested = remoteLogoutRequested,
        updateChannel = updateChannel,
        remoteConfig = remoteConfig,
        featureFlags = featureFlags,
        smartFavorites = smartFavorites
    )

    internal fun parseSmartFavorites(source: org.json.JSONArray?): List<RemoteSmartFavoriteGroup> {
        if (source == null) return emptyList()
        return buildList {
            for (index in 0 until source.length()) {
                val item = source.optJSONObject(index) ?: continue
                val id = item.optString("id").trim()
                val name = item.optString("name").trim()
                if (id.isBlank() || name.isBlank()) continue
                val entries = item.optJSONArray("entries")?.let { values -> buildList {
                    for (entryIndex in 0 until values.length()) {
                        val entry = values.optJSONObject(entryIndex) ?: continue
                        val type = entry.optString("contentType").trim()
                        val key = entry.optString("contentKey").trim()
                        val title = entry.optString("title").trim()
                        if (type !in setOf("live", "movie", "series") || key.isBlank() || title.isBlank()) continue
                        add(RemoteSmartFavoriteEntry(type, key, title, entry.optString("posterUrl").ifBlank { null }, entry.optInt("sortOrder", 0)))
                    }
                } }.orEmpty()
                add(RemoteSmartFavoriteGroup(id, name, item.optString("color", "#dc143c"), item.optInt("sortOrder", index), entries))
            }
        }
    }

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
