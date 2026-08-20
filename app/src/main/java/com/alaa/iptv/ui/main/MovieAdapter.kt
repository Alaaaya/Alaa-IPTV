package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.alaa.iptv.ui.navigation.FocusBoundaryPolicy
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class MovieAdapter(
    private val movies: List<Movie>,
    private val displayTheme: String = AppPreferences.THEME_ALAA_CLASSIC,
    private val onMovieClick: (Movie) -> Unit,
    private val onMovieLongClick: ((Movie) -> Unit)? = null,
    private val onMovieFocused: ((Movie) -> Unit)? = null,
    private val isPosterDataSaver: Boolean = false,
    private val gridSpan: Int = DisplayTheme.mediaGridSpan(displayTheme)
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onMovieClick(movies[position])
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION || onMovieLongClick == null) false
                else {
                    onMovieLongClick.invoke(movies[position])
                    true
                }
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                renderFocusState()
                if (hasFocus) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) onMovieFocused?.invoke(movies[position])
                }
            }
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                FocusBoundaryPolicy.blocksVerticalExit(
                    keyCode = keyCode,
                    position = bindingAdapterPosition,
                    itemCount = movies.size,
                    spanCount = gridSpan,
                    orientation = RecyclerView.VERTICAL
                )
            }
        }

        fun bind(movie: Movie) {
            DisplayTheme.applyPosterCard(binding, displayTheme)
            binding.movieTitle.text = movie.name
            setOptionalText(binding.ratingBadge, movie.rating)
            setOptionalText(binding.yearText, movie.year ?: movie.releaseDate)
            setOptionalText(binding.contentBadge, qualityOrNewBadge(movie.name, movie.year ?: movie.releaseDate))

            if (isPosterDataSaver) {
                Glide.with(binding.root.context).clear(binding.moviePoster)
                binding.moviePoster.setImageResource(R.drawable.bg_dark_pattern)
            } else {
                Glide.with(binding.root.context)
                    .load(movie.streamIcon)
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

        private fun qualityOrNewBadge(name: String, date: String?): String? = when {
            name.contains("4k", ignoreCase = true) || name.contains("uhd", ignoreCase = true) -> "4K"
            name.contains("fhd", ignoreCase = true) || name.contains("1080", ignoreCase = true) -> "FHD"
            name.contains("hd", ignoreCase = true) -> "HD"
            date?.startsWith("2026") == true -> "جديد"
            else -> null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}
