package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.databinding.ItemCategoryBinding
import com.alaa.iptv.ui.common.CategoryDisplayPolicy

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onCategoryClick(categories[position])
                }
            }

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                updateUI(hasFocus)
                if (hasFocus) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedPosition(position)
                        onCategoryClick(categories[position])
                    }
                }
            }
        }

        fun bind(category: Category, isSelected: Boolean, position: Int) {
            binding.categoryName.text = CategoryDisplayPolicy.name(category)
            binding.categoryPosition.text = (position + 1).toString()
            binding.categoryCount.text = CategoryDisplayPolicy.countLabel(category.channelCount)
            
            // Initial state based on selection
            updateUI(isSelected)
        }

        private fun updateUI(hasFocus: Boolean) {
            if (hasFocus) {
                // Cyan background for focused item as seen in Universe image
                binding.rootLayout.setBackgroundColor(Color.parseColor("#3498DB")) 
                binding.pointerImageView.visibility = View.VISIBLE
                binding.categoryName.setTextColor(Color.WHITE)
                binding.categoryPosition.setTextColor(Color.WHITE)
                binding.categoryCount.setTextColor(Color.WHITE)
            } else {
                binding.rootLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.pointerImageView.visibility = View.INVISIBLE
                binding.categoryName.setTextColor(Color.WHITE)
                binding.categoryPosition.setTextColor(Color.WHITE)
                binding.categoryCount.setTextColor(Color.WHITE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition, position)
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        selectedPosition = 0
        notifyDataSetChanged()
    }

    private fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition >= 0 && previousPosition < categories.size) {
            notifyItemChanged(previousPosition)
        }
        if (selectedPosition >= 0 && selectedPosition < categories.size) {
            notifyItemChanged(selectedPosition)
        }
    }
}
