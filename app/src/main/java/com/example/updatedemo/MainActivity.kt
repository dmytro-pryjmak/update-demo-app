package com.example.updatedemo

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.updatedemo.util.featureDownload.AppUpgraderManager
import com.example.updatedemo.util.featureDownload.InstallPermissionChecker

class MainActivity : AppCompatActivity() {

    companion object {
        private const val APK_URL = "your apk url"
    }

    private lateinit var permissionChecker: InstallPermissionChecker
    private lateinit var appUpgraderManager: AppUpgraderManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionChecker = InstallPermissionChecker(this)
        appUpgraderManager = AppUpgraderManager.getInstance(this)

        findViewById<Button>(R.id.button).setOnClickListener {
            checkAndStartDownloading()
        }
    }

    private fun checkAndStartDownloading() {
        permissionChecker.checkPermission(
            onGranted = {
                startDownloading()
                showToast(getString(R.string.downloading))
            },
            onDenied = {
                showToast(getString(R.string.permission_required_toast))
            }
        )
    }

    private fun startDownloading() {
        appUpgraderManager.startApkUpgradingFlow(APK_URL)
    }

    private fun showToast(text: CharSequence) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
