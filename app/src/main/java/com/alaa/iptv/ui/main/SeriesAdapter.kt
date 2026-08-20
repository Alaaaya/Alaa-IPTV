package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Series
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.alaa.iptv.ui.navigation.FocusBoundaryPolicy
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class SeriesAdapter(
    private val seriesList: List<Series>,
    private val displayTheme: String = AppPreferences.THEME_ALAA_CLASSIC,
    private val onSeriesClick: (Series) -> Unit,
    private val onSeriesLongClick: ((Series) -> Unit)? = null,
    private val onSeriesFocused: ((Series) -> Unit)? = null,
    private val isPosterDataSaver: Boolean = false,
    private val gridSpan: Int = DisplayTheme.mediaGridSpan(displayTheme)
) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    inner class SeriesViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onSeriesClick(seriesList[position])
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION || onSeriesLongClick == null) false
                else {
                    onSeriesLongClick.invoke(seriesList[position])
                    true
                }
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                renderFocusState()
                if (hasFocus) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) onSeriesFocused?.invoke(seriesList[position])
                }
            }
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                FocusBoundaryPolicy.blocksVerticalExit(
                    keyCode = keyCode,
                    position = bindingAdapterPosition,
                    itemCount = seriesList.size,
                    spanCount = gridSpan,
                    orientation = RecyclerView.VERTICAL
                )
            }
        }

        fun bind(series: Series) {
            DisplayTheme.applyPosterCard(binding, displayTheme)
            binding.movieTitle.text = series.name
            setOptionalText(binding.ratingBadge, series.rating)
            setOptionalText(binding.yearText, series.releaseDate)
            setOptionalText(binding.contentBadge, if (series.releaseDate?.startsWith("2026") == true) "جديد" else null)

            if (isPosterDataSaver) {
                Glide.with(binding.root.context).clear(binding.moviePoster)
                binding.moviePoster.setImageResource(R.drawable.bg_dark_pattern)
            } else {
                Glide.with(binding.root.context)
                    .load(series.cover)
                    .placeholder(R.drawable.bg_dark_pattern)
                    .error(R.drawable.bg_dark_pattern)
                    .into(binding.moviePoster)
            }
            renderFocusState()
        }

        private fun renderFocusState() {
            binding.focusGlow.visibility = android.view.View.GONE
            binding.posterCard.setCardBackgroundColor(Color.parseColor("#1AFFFFFF"))
            binding.root.scaleX = 1f
            binding.root.scaleY = 1f
            binding.root.translationZ = 0f
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
