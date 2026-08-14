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
                renderFocusState(hasFocus)
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
            renderFocusState(binding.root.hasFocus())
        }

        private fun renderFocusState(hasFocus: Boolean) {
            binding.focusGlow.visibility = if (hasFocus) android.view.View.VISIBLE else android.view.View.INVISIBLE
            binding.posterCard.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"))
            binding.root.animate()
                .scaleX(if (hasFocus) 1.025f else 1.0f)
                .scaleY(if (hasFocus) 1.025f else 1.0f)
                .translationZ(if (hasFocus) 18f else 0f)
                .setDuration(140L)
                .start()
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
