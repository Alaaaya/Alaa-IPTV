package com.alaa.iptv.ui.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DirectSectionNavigationPolicyTest {
    @Test
    fun `opening a top level section retires the origin screen`() {
        assertTrue(DirectSectionNavigationPolicy.shouldRetireOriginAfterOpen())
    }

    @Test
    fun `settings returns to dashboard only when dashboard opened it`() {
        assertTrue(DirectSectionNavigationPolicy.settingsReturnsToDashboard(true))
        assertFalse(DirectSectionNavigationPolicy.settingsReturnsToDashboard(false))
    }
}
