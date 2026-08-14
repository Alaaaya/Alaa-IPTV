package com.alaa.iptv.ui.categories

import com.alaa.iptv.data.models.Category

object CategorySearch {
    fun filter(categories: List<Category>, query: String): List<Category> {
        val normalizedQuery = query.trim()
        return categories
            .asSequence()
            .filter { category ->
                normalizedQuery.isBlank() || category.categoryName.contains(normalizedQuery, ignoreCase = true)
            }
            .sortedBy { it.categoryName.lowercase() }
            .toList()
    }
}
