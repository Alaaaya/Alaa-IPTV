package com.alaa.iptv.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.R
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityLoginBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs)

        // إذا مسجّل دخول من قبل ➜ روح عالداشبورد
        if (prefs.isLoggedIn) {
            navigateToDashboard()
            return
        }

        // تحميل البيانات المحفوظة
        binding.serverUrlInput.setText(prefs.serverUrl)
        binding.usernameInput.setText(prefs.username)

        binding.serverUrlInput.requestFocus()

        binding.loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val serverUrl = binding.serverUrlInput.text.toString().trim()
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.login_error))
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val result = repository.authenticate(serverUrl, username, password)

            result.onSuccess { response ->
                if (response.userInfo?.auth == 1) {
                    // حفظ البيانات
                    prefs.serverUrl = serverUrl
                    prefs.username = username
                    prefs.password = password
                    prefs.isLoggedIn = true

                    showLoading(false)
                    navigateToDashboard()
                } else {
                    showLoading(false)
                    showError(response.userInfo?.message ?: getString(R.string.login_error))
                }
            }.onFailure {
                showLoading(false)
                showError(it.message ?: getString(R.string.network_error))
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginButton.isEnabled = !show
        binding.serverUrlInput.isEnabled = !show
        binding.usernameInput.isEnabled = !show
        binding.passwordInput.isEnabled = !show
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // ✅ هنا التغيير المهم
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
