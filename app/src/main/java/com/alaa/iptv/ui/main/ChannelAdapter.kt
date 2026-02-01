package com.alaa.iptv.ui.main

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.databinding.ItemChannelBinding
import com.bumptech.glide.Glide

class ChannelAdapter(
    private var channels: List<Channel>,
    private val onChannelClick: (Channel) -> Unit,
    private val onChannelLongClick: (Channel) -> Unit,
    private val onReorderRequest: ((fromPosition: Int, toPosition: Int) -> Unit)? = null
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = -1
    private var isReorderMode = false
    private var reorderPosition = -1
    private val longPressHandler = Handler(Looper.getMainLooper())
    private val longPressDelay = 1000L // 1 second for long press

    inner class ChannelViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var longPressRunnable: Runnable? = null

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isReorderMode) {
                        // In reorder mode, clicking confirms the new position
                        confirmReorder(position)
                    } else {
                        onChannelClick(channels[position])
                        setSelectedPosition(position)
                    }
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

            // Enhanced focus handling with smooth animations
            binding.root.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    animateFocusChange(view, true)
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onChannelClick(channels[position])
                        setSelectedPosition(position)
                    }
                } else {
                    animateFocusChange(view, false)
                }
            }

            // Key event handling for long press on OK/CENTER button
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                    when (event.action) {
                        KeyEvent.ACTION_DOWN -> {
                            if (event.repeatCount == 0) {
                                startLongPressDetection()
                            }
                            false
                        }
                        KeyEvent.ACTION_UP -> {
                            cancelLongPressDetection()
                            false
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
        }

        private fun startLongPressDetection() {
            longPressRunnable = Runnable {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    enableReorderMode(position)
                }
            }
            longPressHandler.postDelayed(longPressRunnable!!, longPressDelay)
        }

        private fun cancelLongPressDetection() {
            longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
            longPressRunnable = null
        }

        private fun animateFocusChange(view: View, focused: Boolean) {
            val scale = if (focused) 1.05f else 1.0f
            val duration = 200L

            ObjectAnimator.ofFloat(view, "scaleX", view.scaleX, scale).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                start()
            }

            ObjectAnimator.ofFloat(view, "scaleY", view.scaleY, scale).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                start()
            }
        }

        fun bind(channel: Channel, isSelected: Boolean, isReordering: Boolean) {
            binding.channelName.text = channel.name
            binding.channelNumber.text = "Ch ${channel.num}"
            
            if (channel.isFavorite) {
                binding.favoriteIndicator.visibility = View.VISIBLE
            } else {
                binding.favoriteIndicator.visibility = View.GONE
            }

            // Show reorder indicator
            if (isReordering) {
                binding.reorderIndicator.visibility = View.VISIBLE
            } else {
                binding.reorderIndicator.visibility = View.GONE
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

            // Highlight selected or reorder item
            if (isReordering) {
                binding.root.setBackgroundColor(Color.parseColor("#AA2196F3"))
            } else if (isSelected) {
                binding.root.setBackgroundColor(Color.parseColor("#332196F3"))
            } else {
                binding.root.setBackgroundResource(R.drawable.channel_item_background)
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
        holder.bind(
            channels[position], 
            position == selectedPosition,
            isReorderMode && position == reorderPosition
        )
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

    private fun enableReorderMode(position: Int) {
        isReorderMode = true
        reorderPosition = position
        notifyItemChanged(position)
    }

    private fun confirmReorder(toPosition: Int) {
        if (isReorderMode && reorderPosition != toPosition) {
            onReorderRequest?.invoke(reorderPosition, toPosition)
        }
        isReorderMode = false
        val oldPosition = reorderPosition
        reorderPosition = -1
        notifyItemChanged(oldPosition)
        notifyItemChanged(toPosition)
    }

    fun disableReorderMode() {
        if (isReorderMode) {
            isReorderMode = false
            val oldPosition = reorderPosition
            reorderPosition = -1
            notifyItemChanged(oldPosition)
        }
    }
}
