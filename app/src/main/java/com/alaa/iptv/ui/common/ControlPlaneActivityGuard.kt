package com.alaa.iptv.ui.common

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.alaa.iptv.data.preferences.FeatureCatalog
import com.alaa.iptv.BuildConfig
import com.alaa.iptv.data.preferences.AppPreferences
import com.alaa.iptv.data.remote.TvProvisioningClient
import com.alaa.iptv.ui.login.LoginActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.WeakHashMap

object ControlPlaneActivityGuard {
    private const val TAG = "ControlPlaneGuard"
    private val refreshMutex = Mutex()
    private val displayedPolicies = WeakHashMap<Activity, String>()

    /**
     * يراجع الحظر والإعدادات المحدثة عند فتح النشاط أو عودته للمقدمة.
     * لا يسمح للجهاز المزوّد مركزياً والموقوف بالاستمرار في التطبيق.
     */
    suspend fun refreshAndEnforce(activity: Activity, prefs: AppPreferences, force: Boolean = false): Boolean = refreshMutex.withLock {
        if (prefs.isControlPlaneEnrolled && (force || prefs.shouldRefreshControlPlane())) {
            val tvId = prefs.getOrCreateTvId()
            val previousSync = prefs.lastControlPlaneSyncAt()
            val snapshot = TvProvisioningClient.syncControlPlane(tvId, BuildConfig.VERSION_NAME)
                .onFailure {
                    Log.w(TAG, "Control-plane sync failed; keeping last confirmed policy", it)
                    if (prefs.isFeatureEnabled(FeatureCatalog.SAFE_ERROR_LOG)) prefs.addSafeDiagnostic("control-plane", it)
                }
                .getOrNull()
            if (snapshot != null) {
                prefs.applyControlPlaneSnapshot(snapshot)
                if (previousSync > 0 && prefs.isFeatureEnabled(FeatureCatalog.REMOTE_CONFIG_CONFIRMATION)) {
                    activity.runOnUiThread { Toast.makeText(activity, "تم تحديث إعدادات التطبيق.", Toast.LENGTH_SHORT).show() }
                }
            }
            if (snapshot?.remoteLogoutRequested == true) {
                TvProvisioningClient.acknowledgeRemoteLogout(tvId)
                    .onFailure { Log.w(TAG, "Could not acknowledge remote logout", it) }
                prefs.clear()
                val intent = Intent(activity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                activity.startActivity(intent)
                activity.finish()
                return@withLock false
            }
        }

        if (prefs.isDeviceAccessBlocked()) {
            prefs.isLoggedIn = false
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(LoginActivity.EXTRA_DEVICE_BLOCKED, true)
            }
            activity.startActivity(intent)
            activity.finish()
            return@withLock false
        }

        when {
            prefs.isMaintenanceEnabled() -> {
                val message = prefs.maintenanceMessage().ifBlank { "التطبيق تحت الصيانة حالياً. يرجى المحاولة لاحقاً." }
                showBlockingPolicy(activity, "maintenance:$message", "صيانة مجدولة", message, "إغلاق") {
                    activity.finishAffinity()
                }
                return@withLock false
            }
            prefs.isForcedUpdateRequired(BuildConfig.VERSION_NAME) -> {
                val updateUrl = prefs.forcedUpdateUrl()
                val message = "يتطلب هذا الإصدار تحديث Alaa Player قبل متابعة الاستخدام."
                showBlockingPolicy(activity, "force-update:$updateUrl", "تحديث مطلوب", message, "تحديث الآن") {
                    if (updateUrl.isNotBlank()) activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl)))
                    activity.finishAffinity()
                }
                return@withLock false
            }
        }

        displayedPolicies.remove(activity)
        true
    }

    private fun showBlockingPolicy(
        activity: Activity,
        policyKey: String,
        title: String,
        message: String,
        actionLabel: String,
        onAction: () -> Unit
    ) {
        if (activity.isFinishing || displayedPolicies[activity] == policyKey) return
        displayedPolicies[activity] = policyKey
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(actionLabel) { _, _ -> onAction() }
                .setCancelable(false)
                .show()
        }
    }
}
