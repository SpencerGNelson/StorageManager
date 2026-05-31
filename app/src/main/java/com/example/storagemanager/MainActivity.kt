package com.example.storagemanager

import android.app.AppOpsManager
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), StorageService.StorageCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StorageService.callback = this

        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }

        findViewById<Button>(R.id.scanButton).setOnClickListener {
            startService(Intent(this, StorageService::class.java))
        }
    }

    override fun onScanComplete(results: List<StorageScanner.AppStorageInfo>) {
        runOnUiThread {
            displayResults(results)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StorageService.callback = null
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun displayResults(results: List<StorageScanner.AppStorageInfo>) {
        val textView = findViewById<TextView>(R.id.resultsText)
        val output = StringBuilder()

        results.forEach { app ->
            output.appendLine(app.appName)
            output.appendLine("  Cache: ${formatBytes(app.cacheBytes)}")
            output.appendLine("  Data:  ${formatBytes(app.dataBytes)}")
            output.appendLine("  App:   ${formatBytes(app.appBytes)}")
            output.appendLine()
        }

        textView.text = output.toString()
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.2f KB".format(bytes / 1_024.0)
            else -> "$bytes B"
        }
    }
}