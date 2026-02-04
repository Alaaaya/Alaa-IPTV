package com.alaa.iptv.ui.dashboard

data class DashboardItem(
    val title: String,
    val icon: Int,
    val action: () -> Unit
)