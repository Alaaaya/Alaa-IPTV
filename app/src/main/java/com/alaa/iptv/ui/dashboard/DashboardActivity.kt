package com.alaa.iptv.ui.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.ui.main.MainActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val items = listOf(
            DashboardItem("Live TV", R.drawable.ic_live_tv) {
                startActivity(Intent(this, MainActivity::class.java))
            },
            DashboardItem("Movies", R.drawable.ic_movies) {},
            DashboardItem("Series", R.drawable.ic_series) {},
            DashboardItem("Playlist", R.drawable.ic_playlist) {},
            DashboardItem("Settings", R.drawable.ic_settings) {},
            DashboardItem("Replay", R.drawable.ic_replay) {}
        )

        binding.menuRecycler.apply {
            layoutManager = LinearLayoutManager(
                this@DashboardActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = DashboardAdapter(items)
        }
    }
}
