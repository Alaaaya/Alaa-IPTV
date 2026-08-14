package com.alaa.iptv.utils

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.alaa.iptv.BuildConfig
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.io.File

class UpdateChecker(private val context: Context) {

    companion object {
        private const val TAG = "UpdateChecker"
        private const val GITHUB_API = "https://api.github.com/repos/Alaaaya/Alaa-IPTV/releases/latest"
        private val CURRENT_VERSION: String
            get() = BuildConfig.VERSION_NAME
    }

    suspend fun checkForUpdate(showToast: Boolean = false) {
        try {
            val releaseInfo = withContext(Dispatchers.IO) {
                val connection = URL(GITHUB_API).openConnection()
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "Alaa-IPTV-App")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val tagName = json.getString("tag_name").removePrefix("v")
                val downloadUrl = json.getJSONArray("assets")
                    .optJSONObject(0)
                    ?.optString("browser_download_url", "")
                    ?: ""
                val releaseUrl = json.getString("html_url")
                val releaseNotes = json.optString("body", "")

                Triple(tagName, downloadUrl, releaseUrl) to releaseNotes
            }

            val (versionInfo, releaseNotes) = releaseInfo
            val (latestVersion, downloadUrl, releaseUrl) = versionInfo

            if (isNewerVersion(latestVersion, CURRENT_VERSION)) {
                showUpdateDialog(latestVersion, downloadUrl, releaseUrl, releaseNotes)
            } else {
                if (showToast) {
                    Toast.makeText(context, "You have the latest version!", Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "App is up to date (current: $CURRENT_VERSION, latest: $latestVersion)")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check for updates", e)
            if (showToast) {
                Toast.makeText(context, "Failed to check for updates", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        return try {
            val latestParts = latest.split(".").map { it.toInt() }
            val currentParts = current.split(".").map { it.toInt() }

            for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                when {
                    l > c -> return true
                    l < c -> return false
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun showUpdateDialog(
        version: String,
        downloadUrl: String,
        releaseUrl: String,
        releaseNotes: String
    ) {
        val notes = releaseNotes.trim().take(600)
        val message = buildString {
            append("Version $version is now available.\n\nCurrent: v$CURRENT_VERSION")
            if (notes.isNotBlank()) append("\n\nWhat's new:\n$notes")
            append("\n\nWould you like to update?")
        }

        AlertDialog.Builder(context)
            .setTitle("New Version Available!")
            .setMessage(message)
            .setPositiveButton("Download & Install") { _, _ ->
                if (downloadUrl.isNotEmpty()) {
                    downloadAndInstall(downloadUrl, version)
                } else {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                    )
                }
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("View Details") { _, _ ->
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(releaseUrl))
                )
            }
            .setCancelable(false)
            .show()
    }

    private fun downloadAndInstall(downloadUrl: String, version: String) {
        val fileName = "alaa-iptv-v$version.apk"

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Downloading Alaa Player v$version")
            .setDescription("Please wait...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        Toast.makeText(context, "Downloading update...", Toast.LENGTH_LONG).show()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    context.unregisterReceiver(this)
                    installApk(fileName)
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun installApk(fileName: String) {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )

        if (!file.exists()) {
            Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            setDataAndType(uri, "application/vnd.android.package-archive")
        }

        context.startActivity(intent)
    }
}
