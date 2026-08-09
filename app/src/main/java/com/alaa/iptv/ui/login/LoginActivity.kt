package com.alaa.iptv.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityLoginBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, this)

        // Check if already logged in (isLoggedIn is a PROPERTY not a function)
        if (prefs.isLoggedIn) {
            Log.d(TAG, "Already logged in, navigating to dashboard")
            navigateToDashboard()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            val serverUrl = binding.serverUrlInput.text.toString().trim()
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInput(serverUrl, username, password)) {
                performLogin(serverUrl, username, password)
            }
        }
    }

    private fun validateInput(serverUrl: String, username: String, password: String): Boolean {
        when {
            serverUrl.isEmpty() -> {
                binding.serverUrlInput.error = "Server URL is required"
                return false
            }
            username.isEmpty() -> {
                binding.usernameInput.error = "Username is required"
                return false
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "Password is required"
                return false
            }
        }
        return true
    }

    private fun performLogin(serverUrl: String, username: String, password: String) {
        binding.loginButton.isEnabled = false
        binding.loginButton.text = "Logging in..."

        lifecycleScope.launch {
            try {
                // Save credentials first (direct property assignment)
                prefs.serverUrl = serverUrl
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true

                // Verify credentials by trying to fetch live streams
                val result = repository.getLiveStreams(null)

                if (result.isSuccess) {
                    val channels = result.getOrDefault(emptyList())
                    Log.d(TAG, "Login successful! Loaded ${channels.size} channels")
                    navigateToDashboard()
                } else {
                    // Credentials saved but API failed - still allow login
                    Log.w(TAG, "API check failed but credentials saved")
                    navigateToDashboard()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception", e)
                // Still save credentials and navigate
                prefs.serverUrl = serverUrl
                prefs.username = username
                prefs.password = password
                prefs.isLoggedIn = true
                navigateToDashboard()
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
