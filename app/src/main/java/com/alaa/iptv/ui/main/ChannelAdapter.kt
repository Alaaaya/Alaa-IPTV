package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.databinding.ItemChannelBinding
import com.bumptech.glide.Glide

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit,
    private val onChannelLongClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = -1

    inner class ChannelViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChannelClick(channels[position])
                    setSelectedPosition(position)
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChannelLongClick(channels[position])
                    true
                } else {
                    false
                }
            }

            binding.root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.scaleX = 1.05f
                    view.scaleY = 1.05f
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onChannelClick(channels[position])
                    }
                } else {
                    view.scaleX = 1.0f
                    view.scaleY = 1.0f
                }
            }
        }

        fun bind(channel: Channel, isSelected: Boolean) {
            binding.channelName.text = channel.name
            binding.channelNumber.text = "Ch ${channel.num}"
            
            if (channel.isFavorite) {
                binding.favoriteIndicator.visibility = View.VISIBLE
            } else {
                binding.favoriteIndicator.visibility = View.GONE
            }

            // Load channel icon
            if (!channel.streamIcon.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(channel.streamIcon)
                    .placeholder(R.drawable.app_banner)
                    .error(R.drawable.app_banner)
                    .into(binding.channelIcon)
            } else {
                binding.channelIcon.setImageResource(R.drawable.app_banner)
            }

            // Highlight selected
            if (isSelected) {
                binding.root.setBackgroundColor(Color.parseColor("#332196F3"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChannelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.bind(channels[position], position == selectedPosition)
    }

    override fun getItemCount() = channels.size

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition >= 0) {
            notifyItemChanged(previousPosition)
        }
        notifyItemChanged(selectedPosition)
    }
}
