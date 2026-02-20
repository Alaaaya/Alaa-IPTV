package com.alaa.iptv.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alaa.iptv.R
import com.alaa.iptv.databinding.ActivityDashboardBinding
import com.alaa.iptv.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private var _binding: ActivityDashboardBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateDateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDashboardMenu()
        startClock()
        setupStaticInfo()
    }

    private fun setupDashboardMenu() {
        val items = listOf(
            DashboardItem("القنوات", R.drawable.ic_live_tv) { openMain("live") },
            DashboardItem("الأفلام", R.drawable.ic_movies) { openMain("movies") },
            DashboardItem("المسلسلات", R.drawable.ic_series) { openMain("series") },
            DashboardItem("قائمة التشغيل", R.drawable.ic_playlist) { /* TODO */ },
            DashboardItem("الإعدادات", R.drawable.ic_settings) { /* TODO */ },
            DashboardItem("تحديث قائمة التشغيل", R.drawable.ic_refresh) { /* TODO */ }
        )

        val adapter = DashboardAdapter(items)
        binding.dashboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
            
            // Focus on first item for TV
            post {
                val firstView = layoutManager?.findViewByPosition(0)
                firstView?.requestFocus()
            }
        }
    }

    private fun setupStaticInfo() {
        // Logo
        binding.appLogo.setImageResource(R.drawable.app_logo_universe)
        
        // Static info to match image 100%
        binding.ramadanTv.text = "ramadan2025"
        binding.daysLeft.text = "(Days Left: 622)"
        binding.locationTv.text = "Oberhausen, DE"
        binding.weatherDesc.text = "Feels Like Clouds"
    }

    private fun startClock() {
        handler.post(updateTimeRunnable)
    }

    private fun updateDateTime() {
        val now = Date()
        
        // Time Format: 10:28 م
        val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
        binding.timeTv.text = timeFormat.format(now)

        // Date Format: الجمعة 20 أكتوبر 2026
        val dateFormat = SimpleDateFormat("EEEE dd MMMM yyyy", Locale("ar"))
        binding.hijriDate.text = dateFormat.format(now)

        // Short Date: 14/02/2025
        val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("ar"))
        binding.gregorianDate.text = shortDateFormat.format(now)
    }

    private fun openMain(mode: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("MODE", mode)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        _binding = null
    }
}
