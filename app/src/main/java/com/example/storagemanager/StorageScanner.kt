package com.example.storagemanager

import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.os.storage.StorageManager

class StorageScanner(private val context: Context) {

    data class AppStorageInfo(
        val packageName: String,
        val appName: String,
        val cacheBytes: Long,
        val dataBytes: Long,
        val appBytes: Long
    )

    fun scanAllApps(): List<AppStorageInfo> {
        val storageStatsManager = context.getSystemService(
            Context.STORAGE_STATS_SERVICE
        ) as StorageStatsManager

        val packageManager = context.packageManager
        val installedApps = packageManager.getInstalledApplications(0)
        val storageUUID = StorageManager.UUID_DEFAULT

        return installedApps.mapNotNull { appInfo ->
            try {
                val stats = storageStatsManager.queryStatsForPackage(
                    storageUUID,
                    appInfo.packageName,
                    Process.myUserHandle()
                )
                AppStorageInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    cacheBytes = stats.cacheBytes,
                    dataBytes = stats.dataBytes,
                    appBytes = stats.appBytes
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }.sortedByDescending { it.cacheBytes + it.dataBytes + it.appBytes }
    }
}