package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class SeriesAdapter(
    private val seriesList: List<Series>,
    private val displayTheme: String = AppPreferences.THEME_ALAA_CLASSIC,
    private val onSeriesClick: (Series) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    inner class SeriesViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onSeriesClick(seriesList[position])
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.posterCard.setCardBackgroundColor(
                        if (DisplayTheme.hasCustomTheme(displayTheme)) DisplayTheme.metadataColor(displayTheme) else Color.parseColor("#E53935")
                    )
                    binding.root.scaleX = 1.05f
                    binding.root.scaleY = 1.05f
                } else {
                    binding.posterCard.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"))
                    binding.root.scaleX = 1.0f
                    binding.root.scaleY = 1.0f
                }
            }
        }

        fun bind(series: Series) {
            DisplayTheme.applyPosterCard(binding, displayTheme)
            binding.movieTitle.text = series.name
            setOptionalText(binding.ratingBadge, series.rating)
            setOptionalText(binding.yearText, series.releaseDate)

            Glide.with(binding.root.context)
                .load(series.cover)
                .placeholder(R.drawable.bg_dark_pattern)
                .error(R.drawable.bg_dark_pattern)
                .into(binding.moviePoster)
        }

        private fun setOptionalText(view: android.widget.TextView, value: String?) {
            val text = value?.trim().orEmpty()
            view.text = text
            view.visibility = if (text.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val binding = ItemMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.bind(seriesList[position])
    }

    override fun getItemCount(): Int = seriesList.size
}
