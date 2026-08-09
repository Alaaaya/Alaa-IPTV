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
            // === CLICK: Play channel directly ===
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onChannelClick(channels[position])
                }
            }

            // === LONG CLICK: Also play (shortcut) ===
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChannelLongClick(channels[position])
                    true
                } else {
                    false
                }
            }

            // === FOCUS: Update preview/selection only (no playback) ===
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                updateUI(hasFocus)
                if (hasFocus) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedPosition(position)
                        // Only update preview on focus, do NOT play
                        onChannelFocus(channels[position])
                    }
                }
            }
        }

        fun bind(channel: Channel, isSelected: Boolean, position: Int) {
            binding.channelName.text = channel.name
            binding.channelNumber.text = (position + 1).toString()

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

            updateUI(isSelected)
        }

        private fun updateUI(hasFocus: Boolean) {
            if (hasFocus) {
                // Blue/Cyan background for focused item
                binding.rootLayout.setBackgroundColor(Color.parseColor("#0056B3")) 
                binding.channelName.setTextColor(Color.WHITE)
                binding.channelNumber.setTextColor(Color.WHITE)
            } else {
                binding.rootLayout.setBackgroundColor(Color.TRANSPARENT)
                binding.channelName.setTextColor(Color.WHITE)
                binding.channelNumber.setTextColor(Color.WHITE)
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
        holder.bind(channels[position], position == selectedPosition, position)
    }

    override fun getItemCount() = channels.size

    fun updateChannels(newChannels: List<Channel>) {
        channels = newChannels
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition >= 0 && previousPosition < channels.size) {
            notifyItemChanged(previousPosition)
        }
        if (selectedPosition >= 0 && selectedPosition < channels.size) {
            notifyItemChanged(selectedPosition)
        }
    }

    // Separate callback for focus events (preview only, no playback)
    private var onChannelFocus: (Channel) -> Unit = {}

    fun setOnChannelFocusListener(listener: (Channel) -> Unit) {
        onChannelFocus = listener
    }
}
