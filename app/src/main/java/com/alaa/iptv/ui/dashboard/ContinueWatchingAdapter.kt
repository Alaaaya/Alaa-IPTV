package com.alaa.iptv.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ItemContinueWatchingBinding
import com.bumptech.glide.Glide

class ContinueWatchingAdapter(
    private val items: List<ContinueWatchingItem>,
    private val onItemClick: (ContinueWatchingItem) -> Unit
) : RecyclerView.Adapter<ContinueWatchingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemContinueWatchingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.root.scaleX = 1.05f
                    binding.root.scaleY = 1.05f
                    binding.playOverlay.visibility = View.VISIBLE
                } else {
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                    binding.playOverlay.visibility = View.GONE
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
            binding.contentTitle.text = item.title
            binding.contentSubtitle.text = item.subtitle

            // Progress bar
            binding.progressBar.progress = item.progress

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
