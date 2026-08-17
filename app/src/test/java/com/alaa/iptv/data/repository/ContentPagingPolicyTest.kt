package com.alaa.iptv.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentPagingPolicyTest {
    @Test
    fun `Xtream first page remains bounded while preserving provider total`() {
        val page = ContentPagingPolicy.bounds(totalCount = 121, requestedPage = 0, pageSize = 120)

        assertEquals(0, page.startIndex)
        assertEquals(120, page.endIndex)
        assertEquals(121, page.totalCount)
        assertTrue(page.hasMore)
    }

    @Test
    fun `Xtream last page has the remaining item and no further page`() {
        val page = ContentPagingPolicy.bounds(totalCount = 121, requestedPage = 1, pageSize = 120)

        assertEquals(120, page.startIndex)
        assertEquals(121, page.endIndex)
        assertFalse(page.hasMore)
    }

    @Test
    fun `M3U final live page keeps the complete category count`() {
        val page = ContentPagingPolicy.bounds(totalCount = 205, requestedPage = 2, pageSize = 100)

        assertEquals(200, page.startIndex)
        assertEquals(205, page.endIndex)
        assertEquals(205, page.totalCount)
        assertFalse(page.hasMore)
    }

    @Test
    fun `negative page is safely treated as the first page`() {
        val page = ContentPagingPolicy.bounds(totalCount = 100, requestedPage = -4, pageSize = 100)

        assertEquals(0, page.startIndex)
        assertEquals(100, page.endIndex)
        assertFalse(page.hasMore)
    }
}
