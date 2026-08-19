package com.alaa.iptv.ui.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveContentFocusPolicyTest {
    @Test
    fun `initial network content can receive focus`() {
        assertTrue(LiveContentFocusPolicy.requestFocusAfterRefresh(false, false))
    }

    @Test
    fun `background refresh does not steal focus after cached content is shown`() {
        assertFalse(LiveContentFocusPolicy.requestFocusAfterRefresh(true, false))
    }

    @Test
    fun `background refresh preserves focus when user remains in channel list`() {
        assertTrue(LiveContentFocusPolicy.requestFocusAfterRefresh(true, true))
    }

    @Test
    fun `appending a page only requests focus when the channel list owns it`() {
        assertFalse(LiveContentFocusPolicy.requestFocusAfterAppend(false))
        assertTrue(LiveContentFocusPolicy.requestFocusAfterAppend(true))
    }
}
