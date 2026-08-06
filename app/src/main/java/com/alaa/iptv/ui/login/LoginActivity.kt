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
        val username = binding.usernameInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        // التحقق من المدخلات
        if (serverUrl.isEmpty() || !serverUrl.startsWith("http")) {
            showError("الرجاء إدخال رابط سيرفر صحيح")
            return
        }

        // إذا لم يكن رابط M3U مباشر، نتحقق من اسم المستخدم وكلمة المرور
        val isM3U = serverUrl.contains(".m3u") || serverUrl.contains("get.php")
        if (!isM3U && (username.isEmpty() || password.isEmpty())) {
            showError("الرجاء إدخال اسم المستخدم وكلمة المرور")
            return
        }

        showLoading(true)

        // حفظ البيانات في الإعدادات
        prefs.serverUrl = serverUrl
        prefs.username = username
        prefs.password = password
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
