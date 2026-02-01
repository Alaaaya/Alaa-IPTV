package com.alaaaya.iptv.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.models.Channel
import com.alaaaya.iptv.utils.gone
import com.alaaaya.iptv.utils.visible

class ChannelAdapter(
    private val onItemClick: (Channel) -> Unit,
    private val onItemLongClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.ChannelViewHolder>(ChannelDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = getItem(position)
        holder.bind(channel, onItemClick, onItemLongClick)
    }
    
    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivChannelIcon: ImageView = itemView.findViewById(R.id.ivChannelIcon)
        private val tvChannelName: TextView = itemView.findViewById(R.id.tvChannelName)
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        
        fun bind(
            channel: Channel,
            onItemClick: (Channel) -> Unit,
            onItemLongClick: (Channel) -> Unit
        ) {
            tvChannelName.text = channel.name
            tvCategoryName.text = channel.categoryName.ifEmpty { "Uncategorized" }
            
            // Show/hide favorite indicator
            if (channel.isFavorite) {
                ivFavorite.visible()
            } else {
                ivFavorite.gone()
            }
            
            // TODO: Load icon with Glide
            // Glide.with(itemView.context)
            //     .load(channel.iconUrl)
            //     .placeholder(R.drawable.ic_placeholder)
            //     .into(ivChannelIcon)
            
            itemView.setOnClickListener {
                onItemClick(channel)
            }
            
            itemView.setOnLongClickListener {
                onItemLongClick(channel)
                true
            }
            
            itemView.isFocusable = true
            itemView.isClickable = true
        }
    }
    
    private class ChannelDiffCallback : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem == newItem
        }
    }
}
