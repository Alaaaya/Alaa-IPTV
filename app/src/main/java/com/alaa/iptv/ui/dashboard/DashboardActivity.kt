package com.alaa.iptv.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R
import com.alaa.iptv.ui.main.MainActivity

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnLive = findViewById<Button>(R.id.btnLive)
        val btnMovies = findViewById<Button>(R.id.btnMovies)
        val btnSeries = findViewById<Button>(R.id.btnSeries)

        btnLive.requestFocus()

        btnLive.setOnClickListener {
            openMain("live")
        }

        btnMovies.setOnClickListener {
            openMain("movies")
        }

        btnSeries.setOnClickListener {
            openMain("series")
        }
    }

    private fun openMain(mode: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("MODE", mode)
        startActivity(intent)
    }
}
