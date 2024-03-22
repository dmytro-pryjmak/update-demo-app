package com.example.updatedemo.util.featureDownload

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AppUpgraderManager private constructor(context: Context) : AppUpgrader {

    private val appContext = context.applicationContext
    private val upgradingFlowScope = CoroutineScope(Job() + Dispatchers.IO)
    private var currentDownloadCall: Call? = null

    companion object {
        @Volatile private var AppUpgraderManagerInstance: AppUpgraderManager? = null

        private const val CHILD_NAME = "downloaded_apk.apk"
        private const val TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"

        fun getInstance(context: Context): AppUpgraderManager =
            AppUpgraderManagerInstance ?: synchronized(this) {
                AppUpgraderManagerInstance ?: AppUpgraderManager(context).also { AppUpgraderManagerInstance = it }
            }
    }

    override fun startApkUpgradingFlow(url: String) {
        upgradingFlowScope.launch {
            downloadApk(url, appContext)
        }
    }

    override suspend fun downloadApk(url: String, context: Context) {
        withContext(Dispatchers.IO) {
            val okHttpClient = OkHttpClient()
            val request = Request.Builder().url(url).build()

            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val inputStream = response.body?.byteStream()
                    val apkFile = File(context.getExternalFilesDir(null), CHILD_NAME)
                    val outputStream = FileOutputStream(apkFile)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input?.copyTo(output)
                        }
                    }
                    installApk(context, apkFile.absolutePath)
                }
            })
            if (!isActive) cancelUpgradingTasks()
        }
    }

    override fun installApk(context: Context, apkFilePath: String) {
        val apkUri: Uri =
            FileProvider.getUriForFile(context, context.applicationContext.packageName + PROVIDER_PATH, File(apkFilePath))

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, TYPE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun cancelUpgradingTasks() {
        upgradingFlowScope.coroutineContext.cancelChildren()
        currentDownloadCall?.cancel()
    }
}