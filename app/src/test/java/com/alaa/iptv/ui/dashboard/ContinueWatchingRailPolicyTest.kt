package com.alaa.iptv.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ContinueWatchingRailPolicyTest {
    @Test
    fun `neon card rail includes space for focused scaling`() {
        assertEquals(209, ContinueWatchingRailPolicy.railHeightDp(178, 1.08f))
    }

    @Test
    fun `classic card rail includes space for focused scaling`() {
        assertEquals(163, ContinueWatchingRailPolicy.railHeightDp(140, 1.05f))
    }
}
