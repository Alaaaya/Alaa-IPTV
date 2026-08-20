package com.alaa.iptv.ui.common

/** يبقي كل قسم مستقلاً ولا يترك شاشة الرئيسية كواجهة وسيطة عند فتحه. */
object DirectSectionNavigationPolicy {
    fun shouldRetireOriginAfterOpen(): Boolean = true

    fun settingsReturnsToDashboard(launchedFromDashboard: Boolean): Boolean = launchedFromDashboard
}
