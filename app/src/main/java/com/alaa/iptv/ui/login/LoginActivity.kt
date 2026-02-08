package com.alaa.iptv.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.databinding.ActivityLoginBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)

        // إذا المستخدم مسجّل دخول سابقاً ➜ روح مباشرة للداشبورد
        if (prefs.isLoggedIn) {
            navigateToDashboard()
            return
        }

        // تحميل رابط M3U المحفوظ (إن وُجد)
        binding.serverUrlInput.setText(prefs.serverUrl)
        binding.serverUrlInput.requestFocus()

        binding.loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val serverUrl = binding.serverUrlInput.text.toString().trim()

        // تحقق بسيط من رابط M3U
        if (serverUrl.isEmpty() || !serverUrl.startsWith("http")) {
            showError("Please enter a valid M3U URL")
            return
        }

        showLoading(true)

        // نحفظ رابط M3U فقط
        prefs.serverUrl = serverUrl
        prefs.isLoggedIn = true

        showLoading(false)
        navigateToDashboard()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.serverUrlInput.isEnabled = !show
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
