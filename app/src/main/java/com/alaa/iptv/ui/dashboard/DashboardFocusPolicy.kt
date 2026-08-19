package com.alaa.iptv.ui.dashboard

/** قواعد انتقال D-Pad العمودية في الصفحة الرئيسية للتلفزيون. */
object DashboardFocusPolicy {
    enum class Zone { HERO, CATEGORIES, CONTINUE_WATCHING, OTHER }
    enum class Destination { HERO, CATEGORIES, CONTINUE_WATCHING, BLOCK, DEFAULT }

    fun verticalDestination(zone: Zone, moveDown: Boolean, hasContinueWatching: Boolean): Destination = when (zone) {
        Zone.HERO -> if (moveDown) Destination.CATEGORIES else Destination.DEFAULT
        Zone.CATEGORIES -> if (moveDown) {
            if (hasContinueWatching) Destination.CONTINUE_WATCHING else Destination.BLOCK
        } else {
            Destination.HERO
        }
        Zone.CONTINUE_WATCHING -> if (moveDown) Destination.BLOCK else Destination.CATEGORIES
        Zone.OTHER -> Destination.DEFAULT
    }
}
