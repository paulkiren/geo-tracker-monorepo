package com.nerikpaul.geopulsetracker.manager

import android.content.Context
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.nerikpaul.geopulsetracker.service.LocationServiceRefactored
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class to handle location tracking operations
 * Follows the Single Responsibility Principle and provides a clean API
 */
@Singleton
class LocationTrackingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : DefaultLifecycleObserver {

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _currentInterval = MutableStateFlow(5)
    val currentInterval: StateFlow<Int> = _currentInterval.asStateFlow()

    init {
        // Register lifecycle observer to handle app lifecycle changes
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * Start location tracking with specified interval
     */
    fun startTracking(intervalMinutes: Int) {
        if (_isTracking.value) return

        _currentInterval.value = intervalMinutes
        _isTracking.value = true

        val intent = Intent(context, LocationServiceRefactored::class.java).apply {
            action = LocationServiceRefactored.ACTION_START_TRACKING
            putExtra(LocationServiceRefactored.EXTRA_INTERVAL_MINUTES, intervalMinutes)
        }
        
        context.startForegroundService(intent)
    }

    /**
     * Stop location tracking
     */
    fun stopTracking() {
        if (!_isTracking.value) return

        _isTracking.value = false

        val intent = Intent(context, LocationServiceRefactored::class.java).apply {
            action = LocationServiceRefactored.ACTION_STOP_TRACKING
        }
        
        context.startService(intent)
    }

    /**
     * Update tracking interval
     */
    fun updateInterval(intervalMinutes: Int) {
        _currentInterval.value = intervalMinutes
        
        if (_isTracking.value) {
            // Restart tracking with new interval
            stopTracking()
            startTracking(intervalMinutes)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App went to background - tracking continues via foreground service
        super.onStop(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        // App came to foreground
        super.onStart(owner)
    }
}
