package com.alaa.iptv.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ItemCategoryCardBinding

class CategoryCardAdapter(
    private val items: List<CategoryItem>,
    private val onItemClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryCardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.root.scaleX = 1.05f
                    binding.root.scaleY = 1.05f
                    binding.root.elevation = 8f
                } else {
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                    binding.root.elevation = 2f
                }
            }

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(items[pos])
                }
            }
        }

        fun bind(item: CategoryItem) {
            binding.categoryName.text = item.name
            binding.categoryCount.text = if (item.count > 0) item.count.toString() else ""
            binding.categoryIcon.setImageResource(item.iconRes)
            binding.categoryIcon.setColorFilter(Color.parseColor(item.colorHex))
            binding.cardBackground.setCardBackgroundColor(Color.parseColor(item.colorHex + "20")) // 20 = 12% opacity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
