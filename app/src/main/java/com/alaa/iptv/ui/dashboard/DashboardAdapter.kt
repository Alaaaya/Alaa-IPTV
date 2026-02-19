package com.alaa.iptv.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ItemDashboardBinding

class DashboardAdapter(
    private val items: List<DashboardItem>
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDashboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true
            
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.root.setBackgroundResource(R.drawable.bg_dashboard_focus)
                } else {
                    binding.root.setBackgroundResource(android.R.color.transparent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.title.text = item.title
        holder.binding.icon.setImageResource(item.icon)

        holder.binding.root.setOnClickListener {
            item.action.invoke()
        }
    }

    override fun getItemCount(): Int = items.size
}
