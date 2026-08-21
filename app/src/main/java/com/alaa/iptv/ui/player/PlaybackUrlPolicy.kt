package com.alaa.iptv.ui.player

import java.net.URI

/**
 * يتحقق من الروابط قبل إنشاء MediaItem. لا يقبل التطبيق إلا مصادر HTTPS
 * المكتملة كي لا تُرسل بيانات أو بث IPTV عبر نقل غير مشفّر إلى Media3.
 */
object PlaybackUrlPolicy {
    fun normalizedHttpsUrlOrNull(rawUrl: String?): String? {
        val candidate = rawUrl?.trim().orEmpty()
        if (candidate.isBlank()) return null

        return runCatching {
            val uri = URI(candidate)
            candidate.takeIf {
                uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
            }
        }.getOrNull()
    }

    @Deprecated(
        message = "استخدم normalizedHttpsUrlOrNull؛ HTTP غير مسموح لحماية البث وبيانات الاشتراك.",
        replaceWith = ReplaceWith("normalizedHttpsUrlOrNull(rawUrl)")
    )
    fun normalizedHttpUrlOrNull(rawUrl: String?): String? = normalizedHttpsUrlOrNull(rawUrl)
}
