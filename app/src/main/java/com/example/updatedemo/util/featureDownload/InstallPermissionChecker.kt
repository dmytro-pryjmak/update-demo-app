package com.example.updatedemo.util.featureDownload

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class InstallPermissionChecker(private val activity: AppCompatActivity) {

    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (activity.packageManager.canRequestPackageInstalls()) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDenied?.invoke()
            }
        }
    }

    fun checkPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                requestPermissionLauncher.launch(intent)
            } else {
                onPermissionGranted?.invoke()
            }
        } else {
            onPermissionGranted?.invoke()
        }
    }
}
