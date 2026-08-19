package com.alaa.iptv.ui.dashboard

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemContinueWatchingBinding
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class ContinueWatchingAdapter(
    private val items: List<ContinueWatchingItem>,
    private val theme: String = AppPreferences.THEME_ALAA_CLASSIC,
    private val onItemClick: (ContinueWatchingItem) -> Unit
) : RecyclerView.Adapter<ContinueWatchingAdapter.ViewHolder>() {

    fun railHeightDp(): Int = if (DisplayTheme.isNeonIptv(theme)) {
        ContinueWatchingRailPolicy.railHeightDp(cardHeightDp = 178, focusScale = 1.08f)
    } else {
        ContinueWatchingRailPolicy.railHeightDp(cardHeightDp = 140, focusScale = 1.05f)
    }

    inner class ViewHolder(val binding: ItemContinueWatchingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val scale = if (DisplayTheme.isNeonIptv(theme)) 1.08f else 1.05f
                    binding.root.scaleX = scale
                    binding.root.scaleY = scale
                    binding.root.cardElevation = if (DisplayTheme.isNeonIptv(theme)) 18f else 8f
                    binding.playOverlay.visibility = View.VISIBLE
                    binding.continueFocusOutline.visibility = if (DisplayTheme.isNeonIptv(theme)) View.VISIBLE else View.GONE
                } else {
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                    binding.root.cardElevation = 2f
                    binding.playOverlay.visibility = View.GONE
                    binding.continueFocusOutline.visibility = View.GONE
                }
                if (hasFocus) {
                    (binding.root.parent as? RecyclerView)?.smoothScrollToPosition(bindingAdapterPosition)
                }
            }

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(items[pos])
                }
            }
        }

        fun bind(item: ContinueWatchingItem) {
            if (DisplayTheme.isNeonIptv(theme)) {
                val density = binding.root.resources.displayMetrics.density
                binding.root.layoutParams = binding.root.layoutParams.apply {
                    width = (258 * density).toInt()
                    height = (178 * density).toInt()
                }
                binding.root.radius = 20f * density
            }
            binding.contentTitle.text = item.title
            binding.contentSubtitle.text = item.subtitle

            // Progress bar
            binding.progressBar.progress = item.progress
            if (DisplayTheme.hasCustomTheme(theme)) {
                val accent = ColorStateList.valueOf(DisplayTheme.playbackAccentColor(theme))
                binding.progressBar.progressTintList = accent
                binding.progressBar.indeterminateTintList = accent
            }
            binding.playOverlay.setBackgroundColor(DisplayTheme.playbackOverlayColor(theme))
            if (DisplayTheme.isNeonIptv(theme)) {
                binding.root.setCardBackgroundColor(DisplayTheme.cardSurfaceColor(theme))
            }

            // Load image
            if (!item.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.app_banner)
                    .error(R.drawable.app_banner)
                    .centerCrop()
                    .into(binding.contentImage)
            } else {
                binding.contentImage.setImageResource(R.drawable.app_banner)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContinueWatchingBinding.inflate(
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
