package com.nerikpaul.geopulsetracker.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.nerikpaul.geopulsetracker.config.AppConfig
import com.nerikpaul.geopulsetracker.data.LocationRequest as ApiLocationRequest
import com.nerikpaul.geopulsetracker.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private var locationCallback: LocationCallback? = null
    private var isTracking = false
    
    @SuppressLint("MissingPermission")
    fun startLocationTracking(intervalMinutes: Int, onLocationUpdate: (Location) -> Unit) {
        if (isTracking) return
        
        val locationRequest = LocationRequest.create().apply {
            interval = (intervalMinutes * 60 * 1000).toLong() // Convert minutes to milliseconds
            fastestInterval = (intervalMinutes * 60 * 1000).toLong()
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationUpdate(location)
                    sendLocationToServer(location)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
        
        isTracking = true
        Log.d("LocationService", "Started location tracking with interval: $intervalMinutes minutes")
    }
    
    fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        isTracking = false
        Log.d("LocationService", "Stopped location tracking")
    }
    
    private fun sendLocationToServer(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locationRequest = ApiLocationRequest(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    timestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date())
                )
                
                val response = ApiClient.locationService.sendLocation(
                    "Bearer ${AppConfig.AUTH_TOKEN}", 
                    locationRequest
                )
                
                if (response.isSuccessful) {
                    Log.d("LocationService", "Location sent successfully: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.e("LocationService", "Failed to send location: ${response.code()} - ${response.message()}")
                }
            } catch (e: java.net.UnknownHostException) {
                Log.e("LocationService", "Network error - unable to resolve host: ${e.message}")
                Log.i("LocationService", "Check your internet connection and server URL in AppConfig")
            } catch (e: java.io.IOException) {
                Log.e("LocationService", "Network I/O error: ${e.message}")
            } catch (e: Exception) {
                Log.e("LocationService", "Unexpected error sending location: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun isTracking(): Boolean = isTracking
}
