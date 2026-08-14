package com.alaa.iptv.ui.dashboard

import com.alaa.iptv.R

object CategoryVisuals {
    fun backgroundFor(categoryKey: String): Int = when (categoryKey) {
        "live" -> R.drawable.alaa_category_live
        "sports" -> R.drawable.alaa_category_sports
        "news" -> R.drawable.alaa_category_news
        "movies" -> R.drawable.alaa_category_movies
        "series" -> R.drawable.alaa_category_series
        "kids" -> R.drawable.alaa_category_kids
        "documentary" -> R.drawable.alaa_category_documentary
        "music" -> R.drawable.alaa_category_music
        else -> R.drawable.alaa_category_live
    }
}
