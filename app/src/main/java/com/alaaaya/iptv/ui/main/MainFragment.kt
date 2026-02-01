package com.alaaaya.iptv.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.local.AppDatabase
import com.alaaaya.iptv.data.repository.ChannelRepository
import com.alaaaya.iptv.data.repository.UserRepository

class MainFragment : Fragment() {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var channelRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val database = AppDatabase.getInstance(requireContext())
        val channelRepository = ChannelRepository(database.channelDao())
        val userRepository = UserRepository(database.userDao())
        viewModel = MainViewModel(channelRepository, userRepository)
        
        // Initialize views
        titleTextView = view.findViewById(R.id.main_title)
        channelRecyclerView = view.findViewById(R.id.channel_recycler_view)
        
        // Set up RecyclerView
        channelRecyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        
        // Observe channels
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            // TODO: Set up adapter with channels
            titleTextView.text = "${getString(R.string.main_title)} - ${channels.size} channels"
        }
        
        // Load channels
        viewModel.loadChannels()
    }
}
