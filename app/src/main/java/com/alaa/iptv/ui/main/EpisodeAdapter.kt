package com.alaa.iptv.ui.main

import android.graphics.Color
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alaa.iptv.data.models.Episode
import com.alaa.iptv.databinding.ItemEpisodeBinding
import com.alaa.iptv.ui.navigation.FocusBoundaryPolicy

class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onEpisodeClick: (Episode, Int) -> Unit
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
                    Color.parseColor(if (hasFocus) "#E53935" else "#1AFFFFFF")
                )
                binding.root.scaleX = if (hasFocus) 1.02f else 1f
                binding.root.scaleY = if (hasFocus) 1.02f else 1f
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
            val details = listOfNotNull(
                episode.info?.duration?.takeIf { it.isNotBlank() },
                episode.info?.rating?.takeIf { it.isNotBlank() }?.let { "★ $it" }
            )
            binding.episodeMeta.text = details.joinToString("  •  ")
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
        const val EPISODE_GRID_COLUMNS = 4
    }
}
