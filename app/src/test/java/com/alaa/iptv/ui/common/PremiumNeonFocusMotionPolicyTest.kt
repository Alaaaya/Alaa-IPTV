package com.alaa.iptv.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumNeonFocusMotionPolicyTest {
    @Test
    fun `premium pulse is two seconds at normal Android animation scale`() {
        assertEquals(2_000L, PremiumNeonFocusMotionPolicy.scaledDuration(PremiumNeonFocusMotionPolicy.PULSE_DURATION_MS, 1f))
        assertTrue(PremiumNeonFocusMotionPolicy.PULSE_DURATION_MS in 1_800L..2_200L)
    }

    @Test
    fun `premium motion is disabled when system animation scale is zero`() {
        assertEquals(0L, PremiumNeonFocusMotionPolicy.scaledDuration(PremiumNeonFocusMotionPolicy.FOCUS_IN_DURATION_MS, 0f))
        assertEquals(0L, PremiumNeonFocusMotionPolicy.scaledDuration(PremiumNeonFocusMotionPolicy.PULSE_DURATION_MS, -1f))
    }
}
