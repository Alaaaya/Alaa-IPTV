package com.alaa.iptv.ui.player

import java.net.URI

/**
 * يتحقق من الروابط قبل إنشاء MediaItem. لا يقبل التطبيق إلا مصادر HTTP(S)
 * المكتملة كي لا يُرسل رابط فارغ أو مخطط غير مدعوم إلى Media3.
 */
object PlaybackUrlPolicy {
    fun normalizedHttpUrlOrNull(rawUrl: String?): String? {
        val candidate = rawUrl?.trim().orEmpty()
        if (candidate.isBlank()) return null

        return runCatching {
            val uri = URI(candidate)
            val scheme = uri.scheme?.lowercase()
            candidate.takeIf {
                (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
            }
        }.getOrNull()
    }
}
