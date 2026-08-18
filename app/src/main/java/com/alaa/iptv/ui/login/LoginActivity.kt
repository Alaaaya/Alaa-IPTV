package com.alaa.iptv.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.data.remote.TvProvisioningClient
import com.alaa.iptv.data.remote.TvConnectionFailureReporter
import com.alaa.iptv.data.repository.MediaRepository
import com.alaa.iptv.databinding.ActivityLoginBinding
import com.alaa.iptv.ui.dashboard.DashboardActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        const val EXTRA_DEVICE_BLOCKED = "device_blocked"
        private const val SOURCE_XTREAM = "xtream"
        private const val SOURCE_M3U = "m3u"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: AppPreferences
    private lateinit var repository: MediaRepository
    private var passwordVisible = false
    private var selectedSourceType = SOURCE_XTREAM

    private val playlistPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        importPlaylist(uri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = AppPreferences(this)
        repository = MediaRepository(prefs, applicationContext)

        // Check if already logged in (isLoggedIn is a PROPERTY not a function)
        if (prefs.isLoggedIn) {
            Log.d(TAG, "Already logged in, navigating to dashboard")
            navigateToDashboard()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.loginButton.setOnClickListener { submitLogin() }
        binding.sourceXtreamButton.setOnClickListener { setSourceType(SOURCE_XTREAM) }
        binding.sourceM3uButton.setOnClickListener { setSourceType(SOURCE_M3U) }
        binding.importPlaylistButton.setOnClickListener {
            playlistPicker.launch(arrayOf(
                "application/x-mpegURL",
                "application/vnd.apple.mpegurl",
                "audio/x-mpegurl",
                "text/plain",
                "*/*"
            ))
        }
        binding.passwordVisibilityButton.setOnClickListener { togglePasswordVisibility() }
        binding.fetchTvIdButton.setOnClickListener { fetchTvSubscription() }
        binding.tvIdValue.text = prefs.getOrCreateTvId()
        setSourceType(SOURCE_XTREAM)
        listOf(binding.loginButton, binding.importPlaylistButton, binding.sourceXtreamButton, binding.sourceM3uButton).forEach { button ->
            button.setOnFocusChangeListener { view, hasFocus ->
                view.animate()
                    .scaleX(if (hasFocus) 1.025f else 1f)
                    .scaleY(if (hasFocus) 1.025f else 1f)
                    .setDuration(120)
                    .start()
            }
        }
    }

    private fun submitLogin() {
        val serverUrl = binding.serverUrlInput.text.toString().trim()
        val username = if (selectedSourceType == SOURCE_M3U) "" else binding.usernameInput.text.toString().trim()
        val password = if (selectedSourceType == SOURCE_M3U) "" else binding.passwordInput.text.toString().trim()
        if (validateInput(serverUrl, username, password)) {
            performLogin(serverUrl, username, password)
        }
    }

    private fun fetchTvSubscription() {
        val tvId = prefs.getOrCreateTvId()
        binding.errorText.visibility = android.view.View.GONE

        setLoading(true)
        lifecycleScope.launch {
            TvProvisioningClient.fetchSubscription(
                tvId,
                notifyOwner = prefs.isFeatureEnabled(FeatureCatalog.OWNER_ALERTS)
            ).onSuccess { subscription ->
                prefs.tvId = subscription.tvId
                prefs.isControlPlaneEnrolled = true
                binding.tvIdValue.text = subscription.tvId
                setSourceType(subscription.sourceType)
                binding.serverUrlInput.setText(subscription.serverUrl)
                binding.usernameInput.setText(subscription.username)
                binding.passwordInput.setText(subscription.password)
                performLogin(subscription.serverUrl, subscription.username, subscription.password)
            }.onFailure { error ->
                showLoginError(error.message ?: "تعذر جلب بيانات الجهاز")
                setLoading(false)
            }
        }
    }

    private fun togglePasswordVisibility() {
        passwordVisible = !passwordVisible
        binding.passwordInput.transformationMethod = if (passwordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        binding.passwordInput.setSelection(binding.passwordInput.text?.length ?: 0)
    }

    private fun importPlaylist(uriString: String) {
        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val uri = android.net.Uri.parse(uriString)
                    val targetDirectory = File(filesDir, "playlists").apply { mkdirs() }
                    val targetFile = File(targetDirectory, "imported_playlist.m3u")
                    contentResolver.openInputStream(uri)?.use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    } ?: error("تعذر قراءة الملف المختار")
                    targetFile.toURI().toString()
                }
            }.onSuccess { localUrl ->
                setSourceType(SOURCE_M3U)
                binding.serverUrlInput.setText(localUrl)
                binding.usernameInput.text?.clear()
                binding.passwordInput.text?.clear()
                submitLogin()
            }.onFailure { error ->
                showLoginError(error.message ?: "تعذر استيراد القائمة. اختر ملف M3U صالحاً.")
                setLoading(false)
            }
        }
    }

    private fun validateInput(serverUrl: String, username: String, password: String): Boolean {
        binding.errorText.visibility = android.view.View.GONE
        if (selectedSourceType == SOURCE_M3U) {
            if (serverUrl.isEmpty()) {
                binding.serverUrlInput.error = "يرجى إدخال رابط M3U الكامل"
                return false
            }
            return true
        }
        when {
            serverUrl.isEmpty() -> {
                binding.serverUrlInput.error = "يرجى إدخال رابط الاشتراك"
                return false
            }
            repository.isM3U(serverUrl) -> {
                binding.serverUrlInput.error = "هذا رابط M3U. اختر نوع المصدر M3U أولاً"
                return false
            }
            username.isEmpty() -> {
                binding.usernameInput.error = "يرجى إدخال اسم المستخدم"
                return false
            }
            password.isEmpty() -> {
                binding.passwordInput.error = "يرجى إدخال كلمة المرور"
                return false
            }
        }
        return true
    }

    private fun performLogin(serverUrl: String, username: String, password: String) {
        if (prefs.isDeviceAccessBlocked()) {
            showLoginError("هذا الجهاز موقوف من لوحة التحكم. تواصل مع المالك لإعادة التفعيل.")
            return
        }
        setLoading(true)

        lifecycleScope.launch {
            val validation = repository.validateLogin(
                serverUrl = serverUrl,
                username = username,
                password = password,
                forceM3U = selectedSourceType == SOURCE_M3U
            )
            validation.onSuccess {
                prefs.serverUrl = serverUrl.trim()
                prefs.username = username
                prefs.password = password
                prefs.useM3U = selectedSourceType == SOURCE_M3U || repository.isM3U(serverUrl)
                prefs.m3uUrl = if (prefs.useM3U) serverUrl.trim() else ""
                prefs.isLoggedIn = true
                prefs.resetConnectionFailures()
                repository.clearCache()
                Log.d(TAG, "Login validated successfully")
                navigateToDashboard()
            }.onFailure { error ->
                prefs.isLoggedIn = false
                if (prefs.isFeatureEnabled(FeatureCatalog.OWNER_ALERTS) && prefs.registerConnectionFailure() >= 3) {
                    TvConnectionFailureReporter.report(
                        prefs.getOrCreateTvId(),
                        error.message ?: "فشل التحقق من الاشتراك"
                    )
                    prefs.resetConnectionFailures()
                }
                Log.w(TAG, "Login validation failed", error)
                showLoginError(error.message ?: "تعذر تسجيل الدخول، تحقق من بيانات الاشتراك")
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.loginButton.isEnabled = !loading
        binding.importPlaylistButton.isEnabled = !loading
        binding.serverUrlInput.isEnabled = !loading
        binding.usernameInput.isEnabled = !loading
        binding.passwordInput.isEnabled = !loading
        binding.fetchTvIdButton.isEnabled = !loading
        binding.passwordVisibilityButton.isEnabled = !loading
        binding.sourceXtreamButton.isEnabled = !loading
        binding.sourceM3uButton.isEnabled = !loading
        binding.loginButton.text = if (loading) "جاري تسجيل الدخول…" else getString(com.alaa.iptv.R.string.login)
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showLoginError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = android.view.View.VISIBLE
    }

    private fun setSourceType(sourceType: String) {
        selectedSourceType = if (sourceType == SOURCE_M3U) SOURCE_M3U else SOURCE_XTREAM
        val isM3U = selectedSourceType == SOURCE_M3U
        binding.sourceXtreamButton.setBackgroundResource(
            if (isM3U) com.alaa.iptv.R.drawable.bg_login_secondary_button else com.alaa.iptv.R.drawable.bg_login_primary_button
        )
        binding.sourceM3uButton.setBackgroundResource(
            if (isM3U) com.alaa.iptv.R.drawable.bg_login_primary_button else com.alaa.iptv.R.drawable.bg_login_secondary_button
        )
        binding.serverUrlInput.hint = if (isM3U) "رابط M3U الكامل" else "رابط خادم Xtream"
        binding.sourceTypeHint.text = if (isM3U) {
            "الصق رابط M3U الكامل فقط؛ لا تحتاج اسم المستخدم أو كلمة المرور هنا."
        } else {
            "Xtream يحتاج رابط الخادم واسم المستخدم وكلمة المرور."
        }
        (binding.usernameInput.parent as? android.view.View)?.visibility = if (isM3U) android.view.View.GONE else android.view.View.VISIBLE
        (binding.passwordInput.parent as? android.view.View)?.visibility = if (isM3U) android.view.View.GONE else android.view.View.VISIBLE
        binding.serverUrlInput.nextFocusDownId = if (isM3U) com.alaa.iptv.R.id.loginButton else com.alaa.iptv.R.id.usernameInput
        binding.loginButton.nextFocusUpId = if (isM3U) com.alaa.iptv.R.id.serverUrlInput else com.alaa.iptv.R.id.passwordInput
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

}
