package com.alaa.iptv.ui.main

import android.view.KeyEvent

/** مفاتيح الريموت التي تفتح قائمة خيارات القناة بلا تشغيل القناة بالخطأ. */
object ChannelOptionsKeyPolicy {
    fun opensOptions(keyCode: Int, action: Int, repeatCount: Int): Boolean =
        action == KeyEvent.ACTION_DOWN && (
            keyCode == KeyEvent.KEYCODE_MENU ||
                (keyCode in setOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER) && repeatCount > 0)
            )

    fun consumesReleaseAfterOptions(keyCode: Int, action: Int, optionsWereOpened: Boolean): Boolean =
        optionsWereOpened &&
            action == KeyEvent.ACTION_UP &&
            keyCode in setOf(
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_MENU
            )
}
