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
    // We will still use LocationCallback for continuous updates if needed,
    // but the API call will be triggered by the Timer.
    private lateinit var locationCallback: LocationCallback

    private var apiCallInterval = 25000L // Default API call interval
    private var locationRequestInterval = 10000L // Location updates can be more frequent or less frequent than API calls
                                                // We'll set it to a reasonable value to ensure we get updates.

    private var apiCallTimer: Timer? = null
    private var lastKnownLocation: Location? = null // Store the last received location
    private var apiCallJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 123
        const val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"
        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
        const val EXTRA_API_CALL_INTERVAL = "extra_api_call_interval" // New extra for API call interval
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
                intent.getLongExtra(EXTRA_API_CALL_INTERVAL, 25000L).let {
                    apiCallInterval = it
                    Log.d(TAG, "API call interval set to: $apiCallInterval ms")
                }
                startForegroundService()
                startLocationUpdates() // Keep receiving location updates
                startApiCallScheduler() // Start the timer for API calls
            }
            ACTION_STOP_FOREGROUND_SERVICE -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Tracking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Tracker")
            .setContentText("Tracking your location in the background.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Foreground service started with notification.")
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(locationRequestInterval) // Location updates can be more frequent
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(locationRequestInterval / 2)
            .setMaxUpdateDelayMillis(locationRequestInterval + 5000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "Location received and updated: ${location.latitude}, ${location.longitude}")
                    lastKnownLocation = location // Store the latest location
                } ?: run {
                    Log.w(TAG, "Location result is null.")
                }
            }

            override fun onLocationAvailability(p0: LocationAvailability) {
                if (!p0.isLocationAvailable) {
                    Log.e(TAG, "Location is not available.")
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates requested.")
        } else {
            Log.e(TAG, "Location permission not granted. Cannot start location updates.")
            // Decide if you want to stop the service here or just continue without location
            // For this scenario, we assume location is critical, so stopping might be reasonable.
            stopSelf()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped.")
    }

    private fun startApiCallScheduler() {
        apiCallTimer?.cancel() // Cancel any existing timer
        apiCallTimer = Timer()
        apiCallTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "Scheduled API call triggered.")
                // Attempt to get the latest location before making the API call
                if (ContextCompat.checkSelfPermission(this@LocationTrackingService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            lastKnownLocation = location
                            Log.d(TAG, "Last known location retrieved for API call: ${location.latitude}, ${location.longitude}")
                            callApiWithLocation(location)
                        } else {
                            Log.w(TAG, "Last known location is null. Trying last stored location or fetching current location.")
                            // If lastKnownLocation is null, try to use the previously stored one
                            lastKnownLocation?.let {
                                Log.d(TAG, "Using previously stored location for API call: ${it.latitude}, ${it.longitude}")
                                callApiWithLocation(it)
                            } ?: run {
                                Log.e(TAG, "No location available for API call.")
                                // Optionally, request a fresh location here if absolutely necessary
                                // Note: requesting fresh location might block the timer thread if not handled asynchronously
                            }
                        }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to get last known location: ${e.message}", e)
                        lastKnownLocation?.let {
                            Log.d(TAG, "Using previously stored location due to last known location failure: ${it.latitude}, ${it.longitude}")
                            callApiWithLocation(it)
                        } ?: run {
                            Log.e(TAG, "No location available for API call after failure.")
                        }
                    }
                } else {
                    Log.e(TAG, "Location permission not granted for scheduled API call.")
                    // This case should ideally not happen if startLocationUpdates already checked
                }
            }
        }, 0, apiCallInterval) // Start immediately, repeat every apiCallInterval
        Log.d(TAG, "API call scheduler started with interval: $apiCallInterval ms")
    }

    private fun stopApiCallScheduler() {
        apiCallTimer?.cancel()
        apiCallTimer = null
        Log.d(TAG, "API call scheduler stopped.")
    }

    private fun callApiWithLocation(location: Location) {
        apiCallJob?.cancel() // Cancel any previous pending API call if necessary

        apiCallJob = serviceScope.launch {
            try {
                Log.d(TAG, "Calling API with location: Lat=${location.latitude}, Lon=${location.longitude}")
                // Simulate network delay
                delay(1000)

                // TODO: Replace this with your actual API call using Retrofit, OkHttp, etc.
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
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopLocationUpdates()
        stopApiCallScheduler()
        apiCallJob?.cancel()
        serviceScope.cancel()
    }
}