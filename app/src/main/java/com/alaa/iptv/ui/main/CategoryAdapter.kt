package com.alaa.iptv.ui.main

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.databinding.ItemCategoryBinding

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

            // Enhanced focus handling with smooth animations
            binding.root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    animateFocusChange(view, true)
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedPosition(position)
                        onCategoryClick(categories[position])
                    }
                } else {
                    animateFocusChange(view, false)
                }
            }
        }

        private fun animateFocusChange(view: View, focused: Boolean) {
            val scale = if (focused) 1.05f else 1.0f
            val duration = 200L

            ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, scale).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                start()
            }

            ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, scale).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                start()
            }
        }

        fun bind(category: Category, isSelected: Boolean, position: Int) {
            binding.categoryName.text = category.categoryName
            
            // Set position number (1-indexed)
            binding.categoryPosition.text = (position + 1).toString()
            
            // Set category count
            binding.categoryCount.text = category.channelCount.toString()
            
            // Highlight selected
            if (isSelected) {
                binding.innerLayout.setBackgroundColor(Color.parseColor("#99D32F2F"))
                binding.rootLayout.setBackgroundColor(Color.parseColor("#332196F3"))
            } else {
                binding.innerLayout.setBackgroundColor(Color.parseColor("#66D32F2F"))
                binding.rootLayout.setBackgroundColor(Color.parseColor("#26FFFFFF"))
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
