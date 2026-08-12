package com.alaa.iptv.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
        binding.errorText.visibility = android.view.View.GONE
        when {
            serverUrl.isEmpty() -> {
                binding.serverUrlInput.error = "أدخل رابط السيرفر"
                return false
            }
            !repository.isM3U(serverUrl) && username.isEmpty() -> {
                binding.usernameInput.error = "أدخل اسم المستخدم"
                return false
            }
            !repository.isM3U(serverUrl) && password.isEmpty() -> {
                binding.passwordInput.error = "أدخل كلمة المرور"
                return false
            }
        }
        return true
    }

    private fun performLogin(serverUrl: String, username: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            val validation = repository.validateLogin(serverUrl, username, password)
            validation.onSuccess {
                prefs.serverUrl = serverUrl.trim()
                prefs.username = username
                prefs.password = password
                prefs.useM3U = repository.isM3U(serverUrl)
                prefs.m3uUrl = if (prefs.useM3U) serverUrl.trim() else ""
                prefs.isLoggedIn = true
                repository.clearCache()
                Log.d(TAG, "Login validated successfully")
                navigateToDashboard()
            }.onFailure { error ->
                prefs.isLoggedIn = false
                Log.w(TAG, "Login validation failed", error)
                showLoginError(error.message ?: "تعذر الاتصال بالسيرفر. تحقق من البيانات وحاول مجدداً.")
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.loginButton.isEnabled = !loading
        binding.loginButton.text = if (loading) "جارٍ التحقق..." else getString(com.alaa.iptv.R.string.login)
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showLoginError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = android.view.View.VISIBLE
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
