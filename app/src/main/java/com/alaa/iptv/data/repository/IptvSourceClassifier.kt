package com.alaa.iptv.data.repository

/** يميّز روابط قوائم M3U الصريحة من عنوان مضيف Xtream العادي. */
object IptvSourceClassifier {
    fun isM3U(url: String): Boolean {
        val normalized = url.trim().lowercase()
        if (normalized.startsWith("file:")) return true

        val path = normalized.substringBefore('?').trimEnd('/')
        val query = normalized.substringAfter('?', missingDelimiterValue = "")
        return path.endsWith(".m3u") ||
            path.endsWith(".m3u8") ||
            path.endsWith("/get.php") ||
            path.endsWith("/m3u") ||
            query.contains("type=m3u") ||
            query.contains("output=m3u")
    }
}
