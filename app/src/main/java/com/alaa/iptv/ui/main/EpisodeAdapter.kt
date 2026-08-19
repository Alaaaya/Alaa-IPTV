package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.R
import com.alaa.iptv.data.models.Episode
import com.alaa.iptv.databinding.ItemEpisodeBinding
import com.alaa.iptv.ui.navigation.FocusBoundaryPolicy
import com.bumptech.glide.Glide

class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onEpisodeClick: (Episode, Int) -> Unit,
    private val completedEpisodeTitles: Set<String> = emptySet(),
    private val seriesName: String = "",
    private val fallbackImageUrl: String? = null
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onEpisodeClick(episodes[position], position)
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.episodeCard.setCardBackgroundColor(
                    Color.parseColor(if (hasFocus) "#2A3B111A" else "#10131D")
                )
            }
            binding.root.setOnKeyListener { _, keyCode, event ->
                if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                FocusBoundaryPolicy.blocksVerticalExit(
                    keyCode = keyCode,
                    position = bindingAdapterPosition,
                    itemCount = episodes.size,
                    spanCount = EPISODE_GRID_COLUMNS,
                    orientation = RecyclerView.VERTICAL
                )
            }
        }

        fun bind(episode: Episode) {
            binding.episodeNumber.text = "E${episode.episodeNum}"
            binding.episodeTitle.text = episode.title
            binding.episodeMeta.text = episode.info?.plot?.takeIf { it.isNotBlank() }
                ?: episode.info?.rating?.takeIf { it.isNotBlank() }?.let { "التقييم ★ $it" }
                ?: "حلقة من $seriesName"
            binding.episodeDuration.text = episode.info?.duration?.takeIf { it.isNotBlank() }.orEmpty()
            Glide.with(binding.root)
                .load(fallbackImageUrl)
                .placeholder(R.drawable.bg_dark_pattern)
                .error(R.drawable.bg_dark_pattern)
                .into(binding.episodeImage)
            val fullTitle = "$seriesName - ${episode.title}"
            binding.episodeCompletedBadge.visibility = if (completedEpisodeTitles.contains(fullTitle)) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount(): Int = episodes.size

    companion object {
        const val EPISODE_GRID_COLUMNS = 2
    }
}
