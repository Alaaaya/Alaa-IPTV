package com.alaa.iptv.data.repository

import java.net.URI

/**
 * سياسة موحّدة لمصادر IPTV: النقل عن بُعد يكون عبر HTTPS فقط،
 * بينما يُسمح بقوائم M3U المحلية التي يستوردها المستخدم إلى مساحة التطبيق.
 */
object SecureNetworkUrlPolicy {
    fun normalizeServerUrlOrNull(rawUrl: String?): String? {
        val candidate = rawUrl?.trim().orEmpty()
        if (candidate.isBlank()) return null
        val withScheme = if (candidate.contains("://")) candidate else "https://$candidate"
        return runCatching {
            val uri = URI(withScheme)
            if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank()) return null
            withScheme
                .substringBefore("?")
                .removeSuffix("/")
                .removeSuffix("/player_api.php")
                .removeSuffix("/")
        }.getOrNull()
    }

    fun isAllowedRemoteUrl(rawUrl: String?): Boolean = normalizeServerUrlOrNull(rawUrl) != null

    fun isAllowedPlaylistUrl(rawUrl: String?): Boolean {
        val candidate = rawUrl?.trim().orEmpty()
        return candidate.startsWith("file:", ignoreCase = true) || isAllowedRemoteUrl(candidate)
    }
}
