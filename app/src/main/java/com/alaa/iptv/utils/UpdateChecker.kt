package com.alaa.iptv.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.alaa.iptv.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** فحص وتثبيت اختياريين من GitHub Release الرسمي فقط؛ لا ينزّل ولا يثبت دون تأكيد المستخدم. */
class UpdateChecker(private val activity: AppCompatActivity) {
    companion object {
        private const val GITHUB_API = "https://api.github.com/repos/Alaaaya/Alaa-IPTV/releases/latest"
        private const val MIME_APK = "application/vnd.android.package-archive"
    }

    private val appContext = activity.applicationContext

    suspend fun checkForUpdate(showToast: Boolean = false) {
        val release = try {
            withContext(Dispatchers.IO) { fetchLatestRelease() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            if (showToast) Toast.makeText(activity, "تعذر فحص التحديث الآن", Toast.LENGTH_SHORT).show()
            return
        }

        if (!UpdateArtifactPolicy.isNewerVersion(release.version, BuildConfig.VERSION_NAME)) {
            if (showToast) Toast.makeText(activity, "أنت تستخدم آخر إصدار", Toast.LENGTH_SHORT).show()
            return
        }

        showUpdateDialog(release)
    }

    private fun fetchLatestRelease(): ReleaseInfo {
        val connection = (URL(GITHUB_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "AlaaPlayer/${BuildConfig.VERSION_NAME}")
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return try {
            val code = connection.responseCode
            require(code in 200..299) { "Release lookup failed" }
            val json = connection.inputStream.bufferedReader().use { it.readText() }.let(::JSONObject)
            val assets = json.optJSONArray("assets")
            val apkUrl = (0 until (assets?.length() ?: 0))
                .asSequence()
                .mapNotNull { assets?.optJSONObject(it) }
                .map { it.optString("browser_download_url", "") }
                .firstOrNull(UpdateArtifactPolicy::isTrustedDownloadUrl)
                .orEmpty()
            ReleaseInfo(
                version = json.getString("tag_name").removePrefix("v"),
                downloadUrl = apkUrl,
                releaseUrl = json.getString("html_url"),
                notes = UpdateArtifactPolicy.releaseNotesPreview(json.optString("body", ""))
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun showUpdateDialog(release: ReleaseInfo) {
        val message = buildString {
            append("يتوفر Alaa Player v${release.version}.\n\nالإصدار الحالي: v${BuildConfig.VERSION_NAME}")
            if (release.notes.isNotBlank()) append("\n\nما الجديد:\n${release.notes}")
            if (release.downloadUrl.isBlank()) append("\n\nملف APK غير متاح للتنزيل المباشر؛ يمكنك فتح صفحة الإصدار الرسمية.")
        }
        AlertDialog.Builder(activity)
            .setTitle("تحديث متاح")
            .setMessage(message)
            .setPositiveButton(if (release.downloadUrl.isBlank()) "فتح صفحة الإصدار" else "تنزيل وتثبيت") { _, _ ->
                if (release.downloadUrl.isBlank()) openReleasePage(release.releaseUrl) else downloadAndInstall(release)
            }
            .setNegativeButton("لاحقاً", null)
            .setNeutralButton("التفاصيل") { _, _ -> openReleasePage(release.releaseUrl) }
            .setCancelable(true)
            .show()
    }

    private fun downloadAndInstall(release: ReleaseInfo) {
        if (!UpdateArtifactPolicy.isTrustedDownloadUrl(release.downloadUrl)) {
            Toast.makeText(activity, "رابط التحديث غير موثوق", Toast.LENGTH_SHORT).show()
            return
        }
        val updatesDir = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: run {
            Toast.makeText(activity, "تعذر تجهيز مساحة التحديث", Toast.LENGTH_SHORT).show()
            return
        }
        updatesDir.mkdirs()
        val apkFile = File(updatesDir, "AlaaPlayer-${release.version}.apk")
        apkFile.delete()
        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(release.downloadUrl))
            .setTitle("تنزيل Alaa Player v${release.version}")
            .setDescription("سيطلب Android تأكيد التثبيت بعد اكتمال التنزيل.")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(appContext, Environment.DIRECTORY_DOWNLOADS, apkFile.name)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
        val downloadId = downloadManager.enqueue(request)
        registerDownloadReceiver(downloadManager, downloadId, apkFile)
        Toast.makeText(activity, "بدأ تنزيل التحديث بعد موافقتك", Toast.LENGTH_LONG).show()
    }

    private fun registerDownloadReceiver(downloadManager: DownloadManager, downloadId: Long, apkFile: File) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) != downloadId) return
                appContext.unregisterReceiver(this)
                val status = downloadManager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) else -1
                }
                if (status != DownloadManager.STATUS_SUCCESSFUL || !apkFile.isFile || !isInstallableAlaaPackage(apkFile)) {
                    Toast.makeText(activity, "تعذر التحقق من ملف التحديث", Toast.LENGTH_LONG).show()
                    apkFile.delete()
                    return
                }
                requestInstall(apkFile)
            }
        }
        ContextCompat.registerReceiver(appContext, receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun requestInstall(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !appContext.packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(activity)
                .setTitle("إذن تثبيت التحديث")
                .setMessage("يسمح Android للتطبيق بطلب تثبيت التحديث بعد موافقتك. فعّل الإذن ثم ارجع للتطبيق وأعد فحص التحديث.")
                .setPositiveButton("فتح الإذن") { _, _ ->
                    activity.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}")))
                }
                .setNegativeButton("إلغاء", null)
                .show()
            return
        }
        val apkUri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, MIME_APK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching { activity.startActivity(intent) }
            .onFailure { Toast.makeText(activity, "لا يوجد مثبّت APK متاح على هذا الجهاز", Toast.LENGTH_LONG).show() }
    }

    @Suppress("DEPRECATION")
    private fun isInstallableAlaaPackage(apkFile: File): Boolean {
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val archive = appContext.packageManager.getPackageArchiveInfo(apkFile.absolutePath, flags) ?: return false
        val installed = appContext.packageManager.getPackageInfo(appContext.packageName, flags)
        return archive.packageName == appContext.packageName &&
            versionCode(archive) > versionCode(installed) &&
            signerDigests(archive) == signerDigests(installed)
    }

    @Suppress("DEPRECATION")
    private fun versionCode(info: PackageInfo): Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        info.longVersionCode
    } else {
        info.versionCode.toLong()
    }

    @Suppress("DEPRECATION")
    private fun signerDigests(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners.orEmpty()
        } else {
            info.signatures.orEmpty()
        }
        return signatures.map { signature ->
            MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString("") { "%02x".format(it) }
        }.toSet()
    }

    private fun openReleasePage(url: String) {
        if (!UpdateArtifactPolicy.isTrustedReleasePageUrl(url)) {
            Toast.makeText(activity, "رابط صفحة الإصدار غير موثوق", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { Toast.makeText(activity, "تعذر فتح صفحة الإصدار", Toast.LENGTH_SHORT).show() }
    }

    private data class ReleaseInfo(
        val version: String,
        val downloadUrl: String,
        val releaseUrl: String,
        val notes: String
    )
}
