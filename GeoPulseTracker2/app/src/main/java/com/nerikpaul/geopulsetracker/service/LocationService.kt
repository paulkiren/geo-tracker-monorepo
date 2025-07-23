package com.nerikpaul.geopulsetracker.service

import android.content.Context
import android.location.Location

class LocationService(private val context: Context) {
    
    fun startLocationTracking(intervalMinutes: Int, onLocationUpdate: (Location) -> Unit) {
        // TODO: Implement location tracking
    }
    
    fun stopLocationTracking() {
        // TODO: Implement stop tracking
    }
}
