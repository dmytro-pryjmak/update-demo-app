package com.example.updatedemo.util.featureDownload

import android.content.Context

interface AppUpgrader {

    fun startApkUpgradingFlow(url: String)

    suspend fun downloadApk(url: String, context: Context)

    fun installApk(context: Context, apkFilePath: String)

}