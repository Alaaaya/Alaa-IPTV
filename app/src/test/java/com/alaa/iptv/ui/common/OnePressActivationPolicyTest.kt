package com.alaa.iptv.ui.common

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnePressActivationPolicyTest {
    @Test
    fun `center and enter activate only on the initial press`() {
        assertTrue(OnePressActivationPolicy.shouldActivate(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_DOWN))
        assertTrue(OnePressActivationPolicy.shouldActivate(KeyEvent.KEYCODE_ENTER, KeyEvent.ACTION_DOWN))
        assertTrue(OnePressActivationPolicy.shouldActivate(KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.ACTION_DOWN))
        assertFalse(OnePressActivationPolicy.shouldActivate(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.ACTION_UP))
        assertFalse(OnePressActivationPolicy.shouldActivate(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.ACTION_DOWN))
    }
}
