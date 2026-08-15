package com.alaa.iptv.ui.navigation

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * يمنع Android TV من نقل التركيز إلى قسم آخر عند الضغط خارج الحد العلوي أو السفلي
 * لشبكة حالية. لا يعترض الأسهم الأفقية حتى تبقى انتقالات الأعمدة المقصودة متاحة.
 */
object FocusBoundaryPolicy {
    fun blocksVerticalExit(
        keyCode: Int,
        position: Int,
        itemCount: Int,
        spanCount: Int,
        orientation: Int
    ): Boolean {
        if (position !in 0 until itemCount || spanCount <= 0) return false
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> when (orientation) {
                RecyclerView.HORIZONTAL -> position % spanCount == 0
                else -> position < spanCount
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> when (orientation) {
                RecyclerView.HORIZONTAL -> position == itemCount - 1 || position % spanCount == spanCount - 1
                else -> position + spanCount >= itemCount
            }
            else -> false
        }
    }
}
