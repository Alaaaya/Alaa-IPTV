package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.databinding.ItemChannelBinding
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class ChannelAdapter(
    private var channels: List<Channel>,
    private val hotPlayerTheme: Boolean = false,
    private val onChannelClick: (Channel) -> Unit,
    private val onChannelLongClick: (Channel) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = -1
    private var onChannelFocus: (Channel) -> Unit = {}

    inner class ChannelViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onChannelClick(channels[position])
                }
            }

            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onChannelLongClick(channels[position])
                }
                true
            }

            binding.root.setOnFocusChangeListener { _, hasFocus ->
                updateUI(hasFocus)
                if (hasFocus) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        setSelectedPosition(position)
                        onChannelFocus(channels[position])
                    }
                }
            }

            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                val position = bindingAdapterPosition
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_UP -> position == 0
                    KeyEvent.KEYCODE_DPAD_DOWN -> position == channels.lastIndex
                    else -> false
                }
            }
        }

        fun bind(channel: Channel, isSelected: Boolean, position: Int) {
            binding.channelName.text = channel.name
            binding.channelNumber.text = (position + 1).toString()
            
            // iBO Player Style: Quality tag
            binding.qualityTag.text = if (channel.name.contains("HD", true)) "HD" else "SD"
            
            // Favorite indicator
            binding.favoriteIndicator.visibility = View.VISIBLE
            binding.favoriteIndicator.alpha = if (channel.isFavorite) 1.0f else 0.3f

            if (hotPlayerTheme) {
                binding.root.background = DisplayTheme.hotPanelBackground()
                binding.channelNumber.background = DisplayTheme.hotFocusBackground()
                binding.channelNumber.setTextColor(Color.parseColor("#0A1426"))
                binding.qualityTag.background = DisplayTheme.hotPanelBackground()
                binding.qualityTag.setTextColor(DisplayTheme.hotBlueColor())
            }

            if (!channel.streamIcon.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(channel.streamIcon)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(binding.channelIcon)
            } else {
                binding.channelIcon.setImageResource(R.drawable.ic_logo)
            }

            updateUI(isSelected)
        }

        private fun updateUI(hasFocus: Boolean) {
            if (hasFocus) {
                if (hotPlayerTheme) {
                    binding.root.background = DisplayTheme.hotFocusBackground()
                    binding.channelName.setTextColor(Color.parseColor("#0A1426"))
                    binding.channelNumber.setTextColor(Color.parseColor("#0A1426"))
                    binding.qualityTag.setTextColor(Color.parseColor("#0A1426"))
                    binding.favoriteIndicator.setColorFilter(Color.parseColor("#0A1426"))
                } else {
                    binding.root.setBackgroundResource(R.drawable.bg_sidebar_selected)
                    binding.channelName.setTextColor(Color.WHITE)
                    binding.channelNumber.setTextColor(Color.WHITE)
                    binding.qualityTag.setTextColor(Color.WHITE)
                    binding.favoriteIndicator.setColorFilter(Color.WHITE)
                }
            } else {
                if (hotPlayerTheme) {
                    binding.root.background = DisplayTheme.hotPanelBackground()
                    binding.channelNumber.setTextColor(Color.parseColor("#0A1426"))
                    binding.qualityTag.setTextColor(DisplayTheme.hotBlueColor())
                } else {
                    binding.root.setBackgroundColor(Color.TRANSPARENT)
                }
                binding.channelName.setTextColor(Color.WHITE)
                if (!hotPlayerTheme) {
                    binding.channelNumber.setTextColor(Color.parseColor("#808080"))
                    binding.qualityTag.setTextColor(Color.parseColor("#808080"))
                }
                binding.favoriteIndicator.setColorFilter(Color.parseColor("#808080"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val binding = ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        if (previousPosition >= 0 && previousPosition < channels.size) notifyItemChanged(previousPosition)
        if (selectedPosition >= 0 && selectedPosition < channels.size) notifyItemChanged(selectedPosition)
    }

    fun setOnChannelFocusListener(listener: (Channel) -> Unit) {
        onChannelFocus = listener
    }
}
