package com.alaa.iptv.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardRailLayoutPolicyTest {
    @Test
    fun `rail height follows the active card and focus scale`() {
        assertEquals(154, DashboardRailLayoutPolicy.categoryRailHeightDp(118, 1.10f))
        assertEquals(164, DashboardRailLayoutPolicy.categoryRailHeightDp(152, 0.92f))
    }

    @Test
    fun `rail height reserves focus clearance without adopting another themes maximum`() {
        assertEquals(120, DashboardRailLayoutPolicy.categoryRailHeightDp(96, 1.0f))
    }
}
