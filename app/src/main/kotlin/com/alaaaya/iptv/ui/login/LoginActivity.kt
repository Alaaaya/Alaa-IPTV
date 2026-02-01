package com.alaaaya.iptv.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaaaya.iptv.R
import com.alaaaya.iptv.data.api.XtreamCodesApi
import com.alaaaya.iptv.data.db.AppDatabase
import com.alaaaya.iptv.data.models.UserCredentials
import com.alaaaya.iptv.ui.main.MainActivity
import com.alaaaya.iptv.utils.Constants
import com.alaaaya.iptv.utils.Result
import com.alaaaya.iptv.utils.gone
import com.alaaaya.iptv.utils.showToast
import com.alaaaya.iptv.utils.visible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    
    private lateinit var etServerUrl: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: android.widget.ProgressBar
    
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        database = AppDatabase.getInstance(this)
        
        initViews()
        setupListeners()
        checkSavedCredentials()
    }
    
    private fun initViews() {
        etServerUrl = findViewById(R.id.etServerUrl)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }
        
        etPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                attemptLogin()
                true
            } else {
                false
            }
        }
    }
    
    private fun checkSavedCredentials() {
        lifecycleScope.launch {
            val credentials = database.userCredentialsDao().getLastCredentials()
            if (credentials != null) {
                etServerUrl.setText(credentials.serverUrl)
                etUsername.setText(credentials.username)
                etPassword.setText(credentials.password)
            }
        }
    }
    
    private fun attemptLogin() {
        val serverUrl = etServerUrl.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        
        if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showToast(getString(R.string.empty_fields))
            return
        }
        
        performLogin(serverUrl, username, password)
    }
    
    private fun performLogin(serverUrl: String, username: String, password: String) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                // Create API client
                val api = createApiClient(serverUrl)
                
                // Authenticate
                val response = api.authenticate(username, password)
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    
                    if (authResponse?.user_info?.auth == 1 || 
                        authResponse?.user_info?.status == "Active") {
                        // Save credentials
                        saveCredentials(serverUrl, username, password)
                        
                        showToast(getString(R.string.login_success))
                        
                        // Navigate to main activity
                        startMainActivity()
                    } else {
                        showToast(getString(R.string.login_failed))
                    }
                } else {
                    showToast(getString(R.string.login_failed))
                }
            } catch (e: Exception) {
                showToast(getString(R.string.login_error))
                e.printStackTrace()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private suspend fun saveCredentials(serverUrl: String, username: String, password: String) {
        val credentials = UserCredentials(
            serverUrl = serverUrl,
            username = username,
            password = password
        )
        database.userCredentialsDao().deleteAllCredentials()
        database.userCredentialsDao().insertCredentials(credentials)
        
        // Also save to SharedPreferences for quick access
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(Constants.KEY_SERVER_URL, serverUrl)
            putString(Constants.KEY_USERNAME, username)
            putString(Constants.KEY_PASSWORD, password)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    private fun createApiClient(serverUrl: String): XtreamCodesApi {
        val cleanUrl = serverUrl.removeSuffix("/")
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Constants.NETWORK_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("$cleanUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        return retrofit.create(XtreamCodesApi::class.java)
    }
    
    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visible()
            btnLogin.isEnabled = false
        } else {
            progressBar.gone()
            btnLogin.isEnabled = true
        }
    }
    
    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
