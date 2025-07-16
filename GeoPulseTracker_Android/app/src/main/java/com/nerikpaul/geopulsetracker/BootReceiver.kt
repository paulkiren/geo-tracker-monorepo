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
                    // Use a default or persisted interval for API calls
                    putExtra(LocationTrackingService.EXTRA_API_CALL_INTERVAL, 25000L) // Default to 25 seconds
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(it, serviceIntent)
                } else {
                    it.startService(serviceIntent)
                }
            }
        }
    }
}