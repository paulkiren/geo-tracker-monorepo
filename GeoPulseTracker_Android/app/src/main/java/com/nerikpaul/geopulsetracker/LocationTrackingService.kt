package com.nerikpaul.geopulsetracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.Timer
import java.util.TimerTask

class LocationTrackingService : Service() {

    private val TAG = "LocationTrackingService"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequestInterval = 25000L // Default to 25 seconds (25000 milliseconds)
    private var apiCallJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 123
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        const val EXTRA_INTERVAL = "extra_interval"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        when (intent?.action) {
            ACTION_START_FOREGROUND_SERVICE -> {
                intent.getLongExtra(EXTRA_INTERVAL, 25000L).let {
                    locationRequestInterval = it
                    Log.d(TAG, "Location update interval set to: $locationRequestInterval ms")
                }
                startForegroundService()
                startLocationUpdates()
            }
            ACTION_STOP_FOREGROUND_SERVICE -> {
                stopSelf()
            }
        }
        // If the system kills the service, it will try to recreate it
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW // Use IMPORTANCE_LOW for less obtrusive notification
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Tracker")
            .setContentText("Tracking your location in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Or your app's icon
            .setPriority(NotificationCompat.PRIORITY_LOW) // Matches IMPORTANCE_LOW
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started with notification.")
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(locationRequestInterval)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(locationRequestInterval / 2) // Optional: minimum interval between updates
            .setMaxUpdateDelayMillis(locationRequestInterval + 5000L) // Optional: max delay before getting an update
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location received: ${location.latitude}, ${location.longitude}")
                    // Call your API here
                    callApiWithLocation(location)
                } ?: run {
                    Log.w(TAG, "Location result is null.")
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                if (!p0.isLocationAvailable) {
                    Log.e(TAG, "Location is not available.")
                    // Optionally, inform the user or try to enable location services
                }
            }
        }

        // Check for permissions at runtime before requesting updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper() // Use main looper or a dedicated background looper
            )
            Log.d(TAG, "Location updates requested.")
        } else {
            Log.e(TAG, "Location permission not granted. Cannot start location updates.")
            // Handle the case where permission is not granted, e.g., stop the service or inform the user.
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped.")
    }

    private fun callApiWithLocation(location: Location) {
        // Implement your API call logic here
        // This should be done on a background thread (e.g., using Coroutines, Retrofit, OkHttp)
        apiCallJob?.cancel() // Cancel any previous pending API call if necessary

        apiCallJob = serviceScope.launch {
            try {
                Log.d(TAG, "Calling API with location: Lat=${location.latitude}, Lon=${location.longitude}")
                // Simulate network delay
                delay(1000)

                // Replace this with your actual API call using Retrofit, OkHttp, etc.
                // Example using a placeholder:
                // val response = YourApiClient.apiService.sendLocation(location.latitude, location.longitude)
                // if (response.isSuccessful) {
                //     Log.d(TAG, "API call successful: ${response.body()}")
                // } else {
                //     Log.e(TAG, "API call failed: ${response.errorBody()?.string()}")
                // }

                Log.d(TAG, "API call simulation successful!")

            } catch (e: Exception) {
                Log.e(TAG, "API call error: ${e.message}", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // This service is not designed for binding
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopLocationUpdates()
        apiCallJob?.cancel()
        serviceScope.cancel() // Cancel all coroutines launched in serviceScope
    }
}