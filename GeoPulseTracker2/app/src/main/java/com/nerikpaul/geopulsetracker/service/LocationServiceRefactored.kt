package com.nerikpaul.geopulsetracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.nerikpaul.geopulsetracker.R
import com.nerikpaul.geopulsetracker.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Modern LocationService implementation following Android best practices:
 * - Foreground service for background location tracking
 * - Repository pattern for data management
 * - Dependency injection with Hilt
 * - Proper lifecycle management
 * - Flow-based reactive programming
 * - Modern LocationRequest API
 * - Comprehensive error handling
 * - Battery optimization considerations
 */
@AndroidEntryPoint
class LocationServiceRefactored : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var notificationManager: NotificationManager
    
    private var locationCallback: LocationCallback? = null
    private var serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Configuration
    private var currentIntervalMinutes: Int = 5
    private var isTracking = false
    
    // Flow for location updates
    private val _locationUpdates = MutableSharedFlow<Location>()
    val locationUpdates: SharedFlow<Location> = _locationUpdates.asSharedFlow()
    
    // Flow for tracking state
    private val _trackingState = MutableStateFlow(TrackingState.STOPPED)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_TRACKING = "START_TRACKING"
        const val ACTION_STOP_TRACKING = "STOP_TRACKING"
        const val EXTRA_INTERVAL_MINUTES = "interval_minutes"
        
        private const val TAG = "LocationServiceRefactored"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 5)
                startLocationTracking(intervalMinutes)
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    private fun startLocationTracking(intervalMinutes: Int) {
        if (isTracking) {
            Log.d(TAG, "Location tracking already active")
            return
        }

        currentIntervalMinutes = intervalMinutes
        
        // Modern LocationRequest using Builder pattern
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMinutes.minutes.inWholeMilliseconds
        ).apply {
            setMinUpdateIntervalMillis(intervalMinutes.minutes.inWholeMilliseconds)
            setMaxUpdateDelayMillis(2 * intervalMinutes.minutes.inWholeMilliseconds)
            setWaitForAccurateLocation(true)
            setMinUpdateDistanceMeters(10f) // Only update if moved 10 meters
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    handleLocationUpdate(location)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    Log.w(TAG, "Location not available")
                    _trackingState.value = TrackingState.LOCATION_UNAVAILABLE
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            
            isTracking = true
            _trackingState.value = TrackingState.ACTIVE
            startForeground(NOTIFICATION_ID, createTrackingNotification())
            
            Log.i(TAG, "Started location tracking with ${intervalMinutes}min interval")
            
        } catch (exception: SecurityException) {
            Log.e(TAG, "Lost location permission. Could not request updates.", exception)
            _trackingState.value = TrackingState.PERMISSION_DENIED
            stopSelf()
        }
    }

    private fun stopLocationTracking() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
        
        isTracking = false
        _trackingState.value = TrackingState.STOPPED
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.i(TAG, "Stopped location tracking")
    }

    private fun handleLocationUpdate(location: Location) {
        // Validate location quality
        if (!isLocationValid(location)) {
            Log.w(TAG, "Received invalid location, skipping")
            return
        }

        // Emit location update
        serviceScope.launch {
            _locationUpdates.emit(location)
        }

        // Send to server with proper error handling and retry
        serviceScope.launch {
            try {
                locationRepository.sendLocationWithRetry(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = System.currentTimeMillis()
                ).collect { result ->
                    when (result) {
                        is LocationRepository.Result.Success -> {
                            Log.d(TAG, "Location sent successfully: ${location.latitude}, ${location.longitude}")
                            updateNotification("Location sent successfully")
                        }
                        is LocationRepository.Result.Error -> {
                            Log.e(TAG, "Failed to send location: ${result.exception.message}")
                            updateNotification("Failed to send location")
                        }
                        is LocationRepository.Result.Loading -> {
                            Log.d(TAG, "Sending location...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error handling location", e)
            }
        }
    }

    private fun isLocationValid(location: Location): Boolean {
        val age = System.currentTimeMillis() - location.time
        return location.accuracy < 100f && age < 5.minutes.inWholeMilliseconds
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing location tracking for GeoPulse Tracker"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createTrackingNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("GeoPulse Tracker")
            .setContentText("Tracking location every ${currentIntervalMinutes} minutes")
            .setSmallIcon(R.drawable.ic_location) // You'll need to add this icon
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(status: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("GeoPulse Tracker")
            .setContentText("$status • Every ${currentIntervalMinutes} min")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
            
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        serviceScope.cancel()
    }

    enum class TrackingState {
        STOPPED,
        ACTIVE,
        LOCATION_UNAVAILABLE,
        PERMISSION_DENIED
    }
}
