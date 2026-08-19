package com.alaa.iptv.data.repository

import android.content.Context
import com.alaa.iptv.data.preferences.AppPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File

class MediaRepositoryDirectPagingTest {
    @Test
    fun `Xtream repository treats null category as all and preserves page total`() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val repository = MediaRepository(
            prefs = session(useM3U = false),
            context = mock(Context::class.java),
            responseOverride = { url ->
                requestedUrls += url
                xtreamLiveResponse(121)
            }
        )

        try {
            val firstPage = repository.getLiveContentPage(categoryId = null, page = 0).getOrThrow()
            val lastPage = repository.getLiveContentPage(categoryId = null, page = 1).getOrThrow()

            assertEquals(100, firstPage.items.size)
            assertEquals(121, firstPage.totalCount)
            assertTrue(firstPage.hasMore)
            assertEquals(21, lastPage.items.size)
            assertEquals(121, lastPage.totalCount)
            assertFalse(lastPage.hasMore)
            assertTrue(requestedUrls.isNotEmpty())
            assertFalse(requestedUrls.any { it.contains("category_id=") })
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `Xtream repository filters items that do not match the selected category`() = runBlocking {
        val repository = MediaRepository(
            prefs = session(useM3U = false),
            context = mock(Context::class.java),
            responseOverride = { xtreamMixedCategoryResponse() }
        )

        try {
            val page = repository.getLiveContentPage(categoryId = "sports", page = 0).getOrThrow()

            assertEquals(2, page.totalCount)
            assertEquals(listOf("Sports 1", "Sports 2"), page.items.map { it.name })
            assertTrue(page.items.all { it.categoryId == "sports" })
            assertFalse(page.hasMore)
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `M3U repository keeps the full category cache across pages`() = runBlocking {
        val playlist = File.createTempFile("alaa-m3u-page", ".m3u").apply {
            writeText(m3uPlaylist(205, "Arabic"))
            deleteOnExit()
        }
        val repository = MediaRepository(
            prefs = session(useM3U = true, serverUrl = playlist.toURI().toString()),
            context = mock(Context::class.java)
        )

        try {
            val firstPage = repository.getLiveContentPage(categoryId = "Arabic", page = 0).getOrThrow()
            val finalPage = repository.getLiveContentPage(categoryId = "Arabic", page = 2).getOrThrow()

            assertEquals(100, firstPage.items.size)
            assertEquals(205, firstPage.totalCount)
            assertTrue(firstPage.hasMore)
            assertEquals(5, finalPage.items.size)
            assertEquals(205, finalPage.totalCount)
            assertFalse(finalPage.hasMore)
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `M3U repository returns only the indexed selected category without mixing channels`() = runBlocking {
        val playlist = File.createTempFile("alaa-m3u-index", ".m3u").apply {
            writeText(m3uPlaylist(140, "Arabic") + "\n" + m3uPlaylist(75, "Sports"))
            deleteOnExit()
        }
        val repository = MediaRepository(
            prefs = session(useM3U = true, serverUrl = playlist.toURI().toString()),
            context = mock(Context::class.java)
        )

        try {
            val sports = repository.getLiveContentPage(categoryId = "Sports", page = 0).getOrThrow()
            val arabic = repository.getLiveContentPage(categoryId = "Arabic", page = 1).getOrThrow()

            assertEquals(75, sports.totalCount)
            assertEquals(75, sports.items.size)
            assertTrue(sports.items.all { it.categoryId == "Sports" })
            assertEquals(140, arabic.totalCount)
            assertEquals(40, arabic.items.size)
            assertTrue(arabic.items.all { it.categoryId == "Arabic" })
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `M3U repository ignores orphan metadata without a playable stream URL`() = runBlocking {
        val playlist = File.createTempFile("alaa-m3u-orphan", ".m3u").apply {
            writeText(
                """
                #EXTM3U
                #EXTINF:-1 tvg-id="first" group-title="Arabic",First channel
                https://stream.example/first.m3u8
                #EXTINF:-1 tvg-id="orphan" group-title="Arabic",Orphan metadata
                #EXTINF:-1 tvg-id="second" group-title="News",Second channel
                https://stream.example/second.m3u8
                """.trimIndent()
            )
            deleteOnExit()
        }
        val repository = MediaRepository(
            prefs = session(useM3U = true, serverUrl = playlist.toURI().toString()),
            context = mock(Context::class.java)
        )

        try {
            val firstPage = repository.getLiveContentPage(categoryId = null).getOrThrow()
            val categories = repository.getLiveCategories().getOrThrow()

            assertEquals(2, firstPage.totalCount)
            assertEquals(listOf("First channel", "Second channel"), firstPage.items.map { it.name })
            assertEquals(2, categories.sumOf { it.channelCount })
            assertFalse(categories.any { it.categoryName == "Orphan metadata" })
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `M3U repository rejects VOD without attempting an Xtream request`() = runBlocking {
        val repository = MediaRepository(
            prefs = session(useM3U = true, serverUrl = "file:///unused.m3u"),
            context = mock(Context::class.java)
        )

        try {
            val error = repository.getMovieContentPage(categoryId = null).exceptionOrNull()
            assertNotNull(error)
            assertTrue(error?.message.orEmpty().contains("M3U"))
        } finally {
            repository.clearCache()
        }
    }

    @Test
    fun `open M3U fixture keeps category count and pagination stable`() = runBlocking {
        val path = System.getProperty("alaa.openM3uPath")
        assumeTrue("يتطلب هذا الاختبار ملف M3U مؤقتاً يمرر من بيئة الاختبار", !path.isNullOrBlank())
        val playlist = File(path!!)
        assumeTrue("ملف M3U المؤقت غير متاح", playlist.isFile)
        val expectedEntries = playlist.useLines { lines -> lines.count(::isPlayableM3UStreamLine) }

        val repository = MediaRepository(
            prefs = session(useM3U = true, serverUrl = playlist.toURI().toString()),
            context = mock(Context::class.java)
        )

        try {
            val firstPage = repository.getLiveContentPage(categoryId = null, page = 0).getOrThrow()
            val nextPage = repository.getLiveContentPage(categoryId = null, page = 1).getOrThrow()
            val categories = repository.getLiveCategories().getOrThrow()
            val populatedCategory = categories.maxBy { it.channelCount }
            val categoryPage = repository.getLiveContentPage(populatedCategory.categoryId, page = 0).getOrThrow()

            assertEquals(expectedEntries, firstPage.totalCount)
            assertEquals(minOf(100, expectedEntries), firstPage.items.size)
            assertEquals(firstPage.totalCount, nextPage.totalCount)
            assertEquals(minOf(100, (expectedEntries - 100).coerceAtLeast(0)), nextPage.items.size)
            assertTrue(categories.isNotEmpty())
            assertEquals(expectedEntries, categories.sumOf { it.channelCount })
            assertEquals(expectedEntries > firstPage.items.size, firstPage.hasMore)
            assertEquals(populatedCategory.channelCount, categoryPage.totalCount)
            assertEquals(minOf(100, populatedCategory.channelCount), categoryPage.items.size)
        } finally {
            repository.clearCache()
        }
    }

    private fun session(
        useM3U: Boolean,
        serverUrl: String = "https://iptv.example"
    ): AppPreferences = mock(AppPreferences::class.java).also { prefs ->
        `when`(prefs.useM3U).thenReturn(useM3U)
        `when`(prefs.serverUrl).thenReturn(serverUrl)
        `when`(prefs.username).thenReturn("user")
        `when`(prefs.password).thenReturn("pass")
        `when`(prefs.isControlPlaneEnrolled).thenReturn(false)
        `when`(prefs.isDeviceAccessBlocked()).thenReturn(false)
    }

    private fun xtreamLiveResponse(size: Int): String = buildString {
        append('[')
        repeat(size) { index ->
            if (index > 0) append(',')
            append("{\"stream_id\":\"${index + 1}\",\"name\":\"Live ${index + 1}\",\"category_id\":\"sports\"}")
        }
        append(']')
    }

    private fun xtreamMixedCategoryResponse(): String = """
        [
          {"stream_id":"1","name":"Sports 1","category_id":"sports"},
          {"stream_id":"2","name":"News 1","category_id":"news"},
          {"stream_id":"3","name":"Sports 2","category_id":"sports"}
        ]
    """.trimIndent()

    private fun m3uPlaylist(size: Int, group: String): String = buildString {
        appendLine("#EXTM3U")
        repeat(size) { index ->
            appendLine("#EXTINF:-1 tvg-id=\"channel-${index + 1}\" group-title=\"$group\",Channel ${index + 1}")
            appendLine("https://stream.example/${index + 1}.m3u8")
        }
    }

    private fun isPlayableM3UStreamLine(line: String): Boolean {
        val normalized = line.trim()
        return normalized.startsWith("http://", ignoreCase = true) ||
            normalized.startsWith("https://", ignoreCase = true) ||
            normalized.startsWith("rtmp", ignoreCase = true)
    }
}
