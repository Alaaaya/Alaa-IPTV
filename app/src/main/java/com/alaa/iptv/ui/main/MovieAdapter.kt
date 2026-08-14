package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Movie
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.alaa.iptv.ui.theme.DisplayTheme
import com.bumptech.glide.Glide

class MovieAdapter(
    private val movies: List<Movie>,
    private val hotPlayerTheme: Boolean = false,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onMovieClick(movies[position])
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.posterCard.setCardBackgroundColor(
                        if (hotPlayerTheme) DisplayTheme.hotAccentColor() else Color.parseColor("#E53935")
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

        fun bind(movie: Movie) {
            DisplayTheme.applyPosterCard(binding, hotPlayerTheme)
            binding.movieTitle.text = movie.name
            setOptionalText(binding.ratingBadge, movie.rating)
            setOptionalText(binding.yearText, movie.year ?: movie.releaseDate)

            Glide.with(binding.root.context)
                .load(movie.streamIcon)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size
}
