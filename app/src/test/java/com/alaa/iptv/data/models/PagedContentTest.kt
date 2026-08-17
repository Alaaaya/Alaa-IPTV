package com.alaa.iptv.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PagedContentTest {
    @Test
    fun `keeps the provider total separate from the visible page`() {
        val page = PagedContent(
            items = listOf("one", "two", "three"),
            totalCount = 121,
            hasMore = true
        )

        assertEquals(3, page.items.size)
        assertEquals(121, page.totalCount)
        assertTrue(page.hasMore)
    }

    @Test
    fun `accepts a complete category when visible items equal provider total`() {
        val page = PagedContent(
            items = listOf("one", "two"),
            totalCount = 2,
            hasMore = false
        )

        assertFalse(page.hasMore)
        assertEquals(page.items.size, page.totalCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects an impossible total smaller than the visible page`() {
        PagedContent(
            items = listOf("one", "two"),
            totalCount = 1,
            hasMore = false
        )
    }
}
