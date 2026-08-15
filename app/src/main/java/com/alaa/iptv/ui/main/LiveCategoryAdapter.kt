package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.databinding.ItemLiveCategoryBinding

class LiveCategoryAdapter(
    private val onCategorySelected: (Category) -> Unit
) : RecyclerView.Adapter<LiveCategoryAdapter.CategoryViewHolder>() {
    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: String? = null

    fun submit(categories: List<Category>, selectedCategoryId: String?) {
        this.categories = categories
        this.selectedCategoryId = selectedCategoryId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
        CategoryViewHolder(
            ItemLiveCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemLiveCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onCategorySelected(categories[position])
            }
            binding.root.setOnFocusChangeListener { _, _ ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) render(categories[position])
            }
        }

        fun bind(category: Category) {
            binding.categoryName.text = category.categoryName
            render(category)
        }

        private fun render(category: Category) {
            val highlighted = binding.root.hasFocus() || category.categoryId == selectedCategoryId
            binding.root.setBackgroundResource(
                if (highlighted) R.drawable.bg_live_category_selected else R.drawable.bg_live_category_default
            )
            binding.categoryName.setTextColor(if (highlighted) Color.WHITE else Color.parseColor("#D8E1EF"))
        }
    }
}
