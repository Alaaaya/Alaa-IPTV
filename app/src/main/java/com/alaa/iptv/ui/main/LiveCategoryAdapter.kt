package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Category
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemLiveCategoryBinding
import com.alaa.iptv.ui.common.OnePressActivationPolicy
import com.alaa.iptv.ui.navigation.FocusBoundaryPolicy
import com.alaa.iptv.ui.theme.DisplayTheme

class LiveCategoryAdapter(
    private val displayTheme: String = AppPreferences.THEME_ALAA_CLASSIC,
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
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                if (OnePressActivationPolicy.shouldActivate(keyCode, event.action)) {
                    binding.root.performClick()
                    return@setOnKeyListener true
                }
                val spec = DisplayTheme.liveCategorySpec(displayTheme)
                if (spec.placement == DisplayTheme.LiveCategoryPlacement.TOP_RAIL) return@setOnKeyListener false
                FocusBoundaryPolicy.blocksVerticalExit(
                    keyCode = keyCode,
                    position = bindingAdapterPosition,
                    itemCount = categories.size,
                    spanCount = spec.spanCount,
                    orientation = RecyclerView.VERTICAL
                )
            }
        }

        fun bind(category: Category) {
            val spec = DisplayTheme.liveCategorySpec(displayTheme)
            val suffix = if (spec.labelPrefix == "[ ") " ]" else ""
            binding.categoryName.text = "${spec.labelPrefix}${category.categoryName}$suffix"
            binding.categoryName.textSize = LiveCategoryLayoutPolicy.compactNameSizeSp(spec.textSizeSp)
            binding.categoryNumber.text = (bindingAdapterPosition + 1).toString()
            binding.categoryCount.text = if (category.channelCount > 0) {
                "${category.channelCount} عنصر في هذه الفئة"
            } else {
                "اضغط لعرض محتوى الفئة"
            }
            val density = binding.root.resources.displayMetrics.density
            binding.root.layoutParams = binding.root.layoutParams.apply {
                height = (LiveCategoryLayoutPolicy.compactItemHeightDp(spec.itemHeightDp) * density).toInt()
            }
            render(category)
        }

        private fun render(category: Category) {
            val highlighted = binding.root.hasFocus() || category.categoryId == selectedCategoryId
            if (DisplayTheme.hasCustomTheme(displayTheme)) {
                binding.root.background = if (DisplayTheme.isNeonIptv(displayTheme) && highlighted) {
                    binding.root.context.getDrawable(R.drawable.bg_live_category_selected)
                } else if (highlighted) {
                    DisplayTheme.focusBackground(displayTheme)
                } else {
                    DisplayTheme.panelBackground(displayTheme)
                }
                binding.categoryName.setTextColor(
                    if (highlighted) DisplayTheme.focusTextColor(displayTheme) else DisplayTheme.metadataColor(displayTheme)
                )
                binding.categoryCount.setTextColor(
                    if (highlighted) DisplayTheme.focusTextColor(displayTheme) else DisplayTheme.metadataColor(displayTheme)
                )
                binding.categoryNumber.setBackgroundResource(
                    if (highlighted) R.drawable.bg_category_number_selected else R.drawable.bg_category_number_default
                )
            } else {
                binding.root.setBackgroundResource(
                    if (highlighted) R.drawable.bg_live_category_selected else R.drawable.bg_live_category_default
                )
                binding.categoryName.setTextColor(if (highlighted) Color.WHITE else Color.parseColor("#D8E1EF"))
                binding.categoryCount.setTextColor(if (highlighted) Color.WHITE else Color.parseColor("#B7C9DE"))
                binding.categoryNumber.setBackgroundResource(
                    if (highlighted) R.drawable.bg_category_number_selected else R.drawable.bg_category_number_default
                )
            }
        }
    }
}
