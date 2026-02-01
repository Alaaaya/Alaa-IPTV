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
import com.alaa.iptv.ui.main.MainActivity
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
        repository = MediaRepository(prefs, this)
        
        // Check if already logged in
        if (prefs.isLoggedIn) {
            navigateToMain()
            return
        }
        
        // Load saved credentials
        binding.serverUrlInput.setText(prefs.serverUrl)
        binding.usernameInput.setText(prefs.username)
        
        // Request focus on first input
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
            try {
                val result = repository.authenticate(serverUrl, username, password)
                
                result.onSuccess { response ->
                    if (response.userInfo?.auth == 1) {
                        // Save credentials
                        prefs.serverUrl = serverUrl
                        prefs.username = username
                        prefs.password = password
                        prefs.isLoggedIn = true
                        
                        showLoading(false)
                        navigateToMain()
                    } else {
                        showLoading(false)
                        showError(response.userInfo?.message ?: getString(R.string.login_error))
                    }
                }.onFailure { error ->
                    showLoading(false)
                    showError(error.message ?: getString(R.string.network_error))
                }
            } catch (e: Exception) {
                showLoading(false)
                showError(e.message ?: getString(R.string.network_error))
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
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
