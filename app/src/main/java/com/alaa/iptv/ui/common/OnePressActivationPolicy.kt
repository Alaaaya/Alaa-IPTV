package com.alaa.iptv.ui.common

import android.view.KeyEvent

/** يمنع نموذج «اضغط مرة للتركيز ومرة للفتح» في عناصر Android TV القابلة للنقر. */
object OnePressActivationPolicy {
    fun shouldActivate(keyCode: Int, action: Int): Boolean =
        action == KeyEvent.ACTION_DOWN && keyCode in setOf(
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER
        )
}
