package com.alaa.iptv.ui.main

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelOptionsKeyPolicyTest {
    @Test
    fun `menu and held center open options but a normal center press does not`() {
        assertTrue(ChannelOptionsKeyPolicy.opensOptions(KeyEvent.KEYCODE_MENU, KeyEvent.ACTION_DOWN, 0))
        assertTrue(ChannelOptionsKeyPolicy.opensOptions(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, 1))
        assertTrue(ChannelOptionsKeyPolicy.opensOptions(KeyEvent.KEYCODE_ENTER, KeyEvent.ACTION_DOWN, 2))
        assertFalse(ChannelOptionsKeyPolicy.opensOptions(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN, 0))
        assertFalse(ChannelOptionsKeyPolicy.opensOptions(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP, 1))
    }

    @Test
    fun `release is consumed after options so the channel does not start`() {
        assertTrue(ChannelOptionsKeyPolicy.consumesReleaseAfterOptions(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP, true))
        assertTrue(ChannelOptionsKeyPolicy.consumesReleaseAfterOptions(KeyEvent.KEYCODE_MENU, KeyEvent.ACTION_UP, true))
        assertFalse(ChannelOptionsKeyPolicy.consumesReleaseAfterOptions(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP, false))
    }
}
