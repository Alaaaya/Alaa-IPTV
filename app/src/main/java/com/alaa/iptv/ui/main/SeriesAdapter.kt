package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.bumptech.glide.Glide

class SeriesAdapter(
    private val seriesList: List<Channel>,
    private val onSeriesClick: (Channel) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    inner class SeriesViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onSeriesClick(seriesList[bindingAdapterPosition])
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.posterCard.setCardBackgroundColor(Color.parseColor("#E53935"))
                    binding.root.scaleX = 1.1f
                    binding.root.scaleY = 1.1f
                } else {
                    binding.posterCard.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"))
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                }
            }
        }

        fun bind(series: Channel) {
            binding.movieTitle.text = series.name
            binding.ratingBadge.text = "${(8..9).random()}.${(0..9).random()}"
            // Series style: S1 - E1
            binding.yearText.text = "S${(1..12).random()} - E${(1..24).random()}"

            Glide.with(binding.root.context)
                .load(series.streamIcon)
                .placeholder(R.drawable.bg_dark_pattern)
                .into(binding.moviePoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val binding = ItemMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.bind(seriesList[position])
    }

    override fun getItemCount() = seriesList.size
}
