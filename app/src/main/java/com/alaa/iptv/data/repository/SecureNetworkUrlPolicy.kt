package com.alaa.iptv.data.repository

import java.net.URI

/**
 * سياسة موحّدة لمصادر IPTV: يُقبل HTTPS، ويُقبل HTTP الصريح لتوافق مزودي IPTV
 * الذين لا يملكون TLS؛ أما العناوين المحلية والحلقية فترفض دائماً.
 */
object SecureNetworkUrlPolicy {
    fun normalizeServerUrlOrNull(rawUrl: String?): String? {
        val candidate = rawUrl?.trim().orEmpty()
        if (candidate.isBlank()) return null
        val withScheme = if (candidate.contains("://")) candidate else "https://$candidate"
        return runCatching {
            val uri = URI(withScheme)
            val host = uri.host?.lowercase().orEmpty()
            val safeScheme = uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)
            if (!safeScheme || host.isBlank() || isLocalHost(host)) return null
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

    private fun isLocalHost(host: String): Boolean {
        if (host == "localhost" || host == "::1" || host.startsWith("127.")) return true
        if (host.startsWith("10.") || host.startsWith("192.168.")) return true
        if (host.startsWith("169.254.")) return true
        if (!host.startsWith("172.")) return false
        val secondOctet = host.split(".").getOrNull(1)?.toIntOrNull() ?: return false
        return secondOctet in 16..31
    }
}
