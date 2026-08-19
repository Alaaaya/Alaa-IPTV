package com.alaa.iptv.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelFocusMotionPolicyTest {
    @Test
    fun `motion is disabled when system animations are disabled`() {
        assertEquals(0L, ChannelFocusMotionPolicy.durationMs(hasFocus = true, animatorScale = 0f))
        assertEquals(0L, ChannelFocusMotionPolicy.durationMs(hasFocus = false, animatorScale = -1f))
    }

    @Test
    fun `focus animation remains short at normal system scale`() {
        assertEquals(110L, ChannelFocusMotionPolicy.durationMs(hasFocus = true, animatorScale = 1f))
        assertEquals(85L, ChannelFocusMotionPolicy.durationMs(hasFocus = false, animatorScale = 1f))
        assertTrue(ChannelFocusMotionPolicy.durationMs(hasFocus = true, animatorScale = 5f) <= 180L)
    }
}
