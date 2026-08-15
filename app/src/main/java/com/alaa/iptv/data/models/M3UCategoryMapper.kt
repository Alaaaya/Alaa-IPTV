package com.alaa.iptv.data.models

object M3UCategoryMapper {
    fun categories(channels: List<Channel>): List<Category> {
        val grouped = linkedMapOf<String, Pair<String, Int>>()
        channels.forEach { channel ->
            val categoryId = channel.categoryId?.takeIf { it.isNotBlank() } ?: "Uncategorized"
            val categoryName = channel.categoryName?.takeIf { it.isNotBlank() } ?: categoryId
            val previous = grouped[categoryId]
            grouped[categoryId] = categoryName to ((previous?.second ?: 0) + 1)
        }
        return grouped.map { (id, value) ->
            Category(categoryId = id, categoryName = value.first, channelCount = value.second)
        }
    }

    fun page(channels: List<Channel>, categoryId: String?, page: Int, pageSize: Int): List<Channel> {
        val scoped = categoryId?.takeIf { it != "all" }?.let { selectedId ->
            channels.filter { it.categoryId == selectedId }
        } ?: channels
        return scoped.drop(page.coerceAtLeast(0) * pageSize).take(pageSize)
    }
}
