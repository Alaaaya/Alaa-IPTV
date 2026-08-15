package com.alaa.iptv.data.models

/** يختار محاولة بديلة واحدة فقط بين MPEG-TS وHLS دون الدخول في حلقة إعادة محاولة. */
object LiveUrlFallbackPolicy {
    fun nextAlternative(currentUrl: String, attemptedUrls: Set<String>): String? {
        val pathWithoutQuery = currentUrl.substringBefore('?')
        val replacement = when {
            pathWithoutQuery.endsWith(".ts", ignoreCase = true) -> ".m3u8"
            pathWithoutQuery.endsWith(".m3u8", ignoreCase = true) -> ".ts"
            else -> return null
        }
        val alternative = currentUrl.replace(
            Regex("\\.(ts|m3u8)(?=\\?|$)", RegexOption.IGNORE_CASE),
            replacement
        )
        return alternative.takeIf { it != currentUrl && it !in attemptedUrls }
    }
}
