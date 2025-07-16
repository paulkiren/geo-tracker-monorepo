package com.nerikpaul.geopulsetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted. Attempting to restart location service.")
            context?.let {
                val serviceIntent = Intent(it, LocationTrackingService::class.java).apply {
                    action = LocationTrackingService.ACTION_START_FOREGROUND_SERVICE
                    // You might want to persist the interval or use a default
                    putExtra(LocationTrackingService.EXTRA_INTERVAL, 25000L)
                }
                // For Android O (API 26) and above, use startForegroundService()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(it, serviceIntent)
                } else {
                    it.startService(serviceIntent)
                }
            }
        }
    }
}