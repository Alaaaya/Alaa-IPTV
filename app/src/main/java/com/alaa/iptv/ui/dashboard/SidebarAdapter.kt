package com.alaa.iptv.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemSidebarBinding
import com.alaa.iptv.ui.theme.DisplayTheme

class SidebarAdapter(
    private val items: List<SidebarItem>,
    private val displayTheme: String = AppPreferences.THEME_ALAA_CLASSIC,
    private val onItemClick: (SidebarItem) -> Unit
) : RecyclerView.Adapter<SidebarAdapter.ViewHolder>() {

    private var selectedPosition = items.indexOfFirst { it.isSelected }.takeIf { it >= 0 } ?: 0

    inner class ViewHolder(val binding: ItemSidebarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                updateFocusState(hasFocus)
            }

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    selectedPosition = pos
                    notifyDataSetChanged()
                    onItemClick(items[pos])
                }
            }
        }

        fun bind(item: SidebarItem, isSelected: Boolean) {
            binding.sidebarIcon.setImageResource(item.iconRes)
            binding.sidebarTitle.text = item.title

            if (isSelected) {
                applySelectedStyle()
            } else {
                applyUnselectedStyle()
            }
        }

        private fun updateFocusState(hasFocus: Boolean) {
            val pos = bindingAdapterPosition
            val isSelected = pos == selectedPosition

            if (hasFocus || isSelected) {
                applySelectedStyle()
            } else {
                applyUnselectedStyle()
            }
        }

        private fun applySelectedStyle() {
            if (DisplayTheme.hasCustomTheme(displayTheme)) {
                binding.root.background = DisplayTheme.focusBackground(displayTheme)
                binding.sidebarIcon.setColorFilter(DisplayTheme.focusTextColor(displayTheme))
                binding.sidebarTitle.setTextColor(DisplayTheme.focusTextColor(displayTheme))
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_sidebar_selected)
                binding.sidebarIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.white))
                binding.sidebarTitle.setTextColor(Color.WHITE)
            }
        }

        private fun applyUnselectedStyle() {
            if (DisplayTheme.hasCustomTheme(displayTheme)) {
                binding.root.background = DisplayTheme.panelBackground(displayTheme)
                binding.sidebarIcon.setColorFilter(Color.WHITE)
                binding.sidebarTitle.setTextColor(Color.WHITE)
            } else {
                binding.root.setBackgroundResource(android.R.color.transparent)
                binding.sidebarIcon.setColorFilter(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
                binding.sidebarTitle.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_secondary))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSidebarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount() = items.size
}
