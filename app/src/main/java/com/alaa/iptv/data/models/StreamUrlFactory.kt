package com.alaa.iptv.data.models

/**
 * Creates Xtream-compatible stream paths without retaining credentials or URLs in storage.
 * Live playback intentionally uses MPEG-TS: some providers redirect their .m3u8 routes to
 * a continuous transport stream, which must be opened by Media3 as progressive media.
 */
object StreamUrlFactory {
    private fun cleanBase(serverUrl: String): String = serverUrl.trim().removeSuffix("/")

    fun live(serverUrl: String, username: String, password: String, streamId: String): String =
        "${cleanBase(serverUrl)}/live/$username/$password/$streamId.ts"

    fun movie(
        serverUrl: String,
        username: String,
        password: String,
        streamId: String,
        extension: String?
    ): String = "${cleanBase(serverUrl)}/movie/$username/$password/$streamId.${safeExtension(extension)}"

    fun episode(
        serverUrl: String,
        username: String,
        password: String,
        episodeId: String,
        extension: String?
    ): String = "${cleanBase(serverUrl)}/series/$username/$password/$episodeId.${safeExtension(extension)}"

    private fun safeExtension(extension: String?): String =
        extension?.trim()?.removePrefix(".")?.takeIf { it.isNotBlank() } ?: "mp4"
}
