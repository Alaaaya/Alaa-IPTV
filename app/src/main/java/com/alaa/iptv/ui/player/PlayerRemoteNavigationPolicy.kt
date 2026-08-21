package com.alaa.iptv.ui.player

import android.view.KeyEvent

/** يثبت لغة ريموت المشغّل: القنوات يسار/OK، وعناصر الفيديو يمين. */
object PlayerRemoteNavigationPolicy {
    fun opensChannelList(keyCode: Int, isLive: Boolean): Boolean = isLive && keyCode in setOf(
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_DPAD_LEFT
    )

    fun opensPlayerControls(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
}
