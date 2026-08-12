package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Channel
import com.alaa.iptv.databinding.ItemMovieCardBinding
import com.bumptech.glide.Glide

class MovieAdapter(
    private val movies: List<Channel>,
    private val onMovieClick: (Channel) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(private val binding: ItemMovieCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onMovieClick(movies[bindingAdapterPosition])
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

        fun bind(movie: Channel) {
            binding.movieTitle.text = movie.name
            binding.ratingBadge.text = "${(7..9).random()}.${(0..9).random()}"
            binding.yearText.text = "${(2010..2024).random()}"

            Glide.with(binding.root.context)
                .load(movie.streamIcon)
                .placeholder(R.drawable.bg_dark_pattern)
                .into(binding.moviePoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size
}
