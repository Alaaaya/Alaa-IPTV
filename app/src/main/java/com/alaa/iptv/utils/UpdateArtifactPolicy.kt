package com.alaa.iptv.utils

import java.net.URI

/** قواعد نقية قابلة للاختبار تمنع تنزيل APK من مصدر غير إصدار Alaa Player الرسمي. */
object UpdateArtifactPolicy {
    private const val GITHUB_HOST = "github.com"
    private const val RELEASE_PREFIX = "/Alaaaya/Alaa-IPTV/releases/download/"

    fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = parseVersion(latest) ?: return false
        val currentParts = parseVersion(current) ?: return false
        val maxSize = maxOf(latestParts.size, currentParts.size)
        for (index in 0 until maxSize) {
            val latestPart = latestParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    fun isTrustedDownloadUrl(url: String): Boolean = runCatching {
        val parsed = URI(url)
        parsed.scheme.equals("https", ignoreCase = true) &&
            parsed.host.equals(GITHUB_HOST, ignoreCase = true) &&
            parsed.path.startsWith(RELEASE_PREFIX) &&
            parsed.path.substringAfterLast('/').matches(Regex("AlaaPlayer-[0-9]+(\\.[0-9]+){1,3}\\.apk"))
    }.getOrDefault(false)

    fun releaseNotesPreview(notes: String): String = notes.trim().take(600)

    private fun parseVersion(version: String): List<Int>? {
        val clean = version.removePrefix("v").trim()
        if (!clean.matches(Regex("[0-9]+(\\.[0-9]+){1,3}"))) return null
        return clean.split('.').map { it.toIntOrNull() ?: return null }
    }
}
