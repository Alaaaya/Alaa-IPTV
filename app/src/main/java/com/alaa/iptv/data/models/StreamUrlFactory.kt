package com.alaa.iptv.data.models

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Creates Xtream-compatible stream paths without retaining credentials or URLs in storage.
 * Live playback intentionally uses MPEG-TS: some providers redirect their .m3u8 routes to
 * a continuous transport stream, which must be opened by Media3 as progressive media.
 */
object StreamUrlFactory {
    private fun cleanBase(serverUrl: String): String = serverUrl.trim().trimEnd('/')
    private fun segment(value: String): String =
        URLEncoder.encode(value.trim(), StandardCharsets.UTF_8.toString()).replace("+", "%20")

    fun live(serverUrl: String, username: String, password: String, streamId: String): String =
        "${cleanBase(serverUrl)}/live/${segment(username)}/${segment(password)}/${segment(streamId)}.ts"

    fun movie(
        serverUrl: String,
        username: String,
        password: String,
        streamId: String,
        extension: String?
    ): String = "${cleanBase(serverUrl)}/movie/${segment(username)}/${segment(password)}/${segment(streamId)}.${safeExtension(extension)}"

    fun episode(
        serverUrl: String,
        username: String,
        password: String,
        episodeId: String,
        extension: String?
    ): String = "${cleanBase(serverUrl)}/series/${segment(username)}/${segment(password)}/${segment(episodeId)}.${safeExtension(extension)}"

    private fun safeExtension(extension: String?): String {
        val candidate = extension
            ?.trim()
            ?.lowercase()
            ?.removePrefix(".")
            ?.substringBefore('?')
            ?.substringBefore('#')
            ?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) }
        return candidate ?: "mp4"
    }
}
