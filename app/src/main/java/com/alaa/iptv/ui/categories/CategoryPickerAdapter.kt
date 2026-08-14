package com.alaa.iptv.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.databinding.ItemCategoryPickerBinding

class CategoryPickerAdapter(
    private val onSelected: (Category) -> Unit
) : RecyclerView.Adapter<CategoryPickerAdapter.CategoryViewHolder>() {
    private var categories: List<Category> = emptyList()

    fun submit(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
        CategoryViewHolder(
            ItemCategoryPickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategoryPickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            binding.categoryName.text = category.categoryName
            binding.root.setOnClickListener { onSelected(category) }
        }
    }
}
