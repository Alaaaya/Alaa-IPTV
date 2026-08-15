package com.alaa.iptv.ui.dashboard

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.databinding.ItemCategoryCardBinding

class CategoryCardAdapter(
    private val items: List<CategoryItem>,
    private val theme: String,
    private val onItemClick: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryCardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                val style = DisplayTheme.categoryCardStyle(theme)
                binding.focusOutline.visibility = if (hasFocus) android.view.View.VISIBLE else android.view.View.GONE
                binding.iconGlow.alpha = if (style.monochrome) 0f else if (hasFocus) 1.0f else 0.72f
                if (hasFocus) {
                    binding.root.scaleX = style.focusScale
                    binding.root.scaleY = style.focusScale
                    binding.root.elevation = style.focusedElevation
                    binding.categoryIcon.scaleX = style.iconScale
                    binding.categoryIcon.scaleY = style.iconScale
                } else {
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                    binding.root.elevation = 2f
                    binding.categoryIcon.scaleX = 1.0f
                    binding.categoryIcon.scaleY = 1.0f
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
            val spec = DisplayTheme.dashboardCardSpec(theme)
            val density = binding.root.resources.displayMetrics.density
            binding.cardBackground.layoutParams = binding.cardBackground.layoutParams.apply {
                width = (spec.widthDp * density).toInt()
                height = (spec.heightDp * density).toInt()
            }
            binding.cardBackground.radius = spec.radiusDp * density
            binding.categoryName.textSize = spec.titleSizeSp
            binding.categoryCount.visibility = if (spec.showCount) android.view.View.VISIBLE else android.view.View.GONE
            binding.categoryName.text = item.name
            binding.categoryCount.text = if (item.count > 0) item.count.toString() else ""
            binding.categoryBackdrop.setImageResource(item.backgroundRes)
            binding.categoryBackdrop.alpha = DisplayTheme.categoryCardStyle(theme).backdropAlpha
            binding.categoryIcon.setImageResource(item.iconRes)
            val neonColor = Color.parseColor(item.colorHex)
            val style = DisplayTheme.categoryCardStyle(theme)
            binding.categoryIcon.setColorFilter(if (style.monochrome) Color.WHITE else neonColor)
            binding.categoryIcon.scaleX = spec.iconScale
            binding.categoryIcon.scaleY = spec.iconScale
            binding.iconGlow.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                val alpha = (70 * style.glowMultiplier).toInt().coerceAtMost(120)
                setColor(Color.argb(alpha, Color.red(neonColor), Color.green(neonColor), Color.blue(neonColor)))
                setStroke(2, if (style.monochrome) Color.WHITE else neonColor)
            }
            binding.cardBackground.setCardBackgroundColor(Color.TRANSPARENT)
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
