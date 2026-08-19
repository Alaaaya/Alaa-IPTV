package com.alaa.iptv.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardFocusPolicyTest {
    @Test
    fun `vertical route follows hero categories and continue watching`() {
        assertEquals(
            DashboardFocusPolicy.Destination.CATEGORIES,
            DashboardFocusPolicy.verticalDestination(DashboardFocusPolicy.Zone.HERO, moveDown = true, hasContinueWatching = true)
        )
        assertEquals(
            DashboardFocusPolicy.Destination.CONTINUE_WATCHING,
            DashboardFocusPolicy.verticalDestination(DashboardFocusPolicy.Zone.CATEGORIES, moveDown = true, hasContinueWatching = true)
        )
        assertEquals(
            DashboardFocusPolicy.Destination.CATEGORIES,
            DashboardFocusPolicy.verticalDestination(DashboardFocusPolicy.Zone.CONTINUE_WATCHING, moveDown = false, hasContinueWatching = true)
        )
    }

    @Test
    fun `bottom and unavailable rows block instead of leaking focus`() {
        assertEquals(
            DashboardFocusPolicy.Destination.BLOCK,
            DashboardFocusPolicy.verticalDestination(DashboardFocusPolicy.Zone.CATEGORIES, moveDown = true, hasContinueWatching = false)
        )
        assertEquals(
            DashboardFocusPolicy.Destination.BLOCK,
            DashboardFocusPolicy.verticalDestination(DashboardFocusPolicy.Zone.CONTINUE_WATCHING, moveDown = true, hasContinueWatching = true)
        )
    }
}
