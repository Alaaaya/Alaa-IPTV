package com.alaa.iptv.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dashboard آمن مؤقتًا
        setContentView(R.layout.activity_dashboard)
    }
}
