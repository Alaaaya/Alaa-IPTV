package com.alaa.iptv.ui.player

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerRemoteNavigationPolicyTest {
    @Test
    fun `live playback opens the channel list with OK Enter or left`() {
        assertTrue(PlayerRemoteNavigationPolicy.opensChannelList(KeyEvent.KEYCODE_DPAD_CENTER, true))
        assertTrue(PlayerRemoteNavigationPolicy.opensChannelList(KeyEvent.KEYCODE_ENTER, true))
        assertTrue(PlayerRemoteNavigationPolicy.opensChannelList(KeyEvent.KEYCODE_DPAD_LEFT, true))
    }

    @Test
    fun `non live playback does not open a channel list and right opens controls`() {
        assertFalse(PlayerRemoteNavigationPolicy.opensChannelList(KeyEvent.KEYCODE_DPAD_CENTER, false))
        assertFalse(PlayerRemoteNavigationPolicy.opensChannelList(KeyEvent.KEYCODE_DPAD_LEFT, false))
        assertTrue(PlayerRemoteNavigationPolicy.opensPlayerControls(KeyEvent.KEYCODE_DPAD_RIGHT))
    }
}
