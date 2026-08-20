package com.alaa.iptv.ui.dashboard

import android.graphics.Color
import android.view.KeyEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.ui.theme.DisplayTheme
import com.alaa.iptv.databinding.ItemCategoryCardBinding
import com.alaa.iptv.ui.common.OnePressActivationPolicy

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
                binding.focusOutline.visibility = if (hasFocus) android.view.View.VISIBLE else android.view.View.GONE
                binding.iconGlow.visibility = android.view.View.GONE
                binding.root.scaleX = 1f
                binding.root.scaleY = 1f
                binding.root.elevation = 2f
                binding.categoryIcon.scaleX = 1f
                binding.categoryIcon.scaleY = 1f
            }

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(items[pos])
                }
            }
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (OnePressActivationPolicy.shouldActivate(keyCode, event.action)) {
                    binding.root.performClick()
                    true
                } else false
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
            val isNeonIptv = DisplayTheme.isNeonIptv(theme)
            binding.categoryBackdrop.alpha = if (isNeonIptv) 0.26f else DisplayTheme.categoryCardStyle(theme).backdropAlpha
            binding.categoryContent.gravity = if (isNeonIptv) Gravity.START or Gravity.CENTER_VERTICAL else Gravity.CENTER
            binding.categoryName.gravity = if (isNeonIptv) Gravity.START else Gravity.CENTER
            binding.categoryCount.gravity = if (isNeonIptv) Gravity.START else Gravity.CENTER
            binding.categoryIcon.setImageResource(item.iconRes)
            if (DisplayTheme.hasCustomTheme(theme)) {
                binding.focusOutline.background = DisplayTheme.focusOutlineBackground(theme)
            }
            val neonColor = Color.parseColor(item.colorHex)
            val style = DisplayTheme.categoryCardStyle(theme)
            binding.categoryIcon.setColorFilter(if (style.monochrome) Color.WHITE else neonColor)
            binding.categoryIcon.scaleX = 1f
            binding.categoryIcon.scaleY = 1f
            binding.iconGlow.visibility = android.view.View.GONE
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
