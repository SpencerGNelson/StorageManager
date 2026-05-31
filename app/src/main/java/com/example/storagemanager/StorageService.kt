package com.example.storagemanager

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StorageService : Service() {

    interface StorageCallback {
        fun onScanComplete(results: List<StorageScanner.AppStorageInfo>)
    }

    companion object {
        var callback: StorageCallback? = null
    }

    private lateinit var scanner: StorageScanner

    override fun onCreate() {
        super.onCreate()
        scanner = StorageScanner(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val results = scanner.scanAllApps()
        callback?.onScanComplete(results)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}